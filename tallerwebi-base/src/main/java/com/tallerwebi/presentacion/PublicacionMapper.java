package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PublicacionMapper {

    private final ServicioLike servicioLike;
    private final ServicioComentario servicioComentario;
    private Usuario usuario;


    public PublicacionMapper(ServicioLike servicioLike, ServicioComentario servicioComentario) {
        this.servicioLike = servicioLike;
        this.servicioComentario = servicioComentario;
    }

    public DatosPublicacion toDto(Publicacion p) {
        DatosPublicacion dto = new DatosPublicacion();

        dto.setId(p.getId());
        dto.setDescripcion(p.getDescripcion());
        dto.setNombreUsuario(p.getUsuario().getNombre());
        dto.setApellidoUsuario(p.getUsuario().getApellido());
        dto.setCantLikes(servicioLike.contarLikes(p.getId()));
        dto.setCantComentarios(servicioComentario.contarComentarios(p.getId()));
        dto.setFechaPublicacion(p.getFechaPublicacion());
        dto.setUsuarioId(p.getUsuario().getId());
        dto.setDioLike(servicioLike.yaDioLike(usuario, p));


        // Debug print
       // System.out.println("Usuario ID: " + p.getUsuario().getId());
        // Mapear comentarios
        List<DatosComentario> comentariosDto = new ArrayList<>();
        if (p.getComentarios() != null) {
            for (Comentario c : p.getComentarios()) {
                comentariosDto.add(toComentarioDto(c));
            }
        }
        dto.setComentarios(comentariosDto);

        // Mapeo de archivo (sin usar DatosArchivoPublicacion)
        if (p.getArchivo() != null) { // Verificamos si el archivo no es nulo
            dto.setArchivoNombre(p.getArchivo().getNombreArchivo()); // Asignar nombre del archivo
            dto.setArchivoTipo(p.getArchivo().getTipoContenido());   // Asignar tipo de archivo
        }

        return dto;
    }

    private DatosComentario toComentarioDto(Comentario c) {
        DatosComentario comentarioDto = new DatosComentario();
        comentarioDto.setTexto(c.getTexto());
        comentarioDto.setNombreUsuario(c.getUsuario().getNombre());
        comentarioDto.setApellidoUsuario(c.getUsuario().getApellido());
        // Agregar otros campos necesarios de Comentario
        return comentarioDto;
    }
}

