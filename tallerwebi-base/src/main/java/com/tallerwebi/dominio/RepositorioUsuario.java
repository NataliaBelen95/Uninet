package com.tallerwebi.dominio;


import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario {

    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void actualizar(Usuario usuario);
    void setearCarreraAUsuario(Usuario usuario, Carrera carrera);
    Usuario buscarPorId(long id);
    List<Usuario> buscarTodos();
    Usuario findByIdWithPublicaciones(long id);
    void actualizarContrasena(Usuario usuario, String nuevaContrasena);
    Usuario buscarPorSlug(String slug);
    List<Usuario> findAll();
    List<Publicacion> obtenerPublicacionesDeUsuario(long usuId);
}