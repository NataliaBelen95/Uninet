package com.tallerwebi.dominio.excepcion;

public class NoSePudoGenerarResumenDelPDFException extends RuntimeException{

    public NoSePudoGenerarResumenDelPDFException() {
        super("No se pudo generar un resumen del pdf seleccionado");
    }
}
