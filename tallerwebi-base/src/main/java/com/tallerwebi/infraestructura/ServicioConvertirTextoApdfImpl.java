package com.tallerwebi.infraestructura;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class ServicioConvertirTextoApdfImpl implements ServicioConvertirTextoApdf {
    public File generarPdf(String resumenHtml, String nombreArchivo) throws IOException {
        File dir = new File(System.getProperty("user.dir") + "/archivos_pdf");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Parseamos y convertimos a XHTML válido
        Document doc = Jsoup.parse(resumenHtml);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);  // <-- clave aquí

        String htmlXhtml = doc.html();

        File archivoPdf = new File(dir, nombreArchivo);

        try (OutputStream os = new FileOutputStream(archivoPdf)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlXhtml, null);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Error al generar PDF con HTML: " + e.getMessage(), e);
        }

        return archivoPdf;
    }

}
