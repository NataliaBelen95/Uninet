package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
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
    private final ServicioPublicado servicioPublicado;
    private final ServicioLike servicioLike;


    public ControladorHome(ServicioPublicado servicioPublicado, ServicioLike servicioLike, PublicacionMapper publicacionMapper) {
        this.servicioPublicado = servicioPublicado;
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

        List<Publicacion> publicaciones = servicioPublicado.findAll();
        List<DatosPublicacion> datosPublicaciones = publicaciones.stream()
                .map(publicacionMapper::toDto) //equivale a .map(p-> publicacionMapper.toDto(p))
                .collect(Collectors.toList());

        model.addAttribute("datosPublicaciones", datosPublicaciones);
        return new ModelAndView("home", model);
    }
}

