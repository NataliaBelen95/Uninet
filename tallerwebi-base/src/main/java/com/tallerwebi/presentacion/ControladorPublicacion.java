package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

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
                                           @RequestParam("archivos") MultipartFile archivo,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) throws PublicacionFallida {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        //System.out.println("datosUsuario: " + datosUsuario);

        if (datosUsuario != null) {
            try {
                // Obtener el usuario logueado
                Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
                Publicacion publicacion = new Publicacion();
                publicacion.setDescripcion(descripcion);
                servicioPublicacion.realizar(publicacion, usuario, archivo); // Crear la publicación
                DatosPublicacion dto = publicacionMapper.toDto(publicacion, datosUsuario.getId());

                // Enviar la notificación en tiempo real
                notificacionService.enviarMensajePubli("/topic/publicaciones", dto);

                // Obtener la URL de donde el usuario vino (referer)
                String referer = request.getHeader("Referer");

                // Si el referer contiene "/perfil/", redirige al perfil, sino a la home
                if (referer != null && referer.contains("/perfil/")) {
                    // Redirigir al perfil del usuario
                    return new ModelAndView("redirect:/perfil/" + usuario.getId());
                } else {
                    // Redirigir a la home
                    return new ModelAndView("redirect:/home");
                }

            } catch (PublicacionFallida e) {
                redirectAttributes.addFlashAttribute("errorPubli", e.getMessage());
            }
        }

        // Si no hay usuario o hubo algún error, redirigir a la home
        return new ModelAndView("redirect:/home");
    }



    @PostMapping("/publicacion/eliminar/{id}")
    public ModelAndView eliminar(@PathVariable long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Publicacion publicacionAEliminar = servicioPublicacion.obtenerPublicacionPorId(id);

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
        model.addAttribute("cantlikes", servicioLike.contarLikes(publicacion.getId()));
        model.addAttribute("cantComentarios", servicioComentario.contarComentarios(publicacion.getId()));

        return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, comentarios=${comentarios}, likes=${cantLikes}, cantComentarios=${cantComentarios}, usuario=${usuario})";
    }


}



