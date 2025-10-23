package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.List;  // Asegúrate de que esta línea esté presente

@Controller
@RequestMapping("/amistad")
public class ControladorAmistad {

    private final ServicioAmistad servicioAmistad;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioUsuario servicioUsuario;

    @Autowired
    public ControladorAmistad(ServicioAmistad servicioAmistad, RepositorioUsuario repositorioUsuario, ServicioUsuario servicioUsuario) {
        this.servicioAmistad = servicioAmistad;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioUsuario = servicioUsuario;
    }
    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {
        List<Usuario> usuarios = servicioUsuario.obtenerTodosLosUsuarios(); // Método que devuelve todos los usuarios
        model.addAttribute("usuarios", usuarios);
        return "usuarios"; // Vista Thymeleaf donde se renderiza la lista
    }


    @PostMapping("/enviar/{idReceptor}")
    public String enviarSolicitud(@PathVariable Long idReceptor, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Usuario solicitante = repositorioUsuario.buscarPorId(datos.getId());
        Usuario receptor = repositorioUsuario.buscarPorId(idReceptor);

        servicioAmistad.enviarSolicitud(solicitante, receptor);
        return "redirect:/usuarios"; // o donde se muestren los usuarios
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
        Usuario usuario = repositorioUsuario.buscarPorId(datos.getId());

        model.put("solicitudes", servicioAmistad.listarSolicitudesPendientes(usuario));
        return "solicitudes-amistad";
    }

    @GetMapping("/amigos")
    @Transactional
    public String listarAmigos(HttpServletRequest request, ModelMap model) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Usuario usuario = repositorioUsuario.buscarPorId(datos.getId());

        model.put("amigos", servicioAmistad.listarAmigos(usuario));
        return "lista-amigos";
    }
}
