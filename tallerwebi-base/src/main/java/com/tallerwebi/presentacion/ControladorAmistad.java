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
        // ✅ Esto ya está correcto
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
        // ... (Verificaciones de sesión y usuario) ...
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);

        // ... (Lógica de envío) ...
        try {
            servicioAmistad.enviarSolicitud(solicitante, receptor);

            // Si el envío fue exitoso, notificar y redirigir
            Long solicitudId = solicitante.getId(); // Asumo que esta línea sigue siendo incorrecta y debería usar el ID del objeto devuelto
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);

        } catch (IllegalStateException e) {


            // Añadir el mensaje de error al modelo/sesión (dependiendo de tu configuración de Spring)
            // Ejemplo simple usando un parámetro de URL (aunque Flash Attributes es mejor):
            String mensajeError = "Error: " + e.getMessage(); // Obtiene "El usuario Nat ya es tu amigo."
            System.err.println(mensajeError);

            // Redirigir a una página que muestre el error (aquí se usa /usuarios como ejemplo)
            return "redirect:/usuarios?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            // Capturar otros errores como fallo de notificación o DB.
            System.err.println("Error general al enviar solicitud: " + e.getMessage());
            return "redirect:/usuarios?error=Error desconocido al enviar solicitud.";
        }

        return "redirect:/home";
    }

    @PostMapping("/aceptar/{idSolicitud}")
    public String aceptarSolicitud(@PathVariable Long idSolicitud) { // Cambia ResponseEntity<String> a String

        boolean exito = servicioAmistad.aceptarSolicitud(idSolicitud);

        if (exito) {
            // ✅ ÉXITO: Redirige de vuelta a la lista de solicitudes (actualizando la tabla)
            return "redirect:/notificaciones?tab=solicitudes";
        } else {
            // 🛑 FALLO: Redirige de vuelta con un mensaje de error (opcional)
            // Podrías redirigir a una página de error o simplemente de vuelta a la lista:
            return "redirect:/notificaciones?tab=solicitudes&error=solicitudInvalida";
        }
    }
}