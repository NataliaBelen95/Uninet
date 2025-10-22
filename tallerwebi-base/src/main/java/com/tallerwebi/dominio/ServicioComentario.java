package com.tallerwebi.dominio;


import com.tallerwebi.presentacion.DatosComentario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServicioComentario {
    Comentario comentar(DatosComentario dto, Usuario usuario, Publicacion p);
    void editarComentario(Comentario comentario);
    int contarComentarios(long id);
    List<Comentario> encontrarComentariosPorId(long id);
    Usuario usuarioqueComento(long usuId);

}
