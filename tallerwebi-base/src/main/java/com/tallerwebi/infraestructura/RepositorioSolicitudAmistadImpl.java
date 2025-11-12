package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import com.tallerwebi.dominio.SolicitudAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.RepositorioSolicitudAmistad;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public List<SolicitudAmistad> buscarPendientes(Usuario usuario) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM SolicitudAmistad WHERE receptor = :usuario AND estado = 'PENDIENTE'", SolicitudAmistad.class)
                .setParameter("usuario", usuario)
                .list();
    }


    @Override
    public List<Usuario> buscarAmigos(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();

        List<Usuario> amigosComoSolicitante = session.createQuery(
                        "select s.receptor from SolicitudAmistad s " +
                                "where s.solicitante = :usuario and s.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("usuario", usuario)
                .list();

        List<Usuario> amigosComoReceptor = session.createQuery(
                        "select s.solicitante from SolicitudAmistad s " +
                                "where s.receptor = :usuario and s.estado = 'ACEPTADA'", Usuario.class)
                .setParameter("usuario", usuario)
                .list();

        // Merge sin duplicados usando id (por si equals/hashCode no están sobreescritos)
        Map<Long, Usuario> mapa = new LinkedHashMap<>();


        return new ArrayList<>(mapa.values());
    }


    @Override
    public void actualizar(SolicitudAmistad solicitud) {
        sessionFactory.getCurrentSession().update(solicitud);
    }

    @Override
    public List<SolicitudAmistad> buscarSolicitudPendientePorUsuarios(Usuario solicitante, Usuario receptor) {
        final Session session = sessionFactory.getCurrentSession();

        // HQL para buscar la solicitud específica:
        // Solicitante = primer usuario, Receptor = segundo usuario, Estado = PENDIENTE
        String hql = "FROM SolicitudAmistad s " +
                "WHERE s.solicitante = :solicitante " +
                "AND s.receptor = :receptor " +
                "AND s.estado = 'PENDIENTE'"; // Usamos 'PENDIENTE' como String o como referencia a la enum

        Query<SolicitudAmistad> query = session.createQuery(hql, SolicitudAmistad.class);
        query.setParameter("solicitante", solicitante);
        query.setParameter("receptor", receptor);
        // Si tu campo 'estado' en SolicitudAmistad es un Enum, podría ser:
        // query.setParameter("estado", EstadoSolicitud.PENDIENTE);
        // Pero asumiendo que es un String, la consulta HQL de arriba es suficiente.

        return query.list();
    }

    @Override
    public SolicitudAmistad buscarSolicitudActiva(Usuario u1, Usuario u2) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "FROM SolicitudAmistad s WHERE " +
                // Caso A->B o B->A
                "((s.solicitante = :u1 AND s.receptor = :u2) OR " +
                " (s.solicitante = :u2 AND s.receptor = :u1)) AND " +
                // Solo buscamos las que están en curso o ya completadas
                "s.estado IN ('PENDIENTE', 'ACEPTADA')";

        Query<SolicitudAmistad> query = session.createQuery(hql, SolicitudAmistad.class);
        query.setParameter("u1", u1);
        query.setParameter("u2", u2);
        query.setMaxResults(1);

        return query.uniqueResult(); // Devuelve una Solicitud o null
    }

}
