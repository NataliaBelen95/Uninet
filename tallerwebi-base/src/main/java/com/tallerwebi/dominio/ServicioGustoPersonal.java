package com.tallerwebi.dominio;

public interface ServicioGustoPersonal {

    /** Guarda o actualiza un perfil de gustos. */
    void guardarOActualizar(GustosPersonal gustos);

    /** Busca el perfil de gustos de un usuario, devuelve null si no existe. */
    GustosPersonal buscarPorUsuario(Usuario usuario);



}