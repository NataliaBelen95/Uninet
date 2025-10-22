package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioLike {

    boolean existePorUsuarioYPublicacion(long usuarioId, long publiId);

    int contarPorPublicacion(Publicacion publicacion);

    Like guardar(Like like);

    void eliminar(Like like);

    Like buscarPorId(long id);

    Like buscarPorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion);
}