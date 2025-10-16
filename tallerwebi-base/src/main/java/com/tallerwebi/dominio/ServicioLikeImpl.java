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
    private RepositorioUsuario repositorioUsuario;
    private RepositorioPublicacion repositorioPublicacion;

    @Override
    public void darLike(long usuId, long publiId) {
        Like like = new Like();
        like.setUsuario(repositorioUsuario.buscarPorId(usuId));
        like.setPublicacion(repositorioPublicacion.buscarPorId(publiId));
        like.setFecha(LocalDateTime.now());

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
