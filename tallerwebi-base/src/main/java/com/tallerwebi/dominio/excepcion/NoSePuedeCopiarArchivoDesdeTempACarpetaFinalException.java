package com.tallerwebi.dominio.excepcion;

public class NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException extends RuntimeException {

    public NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException() {
        super("No se pudo copiar el archivo desde carpeta temporal a la carpeta final.");
    }
}
