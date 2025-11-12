// Reemplaza los métodos enviar existentes por este método que acepta JSON y form-urlencoded
package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/amistad")
public class ControladorAmistad {

    private final ServicioAmistad servicioAmistad;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioUsuario servicioUsuario;
    private final UsuarioMapper usuarioMapper;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ControladorAmistad(ServicioAmistad servicioAmistad,
                              ServicioNotificacion servicioNotificacion,
                              RepositorioUsuario repositorioUsuario,
                              ServicioUsuario servicioUsuario,
                              UsuarioMapper usuarioMapper) {
        this.servicioAmistad = servicioAmistad;
        this.servicioNotificacion = servicioNotificacion;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioUsuario = servicioUsuario;
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Endpoint unificado para enviar una solicitud de amistad.
     * Soporta:
     *  - Form POST (application/x-www-form-urlencoded) con param receptorId
     *  - JSON POST (application/json) con body { "receptorId": 123 }
     *
     * Si la llamada viene de un submit tradicional (form) devuelve redirect a /usuarios o /home.
     * Si la llamada viene por AJAX (fetch JSON) devuelve ResponseEntity OK/errores.
     */
    @PostMapping(value = "/enviar")
    public ResponseEntity<?> enviarSolicitudUnificado(
            @RequestParam(name = "receptorId", required = false) Long receptorIdParam,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest request) {

        // Extraer receptorId priorizando JSON body si existe
        Long receptorId = receptorIdParam;
        if (payload != null && payload.containsKey("receptorId")) {
            try {
                Object v = payload.get("receptorId");
                if (v instanceof Number) receptorId = ((Number) v).longValue();
                else receptorId = Long.parseLong(String.valueOf(v));
            } catch (Exception ignored) {}
        }

        // Validar sesión
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            // Si es AJAX (JSON) respondemos 401, si es form redirigimos a login
            if (isAjax(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/login").build();
        }

        if (receptorId == null) {
            if (isAjax(request)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("receptorId requerido");
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/usuarios").build();
        }

        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);
        if (solicitante == null || receptor == null) {
            if (isAjax(request)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuarios inválidos");
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/usuarios").build();
        }

        // Crear la solicitud y notificar (servicio devuelve la entidad)
        SolicitudAmistad solicitud = servicioAmistad.enviarSolicitud(solicitante, receptor);
        Long solicitudId = solicitud != null ? solicitud.getId() : null;
        try {
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);
        } catch (Exception e) {
            System.err.println("Error al notificar solicitud de amistad: " + e.getMessage());
        }

        // Respuesta: si es AJAX devolvemos 200 OK, si es form redirigimos
        if (isAjax(request)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/usuarios").build();
        }
    }

    // Helper simple para detectar llamadas AJAX/JSON
    private boolean isAjax(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String xrh = request.getHeader("X-Requested-With");
        if (xrh != null && xrh.equalsIgnoreCase("XMLHttpRequest")) return true;
        if (accept != null && accept.contains("application/json")) return true;
        if (contentType != null && contentType.contains("application/json")) return true;
        return false;
    }

    // ... deja los demás mappings (aceptar/rechazar por ids, etc.) tal como los tenías ...
}