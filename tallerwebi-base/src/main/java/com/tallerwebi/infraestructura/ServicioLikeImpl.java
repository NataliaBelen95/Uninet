package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
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
            repositorioLike.eliminar(like.getId());
        }
    }

    @Override
    public boolean yaDioLike(Usuario usuario, Publicacion publicacion) {
        return repositorioLike.existePorUsuarioYPublicacion(usuario, publicacion);
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
