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


    public ControladorHome(ServicioPublicacion servicioPublicacion, ServicioLike servicioLike, PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.publicacionMapper = publicacionMapper;
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

        List<Publicacion> publicaciones = servicioPublicacion.findAll();
        for (Publicacion p : publicaciones) {
            Hibernate.initialize(p.getArchivo());  // Evita LazyInitializationException para archivos
            // Inicializar más colecciones si usas más lazy
        }

        List<DatosPublicacion> datosPublicaciones = publicaciones.stream()
                .map(publicacionMapper::toDto)
                .collect(Collectors.toList());

        model.addAttribute("datosPublicaciones", datosPublicaciones);
        return new ModelAndView("home", model);
    }

}

