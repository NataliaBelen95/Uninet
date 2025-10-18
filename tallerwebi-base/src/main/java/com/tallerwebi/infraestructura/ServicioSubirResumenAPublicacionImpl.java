package com.tallerwebi.infraestructura;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;  // CORRECTO PARA PRODUCCIÓN
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ServicioSubirResumenAPublicacionImpl implements ServicioSubirResumenAPublicacion {

    @Override
    public MultipartFile obtenerArchivoPdf(String nombreArchivoOriginal) throws IOException {
        String rutaArchivo = System.getProperty("user.dir") + "/archivos_pdf/" + nombreArchivoOriginal;
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            throw new IOException("El archivo no existe en la carpeta de archivos PDF.");
        }

        try (FileInputStream inputStream = new FileInputStream(archivo)) {
            byte[] contenido = IOUtils.toByteArray(inputStream);  // Leemos todo el contenido del archivo

            // Convertimos el contenido del archivo a un MultipartFile (usando CommonsMultipartFile)
            FileItem fileItem = new DiskFileItem("file", "application/pdf", false, archivo.getName(), (int) archivo.length(), archivo.getParentFile());
            fileItem.getOutputStream().write(contenido); // Escribimos el contenido en el OutputStream

            // Devolvemos el archivo como MultipartFile
            return new CommonsMultipartFile(fileItem);
        }
    }

    // Método para convertir el resumen en HTML a un archivo PDF
    @Override
    public File generarPdf(String resumenHtml, String nombreArchivo) throws IOException {
        if (resumenHtml == null || resumenHtml.trim().isEmpty()) {
            throw new IOException("El resumen está vacío. No se puede generar el PDF.");
        }

        // Limpiar el HTML para asegurar que está bien formado usando Jsoup
        try {
            // Parsear el HTML para asegurarnos de que está bien formado
            Document doc = Jsoup.parse(resumenHtml);
            resumenHtml = doc.html(); // El HTML limpio y bien formado

        } catch (Exception e) {
            throw new IOException("Error al limpiar el HTML: " + e.getMessage(), e);
        }

        // Directorio de archivos PDF
        File dir = new File(System.getProperty("user.dir") + "/archivos_pdf");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Creamos el archivo PDF
        File archivoPdf = new File(dir, nombreArchivo);
        try (FileOutputStream os = new FileOutputStream(archivoPdf)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(resumenHtml, null); // Puede incluir una URL base si es necesario
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Error al generar PDF con HTML: " + e.getMessage(), e);
        }

        return archivoPdf;
    }

}





