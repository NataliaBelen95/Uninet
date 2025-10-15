package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServicioComentario {
    void comentar(String texto, Usuario usuario, Publicacion p);
    void editarComentario(Comentario comentario);
    int contarComentarios(Publicacion publicacion);
    List<Comentario> encontrarComentariosPorId(long id);

}
