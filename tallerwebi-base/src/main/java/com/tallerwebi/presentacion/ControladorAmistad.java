package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosAmigos;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/amistad")
public class ControladorAmistad {

    private final ServicioAmistad servicioAmistad;
    private final ServicioNotificacion servicioNotificacion;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioUsuario servicioUsuario;
    private final UsuarioMapper usuarioMapper;

    @Autowired
    public ControladorAmistad(ServicioAmistad servicioAmistad,ServicioNotificacion servicioNotificacion , RepositorioUsuario repositorioUsuario, ServicioUsuario servicioUsuario, UsuarioMapper usuarioMapper) {
        this.servicioAmistad = servicioAmistad;
        this.usuarioMapper = usuarioMapper;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }






    @PostMapping("/enviar/{idReceptor}")
    public String enviarSolicitud(@PathVariable Long idReceptor, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(idReceptor);

        servicioAmistad.enviarSolicitud(solicitante, receptor);
        return "redirect:/usuarios";
    }

    @PostMapping("/aceptar/{idSolicitud}")
    public String aceptarSolicitud(@PathVariable Long idSolicitud) {
        servicioAmistad.aceptarSolicitud(idSolicitud);
        return "redirect:/amistad/solicitudes";
    }

    @PostMapping("/rechazar/{idSolicitud}")
    public String rechazarSolicitud(@PathVariable Long idSolicitud) {
        servicioAmistad.rechazarSolicitud(idSolicitud);
        return "redirect:/amistad/solicitudes";
    }

    @GetMapping("/solicitudes")
    public String listarSolicitudesPendientes(HttpServletRequest request, ModelMap model) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Usuario usuario = servicioUsuario.buscarPorId(datos.getId());

        model.put("solicitudes", servicioAmistad.listarSolicitudesPendientes(usuario));
        return "solicitudes-amistad";
    }

    @GetMapping("/amigos")
    public String listarAmigos(HttpServletRequest request, ModelMap model) {
        // 1. Obtener el DTO de sesión
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }

        // 2. Obtener entidad para la lógica de negocio
        Usuario usuario = repositorioUsuario.buscarPorId(datos.getId());

        // 3. Lógica de amigos
        List<Usuario> amigos = servicioAmistad.listarAmigos(usuario);
        List<DatosAmigos> amigosDTO = amigos.stream()
                .map(a -> new DatosAmigos(a.getId(), a.getNombre(), a.getApellido(), a.getFotoPerfil()))
                .collect(Collectors.toList());


        // 5. Guardar DTO actualizado en sesión
        request.getSession().setAttribute("usuarioLogueado", datos);

        // 6. Pasar todo a la vista
        model.put("usuario", datos); //para seguir con los datos de la logica del nav
        model.put("amigos", amigosDTO);
        model.put("esPropio", true);

        return "lista-amigos";
    }

    @PostMapping("/enviar")
    public String enviarSolicitudForm(@RequestParam("receptorId") Long receptorId, HttpServletRequest request) {
        // 1) obtener datos de sesión
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }

        // 2) buscar entidades
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);
        if (solicitante == null || receptor == null) {
            // manejo simple de error: redirigir a la lista de usuarios
            return "redirect:/usuarios";
        }

        // 3) enviar la solicitud (persistir la solicitud de amistad)
        servicioAmistad.enviarSolicitud(solicitante, receptor);

        // 4) crear notificación persistente y enviar en tiempo real
        // Delegamos la creación y envío a ServicioNotificacion:
        try {
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD);
        } catch (Exception e) {
            // en caso de fallo en la notificación no interrumpimos el flujo; opcional: loguear
            System.err.println("Error al notificar solicitud de amistad: " + e.getMessage());
        }

        return "redirect:/home";
    }
}
