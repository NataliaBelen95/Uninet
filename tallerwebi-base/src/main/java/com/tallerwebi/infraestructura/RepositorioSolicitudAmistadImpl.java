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


}
