package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.departamento.Departamento;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioDepartamentoImpl implements RepositorioDepartamento {
    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioDepartamentoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Departamento> obtenerDepartamentos() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Departamento",Departamento.class)
                .getResultList();
    }
}
