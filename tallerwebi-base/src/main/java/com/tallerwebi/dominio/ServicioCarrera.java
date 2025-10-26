package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicioCarrera {

    private final RepositorioCarrera repositorioCarrera;

    @Autowired
    public ServicioCarrera(RepositorioCarrera repositorioCarrera) {
        this.repositorioCarrera = repositorioCarrera;
    }

    public List<Carrera> buscarTodas() {
        return repositorioCarrera.buscarTodas();
    }

    public List<Carrera> buscarPorDepartamento(Integer departamentoId) {
        return repositorioCarrera.buscarPorDepartamento(departamentoId);
    }

}