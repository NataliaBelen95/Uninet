package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.presentacion.DatosUsuario;
import io.grpc.ClientStreamTracer;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion, Usuario usuario, File archivo) throws PublicacionFallida, IOException;
    public void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivoSubido) throws PublicacionFallida;
    List<Publicacion> findAll();
    int obtenerCantidadDeLikes(long id);
    void eliminarPublicacionEntera(Publicacion publicacion);
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId);
    Publicacion obtenerPublicacion(long id);


}