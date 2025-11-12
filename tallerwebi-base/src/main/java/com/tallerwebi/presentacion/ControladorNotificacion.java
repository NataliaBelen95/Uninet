package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosNotificacion;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.DatosUsuariosNuevos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            return servicioNotificacion.obtenerPorUsuario(usuario.getId())
                    .stream()
                    .filter(n -> !n.isLeida())
                    .map(n -> {
                        // extraer solicitudId de la URL si existe
                        Long amistadId = null;
                        try {
                            String url = n.getUrl();
                            if (url != null && url.contains("solicitudId=")) {
                                java.util.regex.Matcher m = java.util.regex.Pattern.compile("[?&]solicitudId=(\\d+)").matcher(url);
                                if (m.find()) amistadId = Long.parseLong(m.group(1));
                            }
                        } catch (Exception ex) { /* ignore */ }

                        Long emisorId = n.getUsuarioEmisor() != null ? n.getUsuarioEmisor().getId() : null;
                        Long receptorId = n.getUsuarioReceptor() != null ? n.getUsuarioReceptor().getId() : usuario.getId();

                        return new DatosNotificacion(
                                n.getId(),
                                n.getMensaje(),
                                n.isLeida(),
                                n.getFechaCreacion(),
                                n.getUsuarioEmisor() != null ? n.getUsuarioEmisor().getNombre() : "Uninet",
                                n.getUrl(),
                                amistadId,
                                emisorId,
                                n.getTipo()
                        );
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
