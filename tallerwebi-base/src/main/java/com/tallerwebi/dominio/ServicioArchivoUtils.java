package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ExtraccionTextoFallida;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ServicioArchivoUtils {
    private static final String RUTA_BASE_ARCHIVOS = System.getProperty("user.dir");

    /**
     * Extrae el texto de un archivo PDF dado su ruta relativa dentro del proyecto.
     * * @param rutaRelativa La ruta guardada en ArchivoPublicacion (ej: archivosPublicacion/archivo.pdf).
     * @return El texto extraído del PDF.
     * @throws ExtraccionTextoFallida Si ocurre un error de E/S o de procesamiento del PDF.
     */
    public String extraerTextoDePdf(String rutaRelativa) throws ExtraccionTextoFallida {

        // Combina la ruta base con la ruta relativa del archivo
        File file = new File(RUTA_BASE_ARCHIVOS, rutaRelativa);

        if (!file.exists()) {
            throw new ExtraccionTextoFallida("Archivo PDF no encontrado en la ruta: " + file.getAbsolutePath());
        }

        try (PDDocument document = PDDocument.load(file)) {

            if (document.isEncrypted()) {
                throw new ExtraccionTextoFallida("El PDF está encriptado y no se puede leer.");
            }

            // PDFTextStripper es la clase de PDFBox que extrae el texto
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();

        } catch (IOException e) {
            // Captura errores de E/S, formato de PDF, o al cargar el documento
            throw new ExtraccionTextoFallida("Error al procesar el archivo PDF (" + rutaRelativa + "): " + e.getMessage(), e);
        }
    }
}
