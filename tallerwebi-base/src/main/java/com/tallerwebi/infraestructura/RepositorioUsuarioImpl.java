package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositoryUsuario")

public class RepositorioUsuarioImpl implements RepositorioUsuario {

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
    public void modificar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }

    @Override
    public void setearCarreraAUsuario(Usuario usuario, Carrera carrera) {
       usuario.setCarrera(carrera);

    }

    @Override
        public Usuario buscarPorId(long id) {
            return sessionFactory.getCurrentSession().get(Usuario.class, id);
    }
    }



