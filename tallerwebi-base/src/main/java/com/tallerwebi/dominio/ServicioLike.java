package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;


public interface ServicioLike {

    void darLike(Usuario usuario, Publicacion publicacion);

    void quitarLike(long id);

    boolean yaDioLike(Usuario usuario, Publicacion publicacion);

    int contarLikes(long publiId);

    Like obtenerLike(long usuarioId, long publiId);
}



