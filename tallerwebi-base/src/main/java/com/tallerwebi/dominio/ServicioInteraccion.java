package com.tallerwebi.dominio;

import com.tallerwebi.dominio.RepositorioInteraccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicioInteraccion {

    private final RepositorioInteraccion repositorioInteraccion;

    public ServicioInteraccion(RepositorioInteraccion repositorioInteraccion) {
        this.repositorioInteraccion = repositorioInteraccion;
    }

    public void guardarInteraccion(Interaccion interaccion) {
        repositorioInteraccion.guardar(interaccion);
    }

    public List<Interaccion> obtenerInteraccionesDeUsuario(Usuario usuario) {
        return repositorioInteraccion.encontrarDeUsuario(usuario);
    }

    public List<Interaccion> obtenerInteraccionesDePublicacion(Publicacion publicacion) {
        return repositorioInteraccion.encontrarDePubli(publicacion);
    }

    public void eliminarInteraccion(long id) {
        repositorioInteraccion.eliminar(id);
    }
}