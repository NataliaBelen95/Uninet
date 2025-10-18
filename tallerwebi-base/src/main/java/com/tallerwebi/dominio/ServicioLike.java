package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;


public interface ServicioLike {

    void darLike(long usuarioId, long publicacionId);

    void quitarLike(long id);

    boolean yaDioLike(long usuarioId, long publicacionId);

    int contarLikes(long publiId);

    Like obtenerLike(long usuarioId, long publiId);

    void toggleLike(long usuarioId, long publicacionId);
}



