package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioGenero;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
@Controller
public class ControladorMiPerfil {

    private final ServicioUsuario servicioUsuario;
    private final ServicioGenero servicioGenero;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioUsuario servicioUsuario, ServicioGenero servicioGenero) {
        this.servicioUsuario = servicioUsuario;
        this.servicioGenero = servicioGenero;
    }

    @GetMapping("/miPerfil")
    @Transactional(readOnly = true)
    public ModelAndView miPerfil(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        Usuario usuario = servicioUsuario.buscarPorEmail(datosUsuario.getEmail());
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);
        model.addAttribute("generos", servicioGenero.listarGeneros());

        return new ModelAndView("miPerfil", model);
    }
}