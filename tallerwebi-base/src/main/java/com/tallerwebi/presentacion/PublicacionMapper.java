package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.ServicioComentario;
import com.tallerwebi.dominio.ServicioLike;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PublicacionMapper {

    private final ServicioLike servicioLike;
    private final ServicioComentario servicioComentario;

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
        dto.setCantLikes(servicioLike.contarLikes(p));
        dto.setCantComentarios(servicioComentario.contarComentarios(p));
        dto.setFechaPublicacion(p.getFechaPublicacion());
        List<DatosComentario> comentariosDto = new ArrayList<>();
        if (p.getComentarios() != null) {
            for (Comentario c : p.getComentarios()) {
                comentariosDto.add(toComentarioDto(c));
            }
        }
        dto.setComentariosDTO(comentariosDto);

        return dto;
    }

    public DatosComentario toComentarioDto(Comentario c) {
        DatosComentario dc = new DatosComentario();
        dc.setTexto(c.getTexto());

        if (c.getUsuario() != null) {
            dc.setNombreUsuario(c.getUsuario().getNombre());
            dc.setApellidoUsuario(c.getUsuario().getApellido());
        } else {
            dc.setNombreUsuario("Anónimo");
            dc.setApellidoUsuario("");
        }

        return dc;
    }
}
