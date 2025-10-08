package com.tallerwebi.dominio;



public interface RepositorioComentario {

    void guardar (Comentario comentario);

    void eliminar(long id);
    Comentario buscar(long id);


}
