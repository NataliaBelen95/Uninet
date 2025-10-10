package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@Transactional
public class ServicioLikeImpl implements ServicioLike {

    @Autowired
    private RepositorioLike repositorioLike;

    @Override
    public void darLike(Usuario usuario, Publicacion publicacion) {
        if (repositorioLike.existePorUsuarioYPublicacion(usuario, publicacion)) {
            return; // Ya dio like
        }

        Like like = new Like();
        like.setUsuario(usuario);
        like.setPublicacion(publicacion);
        like.setFecha(LocalDateTime.now());

        repositorioLike.guardar(like);
    }

    @Override
    public void quitarLike(long id) {
        Like like = repositorioLike.buscarPorId(id);
        if (like != null) {
            repositorioLike.eliminar(like);
        }
    }

    @Override
    public boolean yaDioLike(Usuario usuario, Publicacion publicacion) {
        return repositorioLike.existePorUsuarioYPublicacion(usuario, publicacion);
    }

    @Override
    public int contarLikes(Publicacion publicacion) {
        return repositorioLike.contarPorPublicacion(publicacion);
    }

    @Override
    public Like obtenerLike(Usuario usuario, Publicacion publicacion) {
        return repositorioLike.buscarPorUsuarioYPublicacion(usuario, publicacion);
    }
}
