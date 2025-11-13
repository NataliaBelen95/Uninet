package com.tallerwebi.dominio.excepcion;

public class ExtraccionTextoFallida extends Exception {
    public ExtraccionTextoFallida(String mensaje) {
        super(mensaje);
    }
    public ExtraccionTextoFallida(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}