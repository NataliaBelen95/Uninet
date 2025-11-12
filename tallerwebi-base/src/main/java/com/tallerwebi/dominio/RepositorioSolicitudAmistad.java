package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioSolicitudAmistad {

    void guardar(SolicitudAmistad solicitud);

    SolicitudAmistad buscarPorId(Long id);

    void actualizar(SolicitudAmistad solicitud);

    List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario);

    SolicitudAmistad buscarSolicitudActiva(Usuario u1, Usuario u2);

    List<Usuario> buscarAmigos(Usuario usuario);


    List<SolicitudAmistad> buscarSolicitudPendientePorUsuarios(Usuario solicitante, Usuario receptor);
}