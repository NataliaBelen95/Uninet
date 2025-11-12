package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosAmigos;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
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
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);

        // Asumiendo que el resto de las verificaciones de nullidad ya pasaron

        try {

            SolicitudAmistad solicitudCreada = servicioAmistad.enviarSolicitud(solicitante, receptor);
            Long solicitudId = solicitudCreada.getId();

            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);

        } catch (IllegalStateException e) {
            //  CAPTURAR LA VALIDACIÓN DE NEGOCIO Y REDIRIGIR AMIGABLEMENTE
            String mensajeCodificado = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            System.err.println("Error de validación de amistad: " + e.getMessage());
            return "redirect:/home?error=" + mensajeCodificado;

        } catch (Exception e) {
            // Capturar otros errores como fallo de notificación o DB.
            System.err.println("Error general al enviar solicitud: " + e.getMessage());
            return "redirect:/usuarios?error=Error desconocido al enviar solicitud.";
        }

        // Redirección de éxito
        return "redirect:/home";
    }

    @PostMapping("/aceptar/{idSolicitud}")
    public String aceptarSolicitud(@PathVariable Long idSolicitud) { // Cambia ResponseEntity<String> a String

        boolean exito = servicioAmistad.aceptarSolicitud(idSolicitud);

        if (exito) {

            return "redirect:/notificaciones?tab=solicitudes";
        } else {

            // Podrías redirigir a una página de error o simplemente de vuelta a la lista:
            return "redirect:/notificaciones?tab=solicitudes&error=solicitudInvalida";
        }
    }
    @GetMapping("/amigos")
    public String listarAmigos(HttpServletRequest request, ModelMap model) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }
        Usuario usuario = repositorioUsuario.buscarPorId(datos.getId());

        List<Usuario> amigos = servicioAmistad.listarAmigos(usuario);
        List<DatosAmigos> amigosDTO = amigos.stream()
                .map(a -> new DatosAmigos(a.getId(), a.getNombre(), a.getApellido(), a.getFotoPerfil()))
                .collect(Collectors.toList());

        request.getSession().setAttribute("usuarioLogueado", datos);

        model.put("usuario", datos);
        model.put("amigos", amigosDTO);
        model.put("esPropio", true);

        return "lista-amigos";
    }
}