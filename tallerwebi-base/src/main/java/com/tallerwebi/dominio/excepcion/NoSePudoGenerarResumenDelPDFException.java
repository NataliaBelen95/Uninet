package com.tallerwebi.dominio.excepcion;

public class NoSePudoGenerarResumenDelPDFException extends RuntimeException{

    public NoSePudoGenerarResumenDelPDFException() {

        super("No se pudo generar un resumen del pdf seleccionado");
    }
    public NoSePudoGenerarResumenDelPDFException(String detalle) {
        super("No se pudo generar un resumen del pdf seleccionado. \n Detalle: " + detalle);
    }
}
