package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Like;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioLike;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioLikeImpl implements RepositorioLike {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean existePorUsuarioYPublicacion(long usuId, long publiId) {
        String hql = "SELECT COUNT(l) FROM Like l WHERE l.usuario.id = :usuarioId AND l.publicacion.id = :publicacionId";
        Long count = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("usuarioId", usuId)
                .setParameter("publicacionId", publiId)
                .uniqueResult();
        //debug
        if (count == null) {
            System.err.println("⚠️ Count is null for usuarioId: " + usuId + " and publicacionId: " + publiId);
        } else {
            System.out.println("Likes count: " + count);
        }

        return count != null && count > 0;
    }


    public int contarPorPublicacion(long publicacionId) {
        String hql = "SELECT COUNT(l) FROM Like l WHERE l.publicacion.id = :publicacionId";
        Long count = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("publicacionId", publicacionId)
                .uniqueResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Like guardar(Like like) {
        sessionFactory.getCurrentSession().save(like);
        return like;
    }


    /*MIRAR ESTA FUNCION*/
    @Override
    public void eliminar(long id) {
        Session session = sessionFactory.getCurrentSession();
        Like like = session.get(Like.class, id);

        if (like != null) {
            Publicacion publicacion = like.getPublicacion();
            if (publicacion != null) {
                publicacion.getLikesDePublicacion().remove(like);  // Quitar de la colección
                like.setPublicacion(null);            // Romper la relación para evitar problemas
            }
            session.delete(like);
        } else {
            System.out.println("Like con id " + id + " no encontrado.");
        }
    }

    @Override
    public Like buscarPorId(long id) {
        return sessionFactory.getCurrentSession().get(Like.class, id);
    }

    @Override
    public Like buscarPorUsuarioYPublicacion(long usuId, long publiId) {
        String hql = "FROM Like l WHERE l.usuario.id = :usuarioId AND l.publicacion.id = :publicacionId";
        List<Like> resultado = sessionFactory.getCurrentSession()
                .createQuery(hql, Like.class)
                .setParameter("usuarioId", usuId)
                .setParameter("publicacionId", publiId)
                .getResultList();

        return resultado.isEmpty() ? null : resultado.get(0);
    }
}