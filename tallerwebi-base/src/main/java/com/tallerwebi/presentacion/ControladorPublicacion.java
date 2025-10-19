package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Controller
public class ControladorPublicacion {

    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final PublicacionMapper publicacionMapper;
    private final NotificacionService notificacionService;
    private final ServicioComentario servicioComentario;


    @Autowired
    public ControladorPublicacion(ServicioPublicacion servicioPublicacion,
                                  ServicioLike servicioLike,
                                  ServicioUsuario servicioUsuario,
                                  PublicacionMapper publicacionMapper, NotificacionService notificacionService,
                                  ServicioComentario servicioComentario) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.servicioUsuario = servicioUsuario;
        this.publicacionMapper = publicacionMapper;
        this.notificacionService = notificacionService;
        this.servicioComentario = servicioComentario;


    }
    @PostMapping("/publicaciones")
    public ModelAndView agregarPublicacion(@RequestParam("descripcion") String descripcion,
                                           @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                           @RequestParam(value = "archivoNombre", required = false) String archivoNombre,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) throws PublicacionFallida, IOException {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
            Publicacion publicacion = new Publicacion();
            publicacion.setDescripcion(descripcion);

            if (archivo != null && !archivo.isEmpty()) {
                // Caso: usuario sube archivo desde disco
                servicioPublicacion.realizar(publicacion, usuario, archivo);
            } else if (archivoNombre != null && !archivoNombre.isEmpty()) {
                // Caso: usuario publicó un resumen generado que ya está en el servidor
                File archivoEnServidor = new File(System.getProperty("user.dir") + "/archivos_pdf/" + archivoNombre);
                if (!archivoEnServidor.exists()) {
                    throw new PublicacionFallida("El archivo especificado no existe en el servidor");
                }
                servicioPublicacion.realizar(publicacion, usuario, archivoEnServidor);
            } else {
                // Caso: sólo texto, sin archivo
                servicioPublicacion.realizar(publicacion, usuario, (MultipartFile) null);
            }

            DatosPublicacion dto = publicacionMapper.toDto(publicacion, datosUsuario.getId());
            notificacionService.enviarMensajePubli("/topic/publicaciones", dto);

            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/perfil/")) {
                return new ModelAndView("redirect:/perfil/" + usuario.getId());
            } else {
                return new ModelAndView("redirect:/home");
            }

        } catch (PublicacionFallida e) {
            redirectAttributes.addFlashAttribute("errorPubli", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:/home");
    }



    @PostMapping("/publicacion/eliminar/{id}")
    public ModelAndView eliminar(@PathVariable long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Publicacion publicacionAEliminar = servicioPublicacion.obtenerPublicacion(id);

        if (datos != null && publicacionAEliminar != null) {
            try {
                if (publicacionAEliminar.getUsuario().getId() == (datos.getId())) {
                    servicioPublicacion.eliminarPublicacionEntera(publicacionAEliminar);
                    System.out.println("ID usuario logueado: " + datos.getId());
                    System.out.println("ID usuario dueño de la publicación: " + publicacionAEliminar.getUsuario().getId());

                    // Después de eliminar, redirigir a la página de la publicación (ver si se puede poner mi perfil despues)
                    return new ModelAndView("redirect:/home");
                } else {
                    return new ModelAndView("error").addObject("mensaje", "No tienes permisos para eliminar esta publicación.");
                }
            } catch (NoSeEncuentraPublicacion e) {
                return new ModelAndView("error").addObject("mensaje", "Hubo un error al intentar eliminar la publicación.");
            }
        }

        return new ModelAndView("error").addObject("mensaje", "Publicación o usuario no encontrado.");
    }

    /*actualizacion tarjeta con datos nuevos*/
    @GetMapping("/publicacion/tarjeta/{id}")
    //@Transactional
    public String obtenerTarjetaPublicacion(@PathVariable long id, Model model, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        // Obtener la publicación
        Publicacion publicacion = servicioPublicacion.obtenerPublicacion(id);

        // Inicializar explícitamente las relaciones perezosas
        if (publicacion != null && publicacion.getUsuario() != null) {
            Hibernate.initialize(publicacion.getUsuario()); // Inicializa el usuario de la publicación
        }

        DatosPublicacion dtopubli = publicacionMapper.toDto(publicacion, datos.getId());

        model.addAttribute("dtopubli", dtopubli);

        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            model.addAttribute("usuario", usuario); // necesario para mostrar el botón de eliminar
        }

        model.addAttribute("comentarios", servicioPublicacion.obtenerComentariosDePublicacion(publicacion.getId()));
        model.addAttribute("cantLikes", servicioLike.contarLikes(publicacion.getId()));
        model.addAttribute("cantComentarios", servicioComentario.contarComentarios(publicacion.getId()));

        return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, comentarios=${comentarios}, likes=${cantLikes}, cantComentarios=${cantComentarios}, usuario=${usuario})";
    }


}



