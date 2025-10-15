package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoSePudoExtraerElTextoDelPDFException;

public interface ServicioHacerResumen {
    String extraerTexto(String rutaArchivo)throws NoSePudoExtraerElTextoDelPDFException;
    String generarResumen(String rutaArchivo) throws NoSePudoExtraerElTextoDelPDFException;

}
