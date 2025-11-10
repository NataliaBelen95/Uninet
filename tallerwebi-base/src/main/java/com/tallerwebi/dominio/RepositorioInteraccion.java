package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;


public interface RepositorioInteraccion {
    void guardar(Interaccion interaccion);

    List<Interaccion> encontrarDeUsuario(Usuario usuario);

    // Buscar todas las interacciones sobre una publicación
    List<Interaccion> encontrarDePubli(Publicacion publicacion);

    // Buscar interacciones por usuario y tipo (ej: todos los likes)
   // List<Interaccion> encontrarDeUsuarioAndTipo(Usuario usuario, String tipo);

    // Buscar interacciones de un usuario sobre una publicación específica
    //List<Interaccion> encontrarPorUsuarioYPubli(Usuario usuario, Publicacion publicacion);

    void eliminar(long interaccionId);

    String consolidarTextoInteraccionesRecientes(Usuario usuario, int limite);

    Interaccion encontrarInteraccionPorId(long interaccionId);
}
