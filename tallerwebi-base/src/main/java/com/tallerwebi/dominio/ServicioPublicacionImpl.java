package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
@Service("servicioPublicado")
@Transactional
public class ServicioPublicacionImpl implements ServicioPublicacion {

    private final RepositorioPublicacion repositorio;

    @Autowired
    public ServicioPublicacionImpl(RepositorioPublicacion repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void realizar(Publicacion publicacion) throws PublicacionFallida {

        if (publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty()) {
            throw new PublicacionFallida("La publicación no puede estar vacía");
        }

        if (publicacion.getDescripcion().length() > 200) {
            throw new PublicacionFallida("Pasaste los 200 caracteres disponibles");
        }

        if (repositorio.existeIgual(publicacion)) {
            throw new PublicacionFallida("Ya existe una publicación igual");
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

    public List<Publicacion> findByUsuarioId(Long id) {
        return repositorio.findByUsuarioId(id);
    }
}
