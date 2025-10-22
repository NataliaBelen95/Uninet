package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Notificacion;
import com.tallerwebi.dominio.ServicioNotificacion;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;


@Controller
public class ControladorNotificacion {

    private final ServicioNotificacion servicioNotificacion;
    private final ServicioUsuario servicioUsuario;


    @Autowired
    public ControladorNotificacion(ServicioNotificacion servicioNotificacion,
                                   ServicioUsuario servicioUsuario) {
        this.servicioNotificacion = servicioNotificacion;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/notificaciones")
    public ModelAndView notificaciones(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("notificaciones"); // nombre de la vista Thymeleaf

        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());

            List<Notificacion> listaNotificaciones = servicioNotificacion.obtenerPorUsuario(usuario);
            //  Forzar la carga de publicacion.id antes de pasar a la vista


            mav.addObject("notificaciones", listaNotificaciones);
        } else {
            mav.addObject("notificaciones", Collections.emptyList());
        }

        return mav;
    }

    @PostMapping("/marcar-leida/{id}")
    @ResponseBody
    public String marcarLeida(@PathVariable Long id) {
        servicioNotificacion.marcarLeida(id);
        return "ok";
    }

}
