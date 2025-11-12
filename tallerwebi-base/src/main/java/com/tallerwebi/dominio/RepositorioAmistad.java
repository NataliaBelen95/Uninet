package com.tallerwebi.dominio;

import java.util.List;

import java.util.List;

public interface RepositorioAmistad {
    void guardar(Amistad amistad);

    void actualizar(Amistad amistad);
    boolean sonAmigos(Usuario u1, Usuario u2);
    Amistad buscarPorUsuarios(Usuario solicitante, Usuario solicitado);
    List<Usuario>  obtenerAmigosDeUsuario(long idUsuario);
    // Devuelve las relaciones de amistad en estado ACEPTADA donde participa usuarioId
    List<Amistad> obtenerAmistadesAceptadasDe(long usuarioId);

    // Devuelve true si existe una amistad aceptada entre los dos usuarios
    boolean existeAmistadAceptadaEntre(long usuarioAId, long usuarioBId);

}
