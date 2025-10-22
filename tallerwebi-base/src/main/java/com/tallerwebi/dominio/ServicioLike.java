package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;


public interface ServicioLike {

    void darLike(long usuarioId, long publicacionId);

    void quitarLike(long id);

    boolean yaDioLike(long usuarioId, long publicacionId);

    int contarLikes(Publicacion publicacion);

    Like obtenerLike(Usuario usuario, Publicacion publicacion);
}



