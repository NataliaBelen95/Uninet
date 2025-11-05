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
    // Mapper principal con flag de "propio"
    public DatosUsuario toDto(Usuario usuario, boolean esPropio) {
        DatosUsuario dto = new DatosUsuario();

        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setEmailPersonal(usuario.getEmailPersonal());
        dto.setPassword(usuario.getPassword());
        dto.setDni(usuario.getDni());
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        dto.setTelefono(usuario.getTelefono());
        dto.setDireccion(usuario.getDireccion());
        dto.setLocalidad(usuario.getLocalidad());
        dto.setProvincia(usuario.getProvincia());
        dto.setCodigoPostal(usuario.getCodigoPostal());
        dto.setGenero(usuario.getGenero());
        dto.setCarrera(usuario.getCarrera());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setSlug(usuario.getSlug() != null ? usuario.getSlug() : "");
        dto.setIsBot(usuario.isEsBot());

        // Solo mapeamos publicaciones y likes si es propio
        if (esPropio) {
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
        }

        return dto;
    }

    // Métodos de conveniencia
    public DatosUsuario toDtoPublico(Usuario usuario) {
        return toDto(usuario, false);
    }

    public DatosUsuario toDtoPropio(Usuario usuario) {
        return toDto(usuario, true);
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
