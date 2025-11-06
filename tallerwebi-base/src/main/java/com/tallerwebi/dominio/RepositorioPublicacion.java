package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface RepositorioPublicacion {

    void guardar(Publicacion publicacion);
    Publicacion buscarPorId(Long id);
    List<Publicacion> listarTodas();
    boolean existeIgual(Publicacion publicacion);
    List<Publicacion> findByUsuarioId(Long id);
    void eliminarPubli(Publicacion publicacion);
    Publicacion obtenerPublicacionCompleta(long id);
    boolean existeHashResumen(String hash, Long usuarioId);
    List<Publicacion> obtenerPublicacionesConLikeDeUsuario(Long usuarioId);
    List<Publicacion> obtenerPublicacionesDeUsuario(Long usuarioId);
    //List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario);
    List<Publicacion> obtenerPublicacionesDirigidasA(Usuario usuario);

}