package com.tallerwebi.dominio;


import com.tallerwebi.infraestructura.RepositorioComentarioImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void comentar(String texto,  Usuario usuario, Publicacion p) {
        Usuario usuarioBuscado = repositorioUsuario.buscarPorId(usuario.getId());
        Publicacion publicacionBuscado = repositorioPublicacion.buscarPorId(p.getId());
        Comentario comentario = new Comentario();
        comentario.setUsuario(usuarioBuscado);
        comentario.setPublicacion(publicacionBuscado);
        comentario.setTexto(texto);

        if(usuarioBuscado !=null && publicacionBuscado!=null){
            repositorioComentario.guardar(comentario);
        }

    }

    @Override
    public void editarComentario(Comentario comentario) {

    }

    @Override
    public int contarComentarios(Publicacion publicacion) {
        return repositorioComentario.contarComentarioPorPublicacion(publicacion);

    }

}
