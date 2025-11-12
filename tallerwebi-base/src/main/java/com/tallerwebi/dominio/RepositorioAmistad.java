package com.tallerwebi.dominio;

import java.util.List;

import java.util.List;

public interface RepositorioAmistad {
    void guardar(Amistad amistad);
    Amistad buscarPorUsuarios(Usuario solicitante, Usuario solicitado);
    List<Amistad> listarSolicitudesPendientes(Usuario usuario);
    List<Amistad> listarAmigos(Usuario usuario);
    void actualizar(Amistad amistad);

    List<Usuario> listarAmigosPorUsuario(long l);

    List<Usuario> obtenerAmigosDeUsuario(long l);
    boolean sonAmigos(Usuario u1, Usuario u2);
}
