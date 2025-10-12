package com.tallerwebi.dominio;



public interface RepositorioComentario {

    void guardar (Comentario comentario);

    void eliminar(Comentario comentario);
    Comentario buscar(long id);
    int contarComentarioPorPublicacion(Publicacion publicacion);


}
