package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class PublicacionMapper {

    private final ServicioLike servicioLike;
    private final ServicioComentario servicioComentario;
    private Usuario usuario;


    public PublicacionMapper(ServicioLike servicioLike, ServicioComentario servicioComentario) {
        this.servicioLike = servicioLike;
        this.servicioComentario = servicioComentario;
    }

    // --- Método base (por defecto sin comentarios) ---
    public DatosPublicacion toDto(Publicacion p, long usuarioId) {
        return toDto(p, usuarioId, true);
    }
    // --- Sobrecarga: permite excluir comentarios ---
    public DatosPublicacion toDto(Publicacion p, long usuarioId, boolean incluirComentarios) {
        DatosPublicacion dto = new DatosPublicacion();

        dto.setId(p.getId());
        dto.setDescripcion(p.getDescripcion());


        dto.setCantLikes(servicioLike.contarLikes(p.getId()));
        dto.setCantComentarios(servicioComentario.contarComentarios(p.getId()));
        dto.setFechaPublicacion(p.getFechaPublicacion());
        dto.setUsuarioId(p.getUsuario().getId());
        dto.setDioLike(servicioLike.yaDioLike(usuarioId, p.getId()));
        dto.setEsPropio(p.getUsuario().getId() == usuarioId);

        // --- Crear DTO del autor ---
        DatosUsuario autorDto = new DatosUsuario();
        autorDto.setId(p.getUsuario().getId());
        autorDto.setNombre(p.getUsuario().getNombre());
        autorDto.setApellido(p.getUsuario().getApellido());
        autorDto.setSlug(p.getUsuario().getSlug());
        autorDto.setUrl(p.getUsuario().getSlug().equals(usuarioId) ? "/miPerfil" : "/perfil/" + p.getUsuario().getSlug());
        dto.setAutor(autorDto);
        // Incluye comentarios solo cuando se necesita
        if (incluirComentarios && p.getComentarios() != null) {
            List<DatosComentario> comentariosDto = new ArrayList<>();
            for (Comentario c : p.getComentarios()) {
                comentariosDto.add(toComentarioDto(c));
            }
            dto.setComentarios(comentariosDto);
        }

        if (p.getArchivo() != null) {
            dto.setArchivoNombre(p.getArchivo().getNombreArchivo());
            dto.setArchivoTipo(p.getArchivo().getTipoContenido());
        }

        return dto;
    }


    // Métodos de conveniencia
    public DatosPublicacion toDtoPublica(Publicacion p, long usuarioId) {
        return toDto(p, usuarioId, true);
    }

    public DatosPublicacion toDtoPropia(Publicacion p, long usuarioId) {
        return toDto(p, usuarioId, false);
    }


    public DatosComentario toComentarioDto(Comentario c) {
        DatosComentario comentarioDto = new DatosComentario();
        comentarioDto.setTexto(c.getTexto());
        comentarioDto.setNombreUsuario(c.getUsuario().getNombre());
        comentarioDto.setApellidoUsuario(c.getUsuario().getApellido());
        //comentarioDto.setFechaComentario(LocalDateTime.now());
        // Agregar otros campos necesarios de Comentario
        return comentarioDto;
    }
}

