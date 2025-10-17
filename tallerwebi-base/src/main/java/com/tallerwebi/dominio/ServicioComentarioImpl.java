package com.tallerwebi.dominio;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicioComentarioImpl implements ServicioComentario {

    private final RepositorioComentario repositorioComentario;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPublicacion repositorioPublicacion;

    public ServicioComentarioImpl(RepositorioComentario repositorioComentario, RepositorioUsuario repositorioUsuario, RepositorioPublicacion repositorioPublicacion) {
        this.repositorioComentario = repositorioComentario;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPublicacion = repositorioPublicacion;
    }
    @Override
    public Comentario comentar(String texto, Usuario usuario, Publicacion p) {
        Comentario comentario = new Comentario();
        comentario.setTexto(texto);
        comentario.setUsuario(usuario);
        comentario.setPublicacion(p);


        return repositorioComentario.guardar(comentario);
    }

    @Override
    public void editarComentario(Comentario comentario) {

    }

    @Override
    public int contarComentarios(long publiId) {
     return repositorioComentario.contarComentarioPorPublicacion(publiId);


    }

    @Override
    public List<Comentario> encontrarComentariosPorId(long id) {
        return repositorioComentario.findComentariosByPublicacionId(id);
    }

    @Override
    public Usuario usuarioqueComento(long usuId) {
        return repositorioComentario.encontrarUsuarioQueHizoComentario(usuId);
    }

}
