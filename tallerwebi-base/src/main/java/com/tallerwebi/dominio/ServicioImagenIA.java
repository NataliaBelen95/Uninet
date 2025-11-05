package com.tallerwebi.dominio;

public interface ServicioImagenIA {

    /**
     * Genera un prompt para la IA de imagen basado en el tema analizado
     * y devuelve la URL de la imagen resultante.
     */
    String generarImagenRelacionada(String temaPrincipal) throws Exception;
}