package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
@Service("servicioPublicado")
@Transactional
public class ServicioPublicadoImpl implements ServicioPublicado {

    private final RepositorioPublicacion repositorio;

    @Autowired
    public ServicioPublicadoImpl(RepositorioPublicacion repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void realizar(Publicacion publicacion) throws PublicacionFallida {
        if (repositorio.existeIgual(publicacion)) {
            throw new PublicacionFallida();
        }
        repositorio.guardar(publicacion);
    }

    @Override
    public Publicacion obtenerPublicacionPorId(long id) {
        return repositorio.buscarPorId(id);
    }

    @Override
    public List<Publicacion> findAll() {
        return repositorio.listarTodas();
    }

    @Override
    public int obtenerCantidadDeLikes(long id) {
        Publicacion p = obtenerPublicacionPorId(id);
        return p.getLikes();
    }
}
