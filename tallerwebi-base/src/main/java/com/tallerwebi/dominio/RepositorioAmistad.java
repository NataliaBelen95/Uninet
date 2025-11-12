package com.tallerwebi.dominio;

import java.util.List;

import java.util.List;

public interface RepositorioAmistad {
    void guardar(Amistad amistad);

    void actualizar(Amistad amistad);
    boolean sonAmigos(Usuario u1, Usuario u2);
    Amistad buscarPorUsuarios(Usuario solicitante, Usuario solicitado);
    List<Usuario>  obtenerAmigosDeUsuario(long idUsuario);


}
