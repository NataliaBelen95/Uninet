package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioAmistad {
    SolicitudAmistad enviarSolicitud(Usuario solicitante, Usuario receptor);

    boolean aceptarSolicitud(Long idSolicitud);
    void rechazarSolicitud(Long idSolicitud);
    List<Usuario> listarAmigos(Usuario usuario);
    List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario);
    List<Usuario> obtenerAmigosDeUsuario(long l);
    List <SolicitudAmistad> buscarSolicitudPendientePorUsuarios (Usuario usuario, Usuario emisor);


}