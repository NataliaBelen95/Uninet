package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida;
    List<Publicacion> findAll();
    int obtenerCantidadDeLikes(long id);
    void eliminarPublicacionEntera(Publicacion publicacion);
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId);
    Publicacion obtenerPublicacion(long id);
}