package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public DatosUsuario toDatosUsuario(Usuario usuario) {
        DatosUsuario datosUsuario = new DatosUsuario();
        datosUsuario.setId(usuario.getId());
        datosUsuario.setNombre(usuario.getNombre());
        datosUsuario.setApellido(usuario.getApellido());
        datosUsuario.setEmail(usuario.getEmail());
        datosUsuario.setCarrera(usuario.getCarrera());

        return datosUsuario;

    }

    public Usuario aEntidad(DatosUsuario dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setCarrera(dto.getCarrera());
        return usuario;


    }


}
