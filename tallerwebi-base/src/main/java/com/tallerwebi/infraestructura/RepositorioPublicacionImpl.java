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
                                "LEFT JOIN FETCH p.archivo a " +   // Cambio aquí: p.archivo en lugar de p.archivos
                                "WHERE p.archivo IS NOT NULL", Publicacion.class) // Verificamos que haya archivo
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
        Publicacion publicacion = sessionFactory.getCurrentSession()
                .createQuery("SELECT p FROM Publicacion p " +
                        "LEFT JOIN FETCH p.usuario " + // esto está bien
                        "WHERE p.id = :id", Publicacion.class)
                .setParameter("id", id)
                .uniqueResult();

        // Inicializás manualmente las colecciones para evitar LazyInitializationException
        Hibernate.initialize(publicacion.getComentarios());
        Hibernate.initialize(publicacion.getLikesDePublicacion());

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
}




