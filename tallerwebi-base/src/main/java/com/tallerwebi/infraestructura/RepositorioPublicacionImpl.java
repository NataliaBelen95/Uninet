package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioPublicacion;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class RepositorioPublicacionImpl implements RepositorioPublicacion {

    private final SessionFactory sessionFactory;
    public RepositorioPublicacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Publicacion publicacion) {
        sessionFactory.getCurrentSession().save(publicacion);
    }

    @Override
    public Publicacion buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Publicacion.class, id);
    }

    @Override
    public List<Publicacion> listarTodas() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT p FROM Publicacion p " +
                                "LEFT JOIN FETCH p.usuario " +
                                "LEFT JOIN FETCH p.comentarios " +
                                "LEFT JOIN FETCH p.archivo", Publicacion.class)
                .getResultList();
    }

    @Override
    public List<Publicacion> listarNoPublicitarias() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT p FROM Publicacion p " +
                                "LEFT JOIN FETCH p.usuario " +
                                "LEFT JOIN FETCH p.comentarios " +
                                "LEFT JOIN FETCH p.archivo " +

                                "WHERE p.esPublicidad = false",
                        Publicacion.class)
                .getResultList();
    }

    public void eliminarPubli(Publicacion publicacion) {
        sessionFactory.getCurrentSession().delete(publicacion);
    }


    @Override
    public Publicacion obtenerPublicacionCompleta(long id) {
        // 🔑 CORRECCIÓN: Usar session.get() para asegurar la carga de TODAS las columnas simples.
        Publicacion publicacion = sessionFactory.getCurrentSession().get(Publicacion.class, id);

        // Cargamos manualmente las colecciones LAZY si las necesitamos
        if (publicacion != null) {
            // Inicializar las colecciones que son LAZY
            Hibernate.initialize(publicacion.getComentarios());
            Hibernate.initialize(publicacion.getLikesDePublicacion()); // Aunque esta es EAGER en tu entidad, es buena práctica si la cambias.
        }

        return publicacion;
    }

    @Override
    public List<Publicacion> obtenerPublicacionesConLikeDeUsuario(Long usuarioId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT p FROM Publicacion p " +
                                "LEFT JOIN FETCH p.usuario " +
                                "LEFT JOIN FETCH p.archivo " +
                                "LEFT JOIN FETCH p.comentarios " +
                                "JOIN p.likesDePublicacion l " +
                                "WHERE l.usuario.id = :usuarioId", Publicacion.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
    @Override
    public List<Publicacion> obtenerPublicacionesDeUsuario(Long usuarioId) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT p FROM Publicacion p " +
                                "LEFT JOIN FETCH p.usuario " +       // Carga el usuario de la publicación
                                "LEFT JOIN FETCH p.comentarios " +   // Carga los comentarios
                                "LEFT JOIN FETCH p.archivo " +       // Carga el archivo adjunto
                                "WHERE p.usuario.id = :usuarioId", Publicacion.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }


    @Override
    public List<Publicacion> obtenerPublicacionesDirigidasA(Usuario usuario) {
        Long usuarioId = usuario.getId();

        // CORRECCIÓN: SOLO obtener anuncios dirigidos al usuario
        return sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT p FROM Publicacion p " +
                                "LEFT JOIN FETCH p.usuario u " +
                                "LEFT JOIN FETCH p.comentarios " +
                                "LEFT JOIN FETCH p.likesDePublicacion " +
                                "LEFT JOIN FETCH p.archivo " +
                                //  SOLO ANUNCIOS DIRIGIDOS A ESTE USUARIO
                                "WHERE u.esBot = true AND p.usuarioDestinatarioId = :usuarioId " +
                                "ORDER BY p.fechaPublicacion DESC",
                        Publicacion.class
                )
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
    @Override
    public List<Publicacion> obtenerPublicacionesDeIdsDeUsuario(List<Long> idsAmigosYPropios) {
        if (idsAmigosYPropios == null || idsAmigosYPropios.isEmpty()) {
            return List.of();
        }

        Session session = sessionFactory.getCurrentSession();


        String hql = "SELECT DISTINCT p FROM Publicacion p " +
                "LEFT JOIN FETCH p.usuario u " +
                "LEFT JOIN FETCH p.comentarios c " +
                "LEFT JOIN FETCH p.likesDePublicacion l " +
                "LEFT JOIN FETCH p.archivo a " +
                "WHERE p.usuario.id IN (:ids) AND p.esPublicidad = false " +
                "ORDER BY p.fechaPublicacion DESC";

        Query<Publicacion> query = session.createQuery(hql, Publicacion.class);

        query.setParameterList("ids", idsAmigosYPropios);

        return query.getResultList();
    }




}




