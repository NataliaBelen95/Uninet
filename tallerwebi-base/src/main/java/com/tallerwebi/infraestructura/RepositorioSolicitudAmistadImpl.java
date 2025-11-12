package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioSolicitudAmistad;
import com.tallerwebi.dominio.SolicitudAmistad;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RepositorioSolicitudAmistadImpl implements RepositorioSolicitudAmistad {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioSolicitudAmistadImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(SolicitudAmistad solicitud) {
        sessionFactory.getCurrentSession().save(solicitud);
    }

    @Override
    public SolicitudAmistad buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(SolicitudAmistad.class, id);
    }

    @Override
    public void actualizar(SolicitudAmistad solicitud) {
        sessionFactory.getCurrentSession().update(solicitud);
    }

    @Override
    public List<SolicitudAmistad> listarSolicitudesPendientes(Usuario usuario) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM SolicitudAmistad WHERE receptor = :usuario AND estado = 'PENDIENTE'", SolicitudAmistad.class)
                .setParameter("usuario", usuario)
                .list();
    }

    @Override
    public SolicitudAmistad buscarSolicitudActiva(Usuario u1, Usuario u2) {
        // Busca si hay alguna solicitud PENDIENTE o ACEPTADA entre los dos usuarios (para bloqueo)
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM SolicitudAmistad s WHERE " +
                "((s.solicitante = :u1 AND s.receptor = :u2) OR " +
                " (s.solicitante = :u2 AND s.receptor = :u1)) AND " +
                "s.estado IN ('PENDIENTE', 'ACEPTADA')";

        Query<SolicitudAmistad> query = session.createQuery(hql, SolicitudAmistad.class);
        query.setParameter("u1", u1);
        query.setParameter("u2", u2);
        query.setMaxResults(1);

        return query.uniqueResult(); // Devuelve una Solicitud o null
    }

    @Override
    public List<Usuario> buscarAmigos(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();

        List<Usuario> amigosComoSolicitante = session.createQuery(
                        "select s.receptor from SolicitudAmistad s " +
                                "where s.solicitante = :usuario and s.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("usuario", usuario)
                .getResultList();

        List<Usuario> amigosComoReceptor = session.createQuery(
                        "select s.solicitante from SolicitudAmistad s " +
                                "where s.receptor = :usuario and s.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("usuario", usuario)
                .getResultList();

        // Combina las listas sin duplicados
        Set<Usuario> unidos = new LinkedHashSet<>();
        if (amigosComoSolicitante != null) unidos.addAll(amigosComoSolicitante);
        if (amigosComoReceptor != null) unidos.addAll(amigosComoReceptor);

        return new ArrayList<>(unidos);
    }

    @Override
    public List<SolicitudAmistad> buscarSolicitudPendientePorUsuarios(Usuario solicitante, Usuario receptor) {
        final Session session = sessionFactory.getCurrentSession();

        // El HQL busca la solicitud PENDIENTE específica (bidireccional)
        String hql = "FROM SolicitudAmistad s WHERE " +
                "((s.solicitante = :solicitante AND s.receptor = :receptor) OR " +
                " (s.solicitante = :receptor AND s.receptor = :solicitante)) AND " +
                "s.estado = 'PENDIENTE'";

        Query<SolicitudAmistad> query = session.createQuery(hql, SolicitudAmistad.class);
        query.setParameter("solicitante", solicitante);
        query.setParameter("receptor", receptor);
        query.setMaxResults(1);

        // Devuelve el primer resultado o null
        List<SolicitudAmistad> results = query.list();
        return results.isEmpty() ? null : Collections.singletonList(results.get(0));
    }
}