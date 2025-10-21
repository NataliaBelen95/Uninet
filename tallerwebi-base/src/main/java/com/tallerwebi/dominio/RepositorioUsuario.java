package com.tallerwebi.dominio;


import org.springframework.stereotype.Repository;

import java.util.List;

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

}