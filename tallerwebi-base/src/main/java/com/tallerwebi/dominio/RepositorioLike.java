package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioLike {

    boolean existePorUsuarioYPublicacion(long usuarioId, long publiId);

    int contarPorPublicacion(long publiId);

    Like guardar(Like like);

    void eliminar(long id);

    Like buscarPorId(long id);
/*CAMBIOS A ID*/
    Like buscarPorUsuarioYPublicacion(long usuarioId, long publiId);
}