package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;
import com.tallerwebi.dominio.excepcion.NoSePuedeSubirArchivoPorFallaException;
import com.tallerwebi.presentacion.DatosUsuario;
import org.springframework.web.multipart.MultipartFile;

public interface ServicioSubirArchivoALaIA {
    String guardarArchivoPdf(MultipartFile archivo, DatosUsuario usuario)
            throws NoSePuedeSubirArchivoPorFallaException, NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;
}
