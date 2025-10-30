package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class ControladorNotificacion {

    private final ServicioNotificacion servicioNotificacion;
    private final ServicioUsuario servicioUsuario;
    private final NotificacionService notificacionService;


    @Autowired
    public ControladorNotificacion(ServicioNotificacion servicioNotificacion,
                                   ServicioUsuario servicioUsuario, NotificacionService notificacionService) {
        this.servicioNotificacion = servicioNotificacion;
        this.servicioUsuario = servicioUsuario;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/notificaciones")
    public ModelAndView notificaciones(HttpSession session) {
        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return new ModelAndView("redirect:/login");

        ModelMap modelo = new ModelMap();
        modelo.addAttribute("notificaciones", servicioNotificacion.obtenerPorUsuario(usuario.getId()));

        //dtoUsuario
        modelo.addAttribute("usuario", usuario);




        List<DatosUsuariosNuevos> usuariosDTO = servicioUsuario.mostrarTodos()
                .stream()
                .map(u -> new DatosUsuariosNuevos(
                        u.getNombre(),
                        u.getApellido(),
                        u.getId()
                ))
                .collect(Collectors.toList());

        // 4. Pasar datos al modelo
        modelo.addAttribute("usuariosNuevos", usuariosDTO);
        modelo.addAttribute("esPropio", true);
        return new ModelAndView("notificaciones", modelo); // explícito, como login
    }


    @PostMapping("/marcar-leida/{id}")
    @ResponseBody
    public String marcarLeida(@PathVariable Long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            notificacionService.marcarLeidaYActualizarContador(usuario, id);
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
                            n.getUsuarioEmisor() != null ? n.getUsuarioEmisor().getNombre() : "Uninet",
                            n.getUrl()
                    ))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
