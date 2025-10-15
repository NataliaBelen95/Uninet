package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional  // Esto asegura que cualquier operación de Hibernate funcione con sesión y transacción
public class ServicioUsuario {
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    public Usuario buscarPorEmail(String email) {
        return repositorioUsuario.buscar(email); // Aquí llamas al método que ya tenés
    }

    public Usuario buscarPorId(long id) {
        return repositorioUsuario.buscarPorId(id);
    }

    public List<Usuario> mostrarTodos() {
        return repositorioUsuario.buscarTodos();
    }

    @Transactional
    public void actualizar(Usuario usuario) {
        repositorioUsuario.actualizar(usuario);
    }

    @Transactional
    public Usuario buscarUsuarioPorIdConPublicaciones(long id) {
       return repositorioUsuario.findByIdWithPublicaciones(id);

    }

    public void actualizarContrasena(Usuario usuario, String nuevaContrasena) {
        repositorioUsuario.actualizarContrasena(usuario, nuevaContrasena);
    }
}