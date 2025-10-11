package com.tallerwebi.dominio;
import java.util.List;

public interface RepositorioGenero {

    List<Genero> listarGeneros();
    Genero buscarGeneroPorNombre(String nombre);
    void guardarGenero(Genero genero);
    Genero buscarGeneroPorId(Long id);
}
