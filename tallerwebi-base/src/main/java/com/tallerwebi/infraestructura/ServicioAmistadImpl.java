package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setSolicitante(solicitante);
        solicitud.setReceptor(receptor);
        repo.guardar(solicitud);
        // si repo.guardar usa session.save(solicitud) con IDENTITY, solicitud.getId() ya estará poblado
        return solicitud;
    }

    @Override
    public void aceptarSolicitud(Long idSolicitud) {
        SolicitudAmistad solicitud = repo.buscarPorId(idSolicitud);
        if (solicitud == null) return;
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        repo.actualizar(solicitud);

        // crear entidad de amistad si tu modelo la requiere
        Amistad amistad = new Amistad();
        amistad.setSolicitante(solicitud.getSolicitante());
        amistad.setSolicitado(solicitud.getReceptor());
        repoAmistad.guardar(amistad);
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
}