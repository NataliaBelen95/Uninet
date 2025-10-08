package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
                .createQuery("SELECT DISTINCT p FROM Publicacion p LEFT JOIN FETCH p.usuario LEFT JOIN FETCH p.comentarios", Publicacion.class)
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
}




