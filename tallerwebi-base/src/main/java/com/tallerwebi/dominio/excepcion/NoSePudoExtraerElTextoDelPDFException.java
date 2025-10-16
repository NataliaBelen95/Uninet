package com.tallerwebi.dominio.excepcion;

public class NoSePudoExtraerElTextoDelPDFException extends RuntimeException{

    public NoSePudoExtraerElTextoDelPDFException() {
        super("No se pudo extraer el texto del pdf para hacer el resumen");
    }
    public NoSePudoExtraerElTextoDelPDFException(String detalle) {
        super("No se pudo extraer el texto del pdf para hacer el resumen.\n Detalle: " + detalle);
    }
}
