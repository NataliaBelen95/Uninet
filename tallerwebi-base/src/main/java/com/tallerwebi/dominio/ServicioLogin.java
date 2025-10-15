package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EmailNoInstitucional;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente, EmailNoInstitucional;
    Usuario buscarPorEmail(String email);
}