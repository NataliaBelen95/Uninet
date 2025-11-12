package com.tallerwebi.dominio;

import java.util.List;
import java.util.Set;

public interface ServicioAmistad {
    SolicitudAmistad enviarSolicitud(Usuario solicitante, Usuario receptor);

    boolean aceptarSolicitud(Long idSolicitud);
    void rechazarSolicitud(Long idSolicitud);
    List<Usuario> listarAmigos(Usuario usuario);
    List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario);
    List<Usuario> listarAmigos(long l);
    // Devuelve el set de ids de usuarios que son amigos (relación aceptada) del usuario dado

    List<Usuario> obtenerAmigosDeUsuario(long l);

    // NUEVOS MÉTODOS añadidos para soportar el filtrado en /home
    List<Amistad> obtenerAmistadesAceptadasDe(long usuarioId);

    boolean existeAmistadAceptadaEntre(long usuarioAId, long usuarioBId);

    Set<Long> obtenerIdsAmigosDe(long usuarioId);


    // 🛑 CAMBIO CLAVE: De List<SolicitudAmistad> a SolicitudAmistad
    SolicitudAmistad buscarSolicitudPendientePorUsuarios (Usuario solicitante, Usuario receptor);
}