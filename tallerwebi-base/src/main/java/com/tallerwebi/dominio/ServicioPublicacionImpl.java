package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service("servicioPublicado")
@Transactional
public class ServicioPublicacionImpl implements ServicioPublicacion {

    private final RepositorioPublicacion repositorio;
    private final RepositorioComentario repositorioComentario;
    private final RepositorioLike  repositorioLike;

    @Autowired
    public ServicioPublicacionImpl(RepositorioPublicacion repositorio, RepositorioComentario repositorioComentario, RepositorioLike repositorioLike) {
        this.repositorio = repositorio;
        this.repositorioComentario = repositorioComentario;
        this.repositorioLike = repositorioLike;
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
        if (publicacion.getFechaPublicacion() == null) {
            publicacion.setFechaPublicacion(LocalDateTime.now());
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

    @Override
    public void eliminarPublicacionEntera(Publicacion publicacion) {
        if(publicacion !=null) {

            repositorio.eliminarPubli(publicacion);
        } else {
            throw  new NoSeEncuentraPublicacion();
        }
    }

    public List<Publicacion> findByUsuarioId(Long id) {
        return repositorio.findByUsuarioId(id);
    }


}
