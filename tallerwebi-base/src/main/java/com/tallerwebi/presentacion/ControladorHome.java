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

        // Obtener usuario logueado de la sesión (DTO)
        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        model.addAttribute("usuario", usuario);

        // Publicaciones y likes
        List<Publicacion> publicaciones = servicioPublicado.findAll();
        List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
        for (Publicacion p : publicaciones) {
            DatosPublicacion dto = new DatosPublicacion();
            dto.setId(p.getId());
            dto.setDescripcion(p.getDescripcion());
            dto.setNombreUsuario(p.getUsuario().getNombre());
            dto.setApellidoUsuario(p.getUsuario().getApellido());
            dto.setCantLikes(servicioLike.contarLikes(p));
            List<Comentario> comentarios = p.getComentarios() != null ? p.getComentarios() : Collections.emptyList();
            dto.setComentarios(comentarios);
            datosPublicaciones.add(dto);
        }

        model.addAttribute("datosPublicaciones", datosPublicaciones);

        return new ModelAndView("home", model);
    }
    }

