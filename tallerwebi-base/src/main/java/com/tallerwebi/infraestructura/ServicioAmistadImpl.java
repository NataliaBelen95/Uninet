package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.tallerwebi.dominio.SolicitudAmistad;
@Service
@Transactional
public class ServicioAmistadImpl implements ServicioAmistad {

    private final RepositorioSolicitudAmistad repo;
    private final RepositorioAmistad repoAmistad;

    @Autowired
    public ServicioAmistadImpl(RepositorioSolicitudAmistad repo, RepositorioAmistad repoAmistad) {
        this.repo = repo;
        this.repoAmistad = repoAmistad;
    }

    @Override
    public SolicitudAmistad enviarSolicitud(Usuario solicitante, Usuario receptor) {
        // 🛑 Bloqueo si ya existe una solicitud pendiente o ya son amigos
        SolicitudAmistad activa = repo.buscarSolicitudActiva(solicitante, receptor);

        if (activa != null) {
            // Devuelve la solicitud existente para evitar la duplicación en la DB
            // Esto también asegura que los botones de 'amigo' y 'solicitud enviada' se muestren correctamente.
            return activa;
        }

        // Si no existe, crea una nueva solicitud.
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setSolicitante(solicitante);
        solicitud.setReceptor(receptor);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        repo.guardar(solicitud);

        return solicitud;
    }

    @Override
    public boolean aceptarSolicitud(Long idSolicitud) {
        SolicitudAmistad solicitud = repo.buscarPorId(idSolicitud);

        if (solicitud == null || solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            return false;
        }

        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        repo.actualizar(solicitud);

        // Crear la entidad Amistad
        Amistad amistad = new Amistad(solicitud.getSolicitante(), solicitud.getReceptor());
        repoAmistad.guardar(amistad);

        return true;
    }

    @Override
    public void rechazarSolicitud(Long idSolicitud) {
        SolicitudAmistad solicitud = repo.buscarPorId(idSolicitud);
        if (solicitud == null) return;
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        repo.actualizar(solicitud);
    }

    @Override
    public List<Usuario> listarAmigos(Usuario usuario) {
        return repo.buscarAmigos(usuario);
    }

    @Override
    public List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario) {
        return repo.buscarPendientes(usuario);
    }

    @Override
    public List<Usuario> obtenerAmigosDeUsuario(long l) {
        return repoAmistad.obtenerAmigosDeUsuario(l);
    }

    @Override
    public SolicitudAmistad buscarSolicitudPendientePorUsuarios(Usuario usuario, Usuario emisor) {
        // Renombramos los parámetros para reflejar la lógica del repositorio:
        // Buscamos la solicitud donde 'emisor' (quien envió) es el solicitante
        // y 'usuario' (el logueado) es el receptor.
        List<SolicitudAmistad> solicitudes = repo.buscarSolicitudPendientePorUsuarios(emisor, usuario);

        if (solicitudes.isEmpty()) {
            return null;
        }
        // Devolvemos el primer (y único esperado) resultado.
        return solicitudes.get(0);
    }
}