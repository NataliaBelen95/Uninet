package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
@Controller
public class ControladorMiPerfil {

    private final ServicioUsuario servicioUsuario;
    private final ServicioGenero servicioGenero;
    private final ServicioLogin servicioLogin;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioUsuario servicioUsuario, ServicioGenero servicioGenero, ServicioLogin servicioLogin) {
        this.servicioUsuario = servicioUsuario;
        this.servicioGenero = servicioGenero;
        this.servicioLogin = servicioLogin;
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

    @PostMapping("/miPerfil")
    @Transactional
    public ModelAndView actualizarPerfil(@ModelAttribute("usuario")  Usuario usuario, HttpServletRequest request) {
       DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
       if(datosUsuario == null){
           return new ModelAndView("miPerfil");
       }
       Usuario usuarioEnBD = servicioUsuario.buscarPorEmail(datosUsuario.getEmail());

       //Acá actualizamos los campos editables
        usuarioEnBD.setEmail(usuario.getEmail());
        usuarioEnBD.setEmailPersonal(usuario.getEmail());
        usuarioEnBD.setFechaNacimiento(usuario.getFechaNacimiento());
        usuarioEnBD.setTelefono(usuario.getTelefono());
        usuarioEnBD.setDireccion(usuario.getDireccion());
        usuarioEnBD.setLocalidad(usuario.getLocalidad());
        usuarioEnBD.setCodigoPostal(usuario.getCodigoPostal());
        usuarioEnBD.setProvincia(usuario.getProvincia());
        usuarioEnBD.setGenero(usuario.getGenero());
        usuarioEnBD.setPassword(usuario.getPassword());

        //Guardamos en la base de datos
        servicioUsuario.actualizar(usuarioEnBD);

        //Actualizamos el modelo y mostramos mensaje
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuarioEnBD);
        model.addAttribute("generos", servicioGenero.listarGeneros());
        model.addAttribute("mensaje", "Cambios guardados con éxito");

        return new ModelAndView("miPerfil", model);
    }
}