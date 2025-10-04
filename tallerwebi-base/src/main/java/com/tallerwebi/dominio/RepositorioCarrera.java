package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;


public interface RepositorioCarrera {
    List <Carrera> buscarTodas();
    void agregarMateria(long id, Materia materia);
    Carrera buscarPorId(long id);
    void guardar(Carrera carrera);

}