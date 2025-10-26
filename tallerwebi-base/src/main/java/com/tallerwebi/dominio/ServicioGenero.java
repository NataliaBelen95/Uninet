package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioGenero {

    private final RepositorioGenero repositorioGenero;

    @Autowired
    public ServicioGenero(RepositorioGenero repositorioGenero) {
        this.repositorioGenero = repositorioGenero;
    }

    public List<Genero> listarGeneros() {
        return repositorioGenero.listarGeneros();
    }

    public void crearSiNoExiste(String nombre) {
        if(repositorioGenero.buscarGeneroPorNombre(nombre) == null){
            repositorioGenero.guardarGenero(new Genero(nombre));
        }
    }

    public Genero buscarPorId(Long id) {
        return repositorioGenero.buscarGeneroPorId(id);
    }
}