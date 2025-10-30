package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.ServicioNotificacion;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DatosUsuario;
import com.tallerwebi.presentacion.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class NotificacionGlobalControlador {

    private final ServicioNotificacion servicioNotificacion;
    private final ServicioUsuario servicioUsuario;
    private final UsuarioMapper usuarioMapper;
    @Autowired
    public NotificacionGlobalControlador(ServicioUsuario servicioUsuario,
                                         ServicioNotificacion servicioNotificacion,
                                         UsuarioMapper usuarioMapper) {
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.usuarioMapper = usuarioMapper;
    }

    @ModelAttribute("usuarioDTO")
    public DatosUsuario agregarUsuarioConNotificaciones(HttpServletRequest request) {
        DatosUsuario usuarioDTO = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (usuarioDTO != null) {
            // Solo actualizar cantidad de notificaciones
            int cantidad = servicioNotificacion.contarNoLeidas(usuarioDTO.getId());
            usuarioDTO.setCantidadNotificaciones(cantidad);
            return usuarioDTO;
        }
        return null;
    }




}
