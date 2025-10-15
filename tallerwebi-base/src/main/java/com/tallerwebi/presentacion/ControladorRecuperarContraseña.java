package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRecuperarContraseña {

    @Autowired
    private ServicioUsuario servicioUsuario;


    @GetMapping("/recuperar")
    public ModelAndView mostrarFormulario() {
        ModelAndView mav = new ModelAndView("recuperarContrasenia");
        mav.addObject("datosRecuperarContrasenia", new DatosRecuperarContrasenia());
        return mav;
    }

    @PostMapping("/recuperar")
    public ModelAndView procesarFormulario(@ModelAttribute("datosRecuperarContrasenia") DatosRecuperarContrasenia datosRecuperarContrasenia) {
        ModelAndView mav = new ModelAndView("recuperarContrasenia");

        if (!datosRecuperarContrasenia.getNuevaContrasena().equals(datosRecuperarContrasenia.getRepetirContrasena())) {
            mav.addObject("error", "Las contraseñas no coinciden.");
            return mav;
        }

        Usuario usuario = servicioUsuario.buscarPorEmail(datosRecuperarContrasenia.getEmail());
        if (usuario == null) {
            mav.addObject("error", "No se encontró un usuario con ese email.");
            return mav;
        }

        servicioUsuario.actualizarContrasena(usuario, datosRecuperarContrasenia.getNuevaContrasena());
        mav.addObject("mensaje", "Contraseña actualizada correctamente.");
        mav.addObject("usuario", new DatosRecuperarContrasenia());
        return mav;
    }

}

