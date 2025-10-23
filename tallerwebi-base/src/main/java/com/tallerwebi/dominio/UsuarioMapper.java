package com.tallerwebi.dominio;


import com.tallerwebi.presentacion.DatosUsuario;
import com.tallerwebi.presentacion.PublicacionMapper;

import java.util.stream.Collectors;


public class UsuarioMapper {

    private final PublicacionMapper publicacionMapper;
    public UsuarioMapper(PublicacionMapper publicacionMapper) {
        this.publicacionMapper = publicacionMapper;
    }


    public  DatosUsuario toDTO(Usuario usuario) {
        DatosUsuario dto = new DatosUsuario();

        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setCarrera(usuario.getCarrera());
        dto.setFotoPerfil(usuario.getFotoPerfil());

        // Publicaciones propias
        if (usuario.getPublicaciones() != null) {
            dto.setDtopublicaciones(
                    usuario.getPublicaciones().stream()
                            .map(p -> publicacionMapper.toDto(p, usuario.getId()))
                            .collect(Collectors.toList())
            );
        }



        return dto;
    }
}