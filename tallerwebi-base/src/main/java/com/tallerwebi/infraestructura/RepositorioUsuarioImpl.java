package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("repositoryUsuario")

public class RepositorioUsuarioImpl implements RepositorioUsuario {
    @Override
    public List<Usuario> findAll() {
        return List.of();
    }

    @Override
    public List<Publicacion> obtenerPublicacionesDeUsuario(long usuId) {
        return List.of();
    }

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Usuario buscarUsuario(String email, String password) {

        Session session = sessionFactory.getCurrentSession();

        Usuario usuario = (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .setMaxResults(1) // asegura que solo devuelva 1
                .add(Restrictions.eq("password", password))
                .uniqueResult();

        if (usuario != null && usuario.getPassword().equals(password)) {
            return usuario;
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        return (Usuario) sessionFactory.getCurrentSession().createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public void actualizar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }

    @Override
    public void setearCarreraAUsuario(Usuario usuario, Carrera carrera) {
       usuario.setCarrera(carrera);

    }

    @Override
    @Transactional
    public Usuario buscarPorId(long id) {
            return sessionFactory.getCurrentSession().get(Usuario.class, id);
    }

    public List<Usuario> buscarTodos() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Usuario", Usuario.class)
                .list();
    }

    @Override
    public Usuario findByIdWithPublicaciones(long id) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT u FROM Usuario u " +
                        "LEFT JOIN FETCH u.publicaciones p " +  // Cargar publicaciones del usuario
                        "WHERE u.id = :id", Usuario.class)    // Filtrar por el id del usuario
                .setParameter("id", id)
                .uniqueResult();
    }


    @Override
    public void actualizarContrasena(Usuario usuario, String nuevaContrasena) {
        Session session = sessionFactory.getCurrentSession();

        usuario.setPassword(nuevaContrasena);

        session.update(usuario);
    }

    @Override
    public Usuario buscarPorSlug(String slug) {
        Session session = sessionFactory.getCurrentSession();
        // Query para traer un único usuario por slug
        return session.createQuery("FROM Usuario u WHERE u.slug = :slug", Usuario.class)
                .setParameter("slug", slug)
                .uniqueResult();
    }


}



