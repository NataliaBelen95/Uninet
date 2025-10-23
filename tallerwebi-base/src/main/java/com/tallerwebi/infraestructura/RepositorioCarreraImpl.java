package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.RepositorioCarrera;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class RepositorioCarreraImpl implements RepositorioCarrera {
    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioCarreraImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

    }

    @Override
    public void guardar(Carrera carrera) {
        sessionFactory.getCurrentSession().saveOrUpdate(carrera);
    }

    @Override
    public Carrera buscarPorId(long id) {
        return null;
    }


    public List<Carrera> buscarTodas() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Carrera", Carrera.class)
                .getResultList();
    }

    @Override
    public void agregarMateria(long id, Materia materia) {
        Carrera carrera = buscarPorId(id);
        if (carrera == null) {
            throw new RuntimeException("Carrera no encontrada");
        }

        carrera.getMaterias().add(materia);
        sessionFactory.getCurrentSession().saveOrUpdate(materia);
        guardar(carrera);

    }
}