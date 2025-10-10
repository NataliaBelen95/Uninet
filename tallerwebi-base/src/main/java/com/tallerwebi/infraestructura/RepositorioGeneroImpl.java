package com.tallerwebi.infraestructura;

import org.hibernate.SessionFactory;
import com.tallerwebi.dominio.Genero;
import com.tallerwebi.dominio.RepositorioGenero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RepositorioGeneroImpl implements RepositorioGenero {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioGeneroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Genero> listarGeneros() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Genero", Genero.class)
                .list();
    }

    @Override
    public Genero buscarGeneroPorNombre(String nombre) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Genero WHERE nombre = :nombre", Genero.class)
                .setParameter("nombre", nombre)
                .uniqueResult();
    }

    @Override
    public void guardarGenero(Genero genero) {
        sessionFactory.getCurrentSession().save(genero);
    }
}
