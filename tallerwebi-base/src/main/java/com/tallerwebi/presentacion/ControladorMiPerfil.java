package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
@Controller
public class ControladorMiPerfil {

    private final ServicioLogin servicioLogin;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioLogin servicioLogin) {
        this.servicioLogin = servicioLogin;
    }



    @GetMapping("/miPerfil")
    @Transactional
    public ModelAndView miPerfil(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        System.out.println("Usuario en sesión: " + datosUsuario);
        System.out.println("Email en sesión: " + datosUsuario.getEmail());

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", datosUsuario);

        return new ModelAndView("miPerfil", model);
    }


}