package com.tallerwebi.dominio;


import java.util.List;

public interface RepositorioComentario {

    void guardar (Comentario comentario);

    void eliminar(Comentario comentario);
    Comentario buscar(long id);
    int contarComentarioPorPublicacion(Publicacion publicacion);
    List<Comentario> findComentariosByPublicacionId(long publicacionId);

}
