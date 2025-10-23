package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.Usuario;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    private final PublicacionMapper publicacionMapper;

    public UsuarioMapper(PublicacionMapper publicacionMapper) {
        this.publicacionMapper = publicacionMapper;
    }

    public DatosUsuario toDto(Usuario usuario) {
        DatosUsuario dto = new DatosUsuario();

        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setCarrera(usuario.getCarrera());
        dto.setFotoPerfil(usuario.getFotoPerfil());

        // 🟢 Publicaciones propias
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
