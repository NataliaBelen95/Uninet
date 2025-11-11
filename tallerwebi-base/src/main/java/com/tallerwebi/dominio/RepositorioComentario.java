package com.tallerwebi.dominio;


import java.util.List;

public interface RepositorioComentario {

    Comentario guardar (Comentario comentario);

    void eliminar(Comentario comentario);
    Comentario buscar(long id);
    int contarComentarioPorPublicacion(long publicacion);
    List<Comentario> findComentariosByPublicacionId(long publicacionId);
    //Usuario encontrarUsuarioQueHizoComentario(long comentarioId);
}
