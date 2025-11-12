package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tallerwebi.dominio.SolicitudAmistad;
@Service
@Transactional
public class ServicioAmistadImpl implements ServicioAmistad {

    // Cambiado de RepositorioSolicitudAmistadImpl a la interfaz para inyección de dependencia
    private final RepositorioSolicitudAmistad repo;
    private final RepositorioAmistad repoAmistad;

    @Autowired
    public ServicioAmistadImpl(RepositorioSolicitudAmistad repo, RepositorioAmistad repoAmistad) {
        this.repo = repo;
        this.repoAmistad = repoAmistad;
    }
    @Override
    public Set<Long> obtenerIdsAmigosDe(long usuarioId) {
        // Se asume que el repositorio devuelve relaciones de amistad ya aceptadas
        return repoAmistad.obtenerAmistadesAceptadasDe(usuarioId)
                .stream()
                .map(a -> {
                    // cada Amistad debe exponer los dos participantes; devolvemos el id del otro
                    return a.getSolicitante().getId() == usuarioId ? a.getSolicitado().getId() : a.getSolicitante().getId();
                })
                .collect(Collectors.toSet());
    }
    @Override
    public List<Usuario> obtenerAmigosDeUsuario(long l) {
        return repoAmistad.obtenerAmigosDeUsuario(l);
    }

    // NUEVOS MÉTODOS para exponer la info necesaria al controlador (filtrado en home)
    @Override
    public List<Amistad> obtenerAmistadesAceptadasDe(long usuarioId) {
        return repoAmistad.obtenerAmistadesAceptadasDe(usuarioId);
    }

    @Override
    public boolean existeAmistadAceptadaEntre(long usuarioAId, long usuarioBId) {
        return repoAmistad.existeAmistadAceptadaEntre(usuarioAId, usuarioBId);
    }

    @Override
    public SolicitudAmistad enviarSolicitud(Usuario solicitante, Usuario receptor) {

        if (repoAmistad.sonAmigos(solicitante, receptor)) {
            throw new IllegalStateException("El usuario " + receptor.getNombre() + " ya es tu amigo.");
        }

        // SEGUNDA VERIFICACIÓN: ¿Hay solicitud PENDIENTE o ACEPTADA en curso?
        SolicitudAmistad activa = repo.buscarSolicitudActiva(solicitante, receptor);

        if (activa != null) {
            return activa;
        }

        // 3. Crear nueva solicitud
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
        // Esto debería llamar a un método en RepositorioAmistad (la tabla final) para un diseño limpio.
        // Pero siguiendo tu estructura actual, se deja aquí.
        return repo.buscarAmigos(usuario);
    }

    @Override
    public List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario) {

        return repo.listarSolicitudesPendientes(usuario);
    }

    @Override
    public List<Usuario> listarAmigos(long l) {
        return repoAmistad.obtenerAmigosDeUsuario(l);
    }

    @Override
    public SolicitudAmistad buscarSolicitudPendientePorUsuarios(Usuario usuario, Usuario emisor) {

        // 1. Llama al repositorio (que debería devolver List<SolicitudAmistad>)
        List<SolicitudAmistad> solicitudes = repo.buscarSolicitudPendientePorUsuarios(emisor, usuario); // Corregido: La variable de retorno del repo debe ser List<SolicitudAmistad>

        // 2. Verifica si la lista está vacía
        if (solicitudes.isEmpty()) {
            return null; // Devuelve null si no se encuentra
        }

        // 3. Devuelve el primer (y único) elemento
        return solicitudes.get(0);
    }
}