package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface RepositorioPublicacion {

    void guardar(Publicacion publicacion);
    Publicacion buscarPorId(Long id);
    List<Publicacion> listarTodas();
    void eliminarPubli(Publicacion publicacion);
    Publicacion obtenerPublicacionCompleta(long id);
    List<Publicacion> obtenerPublicacionesConLikeDeUsuario(Long usuarioId);
    List<Publicacion> obtenerPublicacionesDeUsuario(Long usuarioId);
    List<Publicacion> obtenerPublicacionesDirigidasA(Usuario usuario);



//    /***********************************************************************/
//    //CHEQUEAR QUEDARON SIN USO
//    //boolean existeHashResumen(String hash, Long usuarioId);
//    //List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario);
//    //boolean existeIgual(Publicacion publicacion);
//    /***********************************************************************/
}