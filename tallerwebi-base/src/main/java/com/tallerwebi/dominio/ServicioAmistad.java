package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioAmistad {
    void enviarSolicitud(Usuario solicitante, Usuario receptor);
    void aceptarSolicitud(Long idSolicitud);
    void rechazarSolicitud(Long idSolicitud);
    List<Usuario> listarAmigos(Usuario usuario);
    List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario);

    List<Usuario> obtenerAmigosDeUsuario(long l);
}
