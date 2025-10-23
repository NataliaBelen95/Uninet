package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionNoEncontrada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@Transactional
public class ServicioLikeImpl implements ServicioLike {
    private final RepositorioLike repositorioLike;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPublicacion repositorioPublicacion;

    @Autowired
    public ServicioLikeImpl(RepositorioLike repositorioLike,RepositorioUsuario repositorioUsuario,RepositorioPublicacion repositorioPublicacion) {
        this.repositorioLike = repositorioLike;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPublicacion = repositorioPublicacion;

    }


    @Override
    public void darLike(long usuId, long publiId) {
        Like like = new Like();

        // Buscar el usuario y la publicación por ID
        Usuario usuario = repositorioUsuario.buscarPorId(usuId); // Busca al usuario por ID
        Publicacion publicacion = repositorioPublicacion.buscarPorId(publiId); // Busca la publicación por ID

        // Asignar los objetos completos a Like
        like.setUsuario(usuario); // Asignar el objeto Usuario
        like.setPublicacion(publicacion); // Asignar el objeto Publicacion
        like.setFecha(LocalDateTime.now()); // Asignar la fecha del like

        // Guardar el like en el repositorio
        repositorioLike.guardar(like);
    }


    @Override
    public void quitarLike(long id) {
        Like like = repositorioLike.buscarPorId(id);
        if (like != null) {
            repositorioLike.eliminar(like.getId());
        }
    }

    @Override
    public boolean yaDioLike(long usuId, long publiId) {
        return repositorioLike.existePorUsuarioYPublicacion(usuId, publiId);
    }
    //HACER TOGGLE
    @Override
    public void toggleLike(long idUsuario, long publiId) {
            Publicacion publicacion = repositorioPublicacion.buscarPorId(publiId);
            if(publicacion == null) {
                throw new PublicacionNoEncontrada("Error al encontrar publicacion que se quiere dar like");
            }
            Usuario usuario = repositorioUsuario.buscarPorId(idUsuario);

            boolean yaDioLike = repositorioLike.existePorUsuarioYPublicacion(idUsuario, publiId);
            if (yaDioLike) {
                Like like = repositorioLike.buscarPorUsuarioYPublicacion(idUsuario, publiId);
                if(like != null) {
                    repositorioLike.eliminar(like.getId());
                }
            }   else {
                Like like = new Like();
                like.setUsuario(usuario);
                like.setPublicacion(publicacion);
                like.setFecha(LocalDateTime.now());
                repositorioLike.guardar(like);
            }
    }

    @Override
    public int contarLikes(long publiId) {
        return repositorioLike.contarPorPublicacion(publiId);
    }

    @Override
    public Like obtenerLike(long usuId, long publiId) {
        return repositorioLike.buscarPorUsuarioYPublicacion(usuId, publiId);
    }
}
