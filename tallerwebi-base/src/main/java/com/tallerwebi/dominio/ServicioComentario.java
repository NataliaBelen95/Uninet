package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;

@Service
public interface ServicioComentario {
    void comentar(String texto, Usuario usuario, Publicacion p);
    void editarComentario(Comentario comentario);
    int contarComentarios(Publicacion publicacion);
}
