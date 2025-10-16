package com.tallerwebi.dominio;

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

    @Override
    public int contarLikes(long publiId) {
        return repositorioLike.contarPorPublicacion(publiId);
    }

    @Override
    public Like obtenerLike(long usuId, long publiId) {
        return repositorioLike.buscarPorUsuarioYPublicacion(usuId, publiId);
    }
}
