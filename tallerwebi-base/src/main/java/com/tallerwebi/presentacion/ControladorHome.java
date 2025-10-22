package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ControladorHome {

    private final PublicacionMapper publicacionMapper;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    //private final ServicioUsuario servicioUsuario;



    public ControladorHome(ServicioPublicacion servicioPublicacion, ServicioLike servicioLike, PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.publicacionMapper = publicacionMapper;
        //this.servicioUsuario = servicioUsuario;

    }

    @GetMapping("/home")
    public ModelAndView home(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();

        // Obtener el usuario logueado
        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }                                         //dtoUsuario
        model.addAttribute("usuario", usuario);

        // Ahora la lista de publicaciones ya trae los comentarios por estar con `EAGER` en la entidad
        List<Publicacion> publicaciones = servicioPublicacion.findAll();

        // Mapear las publicaciones a DTOs
        List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
        for (Publicacion publicacion : publicaciones) {
            DatosPublicacion dto = publicacionMapper.toDto(publicacion, usuario.getId());
            //System.out.println("Publicacion usuarioId: " + dto.getUsuarioId());
            datosPublicaciones.add(dto);
        }

        model.addAttribute("datosPublicaciones", datosPublicaciones);
        model.addAttribute("origen", "home");
        return new ModelAndView("home", model);
    }


}

