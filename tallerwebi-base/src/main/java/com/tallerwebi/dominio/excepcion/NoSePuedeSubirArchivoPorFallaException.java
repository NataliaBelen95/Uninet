package com.tallerwebi.dominio.excepcion;

public class NoSePuedeSubirArchivoPorFallaException extends RuntimeException {

    public NoSePuedeSubirArchivoPorFallaException() {
        super("No se pudo subir el archivo al sistema.Falla general");
    }

    public NoSePuedeSubirArchivoPorFallaException(String detalle) {
        super("No se pudo subir el archivo al sistema. Falla general. Detalle: " + detalle);
    }


}
