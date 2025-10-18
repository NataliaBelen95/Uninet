package com.tallerwebi.infraestructura;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


public interface ServicioSubirResumenAPublicacion {
    File generarPdf(String resumen, String nombreArchivo)throws IOException;
    MultipartFile obtenerArchivoPdf(String nombreArchivoOriginal)throws IOException;
}
