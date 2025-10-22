package com.tallerwebi.infraestructura;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tallerwebi.dominio.ServicioPdfGenerator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ServicioPdfGeneratorImpl implements ServicioPdfGenerator {



    public byte[] generarPdfDesdeHtml(String resumenHtml) throws IOException {
        if (resumenHtml == null || resumenHtml.trim().isEmpty()) {
            throw new IOException("El resumen está vacío. No se puede generar el PDF.");
        }
        resumenHtml = cerrarEtiquetasHuérfanas(resumenHtml);
        resumenHtml = convertirAHtmlCompleto(resumenHtml);
        // Usamos Jsoup para limpiar y parsear HTML
        Document doc = Jsoup.parse(resumenHtml, "", Parser.xmlParser());
        resumenHtml = doc.outerHtml();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(resumenHtml, null);
            builder.toStream(baos);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Error al generar PDF con HTML: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    public File generarPdfYGuardar(String resumenHtml, String nombreArchivo) throws IOException {
        if (resumenHtml == null || resumenHtml.trim().isEmpty()) {
            throw new IOException("El resumen está vacío. No se puede generar el PDF.");
        }
        resumenHtml = cerrarEtiquetasHuérfanas(resumenHtml);
        resumenHtml = convertirAHtmlCompleto(resumenHtml);
        // Parsear como XHTML
        Document doc = Jsoup.parse(resumenHtml, "", Parser.xmlParser());
        resumenHtml = doc.outerHtml();
        // Crear carpeta si no existe
        File dir = new File(System.getProperty("user.dir") + "/archivos_pdf");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File archivoPdf = new File(dir, nombreArchivo);

        try (FileOutputStream os = new FileOutputStream(archivoPdf)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(resumenHtml, null);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Error al generar PDF con HTML: " + e.getMessage(), e);
        }

        return archivoPdf;
    }



    public MultipartFile obtenerArchivoPdf(String nombreArchivoOriginal) throws IOException {
        String rutaArchivo = System.getProperty("user.dir") + "/archivos_pdf/" + nombreArchivoOriginal;
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            throw new IOException("El archivo no existe en la carpeta de archivos PDF.");
        }

        // Simplemente envolver el archivo en nuestro FileMultipartFile
        return new FileMultipartFile(archivo, "application/pdf");
    }


    private String convertirAHtmlCompleto(String contenido) {
        return "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\"/>\n" +
                "    <style>\n" +
                "      body { font-family: Arial, sans-serif; }\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body>\n" +
                contenido + "\n" +
                "  </body>\n" +
                "</html>";
    }
    private String cerrarEtiquetasHuérfanas(String html) {
        // Etiquetas que deben cerrarse solas en XHTML
        String[] etiquetas = {"br", "hr", "img", "input", "meta", "link"};

        for (String etiqueta : etiquetas) {
            // Regex que cierra la etiqueta si no termina con "/>"
            html = html.replaceAll("<" + etiqueta + "(\\s+[^>]*)?>", "<" + etiqueta + "$1/>");
        }
        return html;
    }
}
