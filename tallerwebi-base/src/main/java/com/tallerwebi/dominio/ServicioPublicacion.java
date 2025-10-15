package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida;
    List<Publicacion> findAll();
    Publicacion  obtenerPublicacionPorId(long id);

    int obtenerCantidadDeLikes(long id);
    void eliminarPublicacionEntera(Publicacion publicacion);
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId);
    Publicacion obtenerPublicacion(Long id);
}