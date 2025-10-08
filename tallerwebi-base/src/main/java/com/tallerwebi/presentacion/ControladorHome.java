package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.*;

@Controller

public class ControladorHome {

    private final ServicioPublicado servicioPublicado;
    private final ServicioLike servicioLike;

    public ControladorHome(ServicioPublicado servicioPublicado, ServicioLike servicioLike) {
        this.servicioPublicado = servicioPublicado;
        this.servicioLike = servicioLike;
    }

    @GetMapping("/home")
    public ModelAndView home(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();

        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        model.addAttribute("usuario", usuario);

        List<Publicacion> publicaciones = servicioPublicado.findAll();
        List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
        for (Publicacion p : publicaciones) {
            DatosPublicacion dto = mapPublicacionToDto(p);
            datosPublicaciones.add(dto);
        }

        model.addAttribute("datosPublicaciones", datosPublicaciones);
        return new ModelAndView("home", model);
    }

    private DatosPublicacion mapPublicacionToDto(Publicacion p) {
        DatosPublicacion dto = new DatosPublicacion();
        dto.setId(p.getId());
        dto.setDescripcion(p.getDescripcion());
        dto.setNombreUsuario(p.getUsuario().getNombre());
        dto.setApellidoUsuario(p.getUsuario().getApellido());
        dto.setCantLikes(servicioLike.contarLikes(p));

        List<DatosComentario> comentariosDto = new ArrayList<>();
        if (p.getComentarios() != null) {
            for (Comentario c : p.getComentarios()) {
                comentariosDto.add(mapComentarioToDto(c));  // <--- Aquí usás el método nuevo
            }
        }
        dto.setComentariosDTO(comentariosDto);

        return dto;
    }

    private DatosComentario mapComentarioToDto(Comentario c) {
        DatosComentario dc = new DatosComentario();
        dc.setTexto(c.getTexto());

        if (c.getUsuario() != null) {
            dc.setNombreUsuario(c.getUsuario().getNombre());
            dc.setApellidoUsuario(c.getUsuario().getApellido());
        } else {
            dc.setNombreUsuario("Anonimo");
            dc.setApellidoUsuario("");
        }
        return dc;
    }
}

