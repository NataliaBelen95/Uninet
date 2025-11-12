package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/amistad")
public class ControladorAmistad {

    private final ServicioAmistad servicioAmistad;
    private final ServicioNotificacion servicioNotificacion;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioUsuario servicioUsuario;
    private final UsuarioMapper usuarioMapper;

    @Autowired
    public ControladorAmistad(ServicioAmistad servicioAmistad, ServicioNotificacion servicioNotificacion, RepositorioUsuario repositorioUsuario, ServicioUsuario servicioUsuario, UsuarioMapper usuarioMapper) {
        this.servicioAmistad = servicioAmistad;
        this.usuarioMapper = usuarioMapper;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }

    // ... (Métodos listarAmigos, listarSolicitudesPendientes, rechazarSolicitud omitidos por espacio, asumo que son correctos) ...

    @PostMapping("/rechazar/{idSolicitud}")
    public String rechazarSolicitud(@PathVariable Long idSolicitud) {
        servicioAmistad.rechazarSolicitud(idSolicitud);
        return "redirect:/notificaciones?tab=solicitudes";
    }

    //
    @PostMapping("/enviar/{idReceptor}")
    public String enviarSolicitud(@PathVariable Long idReceptor, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }

        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(idReceptor);
        if (solicitante == null || receptor == null) {
            return "redirect:/usuarios";
        }
        SolicitudAmistad solicitudCreada = servicioAmistad.enviarSolicitud(solicitante, receptor);
        Long solicitudId = solicitudCreada.getId();

        try {
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);
        } catch (Exception e) {
            System.err.println("Error al notificar solicitud de amistad: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }


    @PostMapping("/enviar")
    public String enviarSolicitudForm(@RequestParam("receptorId") Long receptorId, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);
        if (solicitante == null || receptor == null) {
            return "redirect:/usuarios";
        }


        SolicitudAmistad solicitudCreada = servicioAmistad.enviarSolicitud(solicitante, receptor);
        Long solicitudId = solicitudCreada.getId();

        try {
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);
        } catch (Exception e) {
            System.err.println("Error al notificar solicitud de amistad: " + e.getMessage());
        }

        return "redirect:/home";
    }

    @PostMapping("/aceptar/{idSolicitud}")
    public ResponseEntity<String> aceptarSolicitud(@PathVariable Long idSolicitud) {

        try {
            boolean exito = servicioAmistad.aceptarSolicitud(idSolicitud);

            if (exito) {
                return ResponseEntity.ok("Amistad aceptada con éxito.");
            } else {
                return ResponseEntity.status(404).body("Solicitud no encontrada o ya ha sido procesada.");
            }

        } catch (Exception e) {
            System.err.println("Error al aceptar solicitud: " + e.getMessage());
            return ResponseEntity.status(500).body("Error interno al procesar la amistad.");
        }
    }
}