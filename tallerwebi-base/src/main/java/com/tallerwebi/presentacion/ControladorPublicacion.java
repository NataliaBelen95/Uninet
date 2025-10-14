package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorPublicacion {

    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final ServicioComentario servicioComentario;
    private final PublicacionMapper publicacionMapper;


    @Autowired

    public ControladorPublicacion(ServicioPublicacion servicioPublicacion,
                                  ServicioLike servicioLike,
                                  ServicioUsuario servicioUsuario, ServicioComentario servicioComentario,
                                  PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.servicioUsuario = servicioUsuario;
        this.servicioComentario = servicioComentario;
        this.publicacionMapper = publicacionMapper;


    }

    @PostMapping("/publicaciones")
    public ModelAndView agregarPublicacion(
            @RequestParam("descripcion") String descripcion,
            @RequestParam("archivos") MultipartFile archivo,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) throws PublicacionFallida {

        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datosUsuario != null) {
            try {
                Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
                Publicacion publicacion = new Publicacion();
                publicacion.setDescripcion(descripcion);
                servicioPublicacion.realizar(publicacion, usuario, archivo);
            } catch (PublicacionFallida e) {
                redirectAttributes.addFlashAttribute("errorPubli", e.getMessage());
            }
        }

        return new ModelAndView("redirect:/home");
    }


//
//
//    @PostMapping("/publicacion/darLike/{id}")
//    public ModelAndView darLike(@PathVariable Long id, HttpServletRequest request) {
//        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
//
//        if (datos != null) {
//            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
//            Publicacion publicacion = servicioPublicacion.obtenerPublicacionPorId(id);
//
//            if (publicacion != null) {
//                if (servicioLike.yaDioLike(usuario, publicacion)) {
//
//                    Like like = servicioLike.obtenerLike(usuario, publicacion);
//                    if (like != null) {
//                        servicioLike.quitarLike(like.getId());
//                    }
//                } else {
//                    servicioLike.darLike(usuario, publicacion);
//                }
//            }
//        }
//
//        return new ModelAndView("redirect:/home");
//    }
@PostMapping("/publicacion/darLike/{id}")
@Transactional
public String darLikeFragment(@PathVariable Long id, Model model, HttpServletRequest request) {
    // Obtener el usuario logueado desde la sesión
    DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

    if (datos != null) {
        // Buscar el usuario y la publicación
        Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
        Publicacion publicacion = servicioPublicacion.obtenerPublicacionPorId(id);

        if (publicacion != null) {
            // Si el usuario ya dio like, lo quita
            if (servicioLike.yaDioLike(usuario, publicacion)) {
                Like like = servicioLike.obtenerLike(usuario, publicacion);
                if (like != null) {
                    servicioLike.quitarLike(like.getId());
                }
            } else {
                // Si el usuario no dio like, lo da
                servicioLike.darLike(usuario, publicacion);
            }

            // Convertir la publicación a DTO para actualizar la vista
            DatosPublicacion dto = publicacionMapper.toDto(publicacion);
            model.addAttribute("dtopubli", dto);
            model.addAttribute("usuario", usuario);  // Agregar usuario al modelo
        }
    }

    // Retornar solo el fragmento actualizado de la publicación
    return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli})";
}
    @PostMapping("/publicacion/comentar/{id}")
    @Transactional
    public String comentar(@PathVariable Long id, HttpServletRequest request, Model model) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            String textoComentario = request.getParameter("texto");
            Publicacion publicacion = servicioPublicacion.obtenerPublicacionConComentarios(id);

            if (publicacion != null && textoComentario != null && !textoComentario.trim().isEmpty()) {
                servicioComentario.comentar(textoComentario, usuario, publicacion);

                // Traer nuevamente la publicación actualizada con comentarios
                publicacion = servicioPublicacion.obtenerPublicacionConComentarios(id);

                // Mapear y pasar los datos a la vista
                DatosPublicacion dtopubli = publicacionMapper.toDto(publicacion);
                List<Comentario> comentarios = servicioComentario.encontrarComentariosPorId(id);

                model.addAttribute("dtopubli", dtopubli);
                model.addAttribute("comentarios", comentarios);

                // Devolver el fragmento Thymeleaf actualizado (comentarios + publicación)
                return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli})";
            }
        }
        return "redirect:/login";
    }


    @PostMapping("/publicacion/eliminar/{id}")
    public ModelAndView eliminar(@PathVariable Long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Publicacion publicacionAEliminar = servicioPublicacion.obtenerPublicacionPorId(id);

        if (datos != null && publicacionAEliminar != null) {
            try {
                if (publicacionAEliminar.getUsuario().getId().equals(datos.getId())) {
                    servicioPublicacion.eliminarPublicacionEntera(publicacionAEliminar);
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

}



