package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion, Usuario usuario, File archivo) throws PublicacionFallida, IOException;
    public void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivoSubido) throws PublicacionFallida, IOException;
    List<Publicacion> findAll();
    int obtenerCantidadDeLikes(long id);
    void eliminarPublicacionEntera(Publicacion publicacion);
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId);
    Publicacion obtenerPublicacion(long id);
    List <Publicacion> obtenerPorLikeDeUsuario(long id);
    List <Publicacion> obtenerPublicacionesDeUsuario(long usuId);


    // 2. 🔑 NUEVO MÉTODO PARA EL BOT (URL de la Imagen)
    void guardarPubliBot(Publicacion publicacion, Usuario usuario, String urlImagen) throws PublicacionFallida;

    List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario);

    List <Publicacion> obtenerTodasPublicacionesIgnorandoPublicidades();
}