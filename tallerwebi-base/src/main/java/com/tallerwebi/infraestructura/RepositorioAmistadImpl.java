package com.tallerwebi.infraestructura;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.tallerwebi.dominio.*;
import org.hibernate.query.Query;


@Repository
public class RepositorioAmistadImpl implements RepositorioAmistad {

    @Override
    public List<Usuario> listarAmigosPorUsuario(long usuarioId) {
        String hql = "SELECT a.solicitado FROM Amistad a WHERE a.solicitante.id = :id";
        List<Usuario> resultado = sessionFactory.getCurrentSession()
                .createQuery(hql, Usuario.class)
                .setParameter("id", usuarioId)
                .getResultList();
        return resultado;
    }

    @Override
    public List<Usuario> obtenerAmigosDeUsuario(long id) {
        List<Usuario> amigosComoSolicitante = sessionFactory.getCurrentSession()
                .createQuery("SELECT sa.receptor FROM SolicitudAmistad sa WHERE sa.solicitante.id = :id AND sa.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("id", id)
                .getResultList();

        List<Usuario> amigosComoDestinatario = sessionFactory.getCurrentSession()
                .createQuery("SELECT sa.solicitante FROM SolicitudAmistad sa WHERE sa.receptor.id = :id AND sa.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("id", id)
                .getResultList();

        Set<Usuario> unidos = new LinkedHashSet<>();
        if (amigosComoSolicitante != null) unidos.addAll(amigosComoSolicitante);
        if (amigosComoDestinatario != null) unidos.addAll(amigosComoDestinatario);

        return new ArrayList<>(unidos);
    }

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioAmistadImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Amistad amistad) {
        sessionFactory.getCurrentSession().save(amistad);
    }

    @Override
    public Amistad buscarPorUsuarios(Usuario solicitante, Usuario solicitado) {
        Session session = sessionFactory.getCurrentSession();
        Query<Amistad> query = session.createQuery(
                "FROM Amistad WHERE (solicitante = :solicitante AND solicitado = :solicitado) OR (solicitante = :solicitado AND solicitado = :solicitante)",
                Amistad.class);
        query.setParameter("solicitante", solicitante);
        query.setParameter("solicitado", solicitado);
        return query.uniqueResult();
    }

    @Override
    public List<Amistad> listarSolicitudesPendientes(Usuario usuario) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Amistad WHERE solicitado = :usuario AND estado = 'PENDIENTE'", Amistad.class)
                .setParameter("usuario", usuario)
                .list();
    }

    @Override
    public List<Amistad> listarAmigos(Usuario usuario) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Amistad WHERE (solicitante = :usuario OR solicitado = :usuario) AND estado = 'ACEPTADA'", Amistad.class)
                .setParameter("usuario", usuario)
                .list();
    }

    @Override
    public void actualizar(Amistad amistad) {
        sessionFactory.getCurrentSession().update(amistad);
    }

    // RepositorioAmistadImpl.java

    @Override
    public boolean sonAmigos(Usuario u1, Usuario u2) {
        // Es crucial que la transacción ya esté abierta (gracias a @Transactional en el servicio)
        final Session session = sessionFactory.getCurrentSession();

        String hql = "SELECT 1 FROM Amistad a WHERE " +
                // Opción 1: u1 es solicitante y u2 es solicitado
                " (a.solicitante = :usuario1 AND a.solicitado = :usuario2) " +
                " OR " +
                // Opción 2: u2 es solicitante y u1 es solicitado
                " (a.solicitante = :usuario2 AND a.solicitado = :usuario1)";

        // Usamos Query<Long> o Query<Integer> si solo queremos verificar existencia (SELECT 1)
        Query<Integer> query = session.createQuery(hql, Integer.class);

        query.setParameter("usuario1", u1);
        query.setParameter("usuario2", u2);

        // Solo necesitamos verificar si existe un resultado, no cargar la entidad completa.
        query.setMaxResults(1);

        // Si getSingleResult() encuentra una fila, devuelve el 1. Si no encuentra nada, lanza NoResultException.
        // Usamos list() para evitar NoResultException y es más limpio para booleans.
        return !query.list().isEmpty();
    }
}
