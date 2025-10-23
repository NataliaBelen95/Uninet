package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Notificacion;
import com.tallerwebi.dominio.ServicioNotificacion;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class ControladorNotificacion {

    private final ServicioNotificacion servicioNotificacion;
    private final ServicioUsuario servicioUsuario;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ControladorNotificacion(ServicioNotificacion servicioNotificacion,
                                   ServicioUsuario servicioUsuario, SimpMessagingTemplate messagingTemplate) {
        this.servicioNotificacion = servicioNotificacion;
        this.servicioUsuario = servicioUsuario;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping ("/notificaciones")
    public ModelAndView notificaciones(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("notificaciones");

        DatosUsuario usuarioDTO = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (usuarioDTO != null) {
            // Traer notificaciones usando solo el id
            List<Notificacion> listaNotificaciones = servicioNotificacion.obtenerPorUsuario(usuarioDTO.getId());
            mav.addObject("notificaciones", listaNotificaciones);
        } else {
            mav.addObject("notificaciones", Collections.emptyList());
        }

        return mav;
    }
    @PostMapping("/marcar-leida/{id}")
    @ResponseBody
    public String marcarLeida(@PathVariable Long id, HttpServletRequest request) {
        servicioNotificacion.marcarLeida(id);

        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            int cantidadNoLeidas = servicioNotificacion.contarNoLeidas(usuario.getId());

            // 🔔 Enviar actualización al topic del usuario
            messagingTemplate.convertAndSend(
                    "/topic/notificaciones-" + usuario.getId(),
                    cantidadNoLeidas
            );
        }

        return "ok";
    }
    @GetMapping("/notificaciones-dropdown")
    @ResponseBody
    public List<DatosNotificacion> notificacionesDropdown(HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if(datos != null){
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            // Solo devolver las no leídas si querés que desaparezcan al marcar como leída
            return servicioNotificacion.obtenerPorUsuario(usuario.getId())
                    .stream()
                    .filter(n -> !n.isLeida())  // FILTRAR LEIDAS
                    .map(n -> new DatosNotificacion(
                            n.getId(),
                            n.getMensaje(),
                            n.isLeida(),
                            n.getFechaCreacion(),
                            n.getUsuarioEmisor().getNombre()
                    ))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
