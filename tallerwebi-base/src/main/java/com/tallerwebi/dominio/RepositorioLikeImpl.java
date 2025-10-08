package com.tallerwebi.dominio;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioLikeImpl implements RepositorioLike {


    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public boolean existePorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion) {
        String hql = "SELECT COUNT(l) FROM Like l WHERE l.usuario = :usuario AND l.publicacion = :publicacion";
        Long count = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("usuario", usuario)
                .setParameter("publicacion", publicacion)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Like encontrarPorUsuarioYPublicacion(Usuario usuario, Publicacion publicacion) {
        String hql = "FROM Like l WHERE l.usuario = :usuario AND l.publicacion = :publicacion";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Like.class)
                .setParameter("usuario", usuario)
                .setParameter("publicacion", publicacion)
                .uniqueResult();
    }

    @Override
    public int contarPorPublicacion(Publicacion publicacion) {
        String hql = "SELECT COUNT(l) FROM Like l WHERE l.publicacion = :publicacion";
        Long count = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("publicacion", publicacion)
                .uniqueResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Like guardar(Like like) {
        sessionFactory.getCurrentSession().save(like);
        return like;
    }

    @Override
    public Like eliminar(Like like) {
        sessionFactory.getCurrentSession().delete(like);
        return like;
    }
}

