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
        dto.setEmailPersonal(usuario.getEmailPersonal());  // <-- agregado
        dto.setPassword(usuario.getPassword()); // Si querés mostrar/editar contraseña (cuidado con seguridad)
        dto.setDni(usuario.getDni()); // <-- agregado
        dto.setFechaNacimiento(usuario.getFechaNacimiento()); // <-- agregado
        dto.setTelefono(usuario.getTelefono()); // <-- agregado
        dto.setDireccion(usuario.getDireccion()); // <-- agregado
        dto.setLocalidad(usuario.getLocalidad()); // <-- agregado
        dto.setProvincia(usuario.getProvincia()); // <-- agregado
        dto.setCodigoPostal(usuario.getCodigoPostal()); // <-- agregado
        dto.setGenero(usuario.getGenero()); // <-- agregado (objeto Genero completo)
        dto.setCarrera(usuario.getCarrera());
        dto.setFotoPerfil(usuario.getFotoPerfil());

        if (usuario.getPublicaciones() != null) {
            dto.setDtopublicaciones(
                    usuario.getPublicaciones().stream()
                            .map(p -> publicacionMapper.toDto(p, usuario.getId()))
                            .collect(Collectors.toList())
            );
        }

        if (usuario.getLikesDados() != null) {
            dto.setLikesGuardados(
                    usuario.getLikesDados().stream()
                            .map(p -> publicacionMapper.toDto(p.getPublicacion(), usuario.getId()))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public Usuario toEntity(DatosUsuario dto, Usuario usuarioExistente) {
        // Actualiza la entidad usuarioExistente con los datos del DTO
        usuarioExistente.setNombre(dto.getNombre());
        usuarioExistente.setApellido(dto.getApellido());
        usuarioExistente.setEmail(dto.getEmail());
        usuarioExistente.setEmailPersonal(dto.getEmailPersonal());
        usuarioExistente.setPassword(dto.getPassword());
        usuarioExistente.setDni(dto.getDni());
        usuarioExistente.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioExistente.setTelefono(dto.getTelefono());
        usuarioExistente.setDireccion(dto.getDireccion());
        usuarioExistente.setLocalidad(dto.getLocalidad());
        usuarioExistente.setProvincia(dto.getProvincia());
        usuarioExistente.setCodigoPostal(dto.getCodigoPostal());
        usuarioExistente.setGenero(dto.getGenero());
        usuarioExistente.setCarrera(dto.getCarrera());
        usuarioExistente.setFotoPerfil(dto.getFotoPerfil());

        // No mapear listas aquí a menos que sea necesario

        return usuarioExistente;
    }
}
