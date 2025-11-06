package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioPublicacion;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class RepositorioPublicacionImpl implements RepositorioPublicacion {

    @Autowired
    private SessionFactory sessionFactory;

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
    public boolean existeIgual(Publicacion publicacion) {

        String hql = "FROM Publicacion WHERE descripcion = :descripcion AND usuario = :usuario";
        Publicacion resultado = sessionFactory.getCurrentSession()
                .createQuery(hql, Publicacion.class)
                .setParameter("descripcion", publicacion.getDescripcion())
                .setParameter("usuario", publicacion.getUsuario())
                .uniqueResult();

        return resultado != null;
    }

    public List<Publicacion> findByUsuarioId(Long id) {
       Usuario usuario = sessionFactory.getCurrentSession().get(Usuario.class, id);
       return usuario.getPublicaciones();
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
     //ver si existe por hash Para que no se repita E impida Crear Una publi nueva
    @Override
    public boolean existeHashResumen(String hash, Long usuarioId) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(p) FROM Publicacion p WHERE p.hashResumen = :hash AND p.usuario.id = :usuarioId", Long.class)
                .setParameter("hash", hash)
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();

        return count != null && count > 0;
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
                        "SELECT p FROM Publicacion p WHERE p.usuario.id = :usuarioId",
                        Publicacion.class
                )
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
//    @Override
//    public List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario) {
//        return sessionFactory.getCurrentSession()
//                .createQuery(
//                        "SELECT DISTINCT p FROM Publicacion p " +
//                                "LEFT JOIN FETCH p.usuario u " +
//                                "LEFT JOIN FETCH p.comentarios " +
//                                "LEFT JOIN FETCH p.archivo " +
//                                "LEFT JOIN FETCH p.likesDePublicacion " +
//                                "WHERE u.esBot = true " +
//                                "ORDER BY p.fechaPublicacion DESC",
//                        Publicacion.class
//                )
//                .getResultList();
//    }

    @Override
    public List<Publicacion> obtenerPublicacionesDirigidasA(Usuario usuario) {
        Long usuarioId = usuario.getId();

        // 🔑 CORRECCIÓN: SOLO obtener anuncios dirigidos al usuario
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

//    @Override
//    public List<Publicacion> obtenerPublicacionesOrganicas() {
//        return List.of();
//    }
}




