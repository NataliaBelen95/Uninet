package com.tallerwebi.infraestructura;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.tallerwebi.dominio.*;
import org.hibernate.query.Query;


@Repository
public class RepositorioAmistadImpl implements RepositorioAmistad {

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
}
