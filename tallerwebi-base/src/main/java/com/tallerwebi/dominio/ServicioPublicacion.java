package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.presentacion.DatosUsuario;
import io.grpc.ClientStreamTracer;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida;
    List<Publicacion> findAll();
    int obtenerCantidadDeLikes(long id);
    void eliminarPublicacionEntera(Publicacion publicacion);
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId);
    Publicacion obtenerPublicacion(long id);
    void compartirResumen(DatosUsuario dtoUsuario, String resumen, String nombreArchivo) throws PublicacionFallida;


}