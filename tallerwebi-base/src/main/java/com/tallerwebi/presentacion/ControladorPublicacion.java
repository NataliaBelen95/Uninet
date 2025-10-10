package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
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


    @Autowired
    public ControladorPublicacion(ServicioPublicacion servicioPublicacion,
                                  ServicioLike servicioLike,
                                  ServicioUsuario servicioUsuario, ServicioComentario servicioComentario,
                                  PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.servicioUsuario = servicioUsuario;
        this.servicioComentario = servicioComentario;

    }


    @RequestMapping(path = "/publicaciones", method = RequestMethod.POST)
    public ModelAndView agregarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) throws PublicacionFallida {


        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");//     if (datosUsuario != null) {
        if(datosUsuario!=null) {
            try {
                Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId()); // ✅ usa ServicioLogin
                publicacion.setUsuario(usuario);
                servicioPublicacion.realizar(publicacion);
            } catch (PublicacionFallida e) {

                redirectAttributes.addFlashAttribute("errorPubli", e.getMessage());

            }
        }

            return new ModelAndView("redirect:/home");
        }






    @PostMapping("/publicacion/darLike/{id}")
    public ModelAndView darLike(@PathVariable Long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            Publicacion publicacion = servicioPublicacion.obtenerPublicacionPorId(id);

            if (publicacion != null) {
                if (servicioLike.yaDioLike(usuario, publicacion)) {

                    Like like = servicioLike.obtenerLike(usuario, publicacion);
                    if (like != null) {
                        servicioLike.quitarLike(like.getId());
                    }
                } else {
                    servicioLike.darLike(usuario, publicacion);
                }
            }
        }

        return new ModelAndView("redirect:/home");
    }


    @PostMapping("/publicacion/comentar/{id}")
    public ModelAndView comentar(@PathVariable Long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");


        if (datos != null) {
            String textoComentario = request.getParameter("texto"); // <-- aquí obtienes el texto del formulario

            if (textoComentario != null && !textoComentario.trim().isEmpty()) {
                Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
                Publicacion publicacion = servicioPublicacion.obtenerPublicacionPorId(id);

                if (publicacion != null) {
                    // Llama al servicio pasando el texto
                    servicioComentario.comentar(textoComentario, usuario, publicacion);
                }
            }
        }



        return new ModelAndView("redirect:/home");
    }
}



//@ResponseBody
//public Map<String, Object> agregarPublicacionAjax(@ModelAttribute Publicacion publicacion,
//                                                  HttpServletRequest request) {
//    Map<String, Object> respuesta = new HashMap<>();
//    DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
//
//    if (datosUsuario != null) {
//        try {
//            Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
//            publicacion.setUsuario(usuario);
//            servicioPublicacion.realizar(publicacion);
//
//            // Lista actualizada de publicaciones
//            List<Publicacion> publicaciones = servicioPublicacion.findAll();
//            List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
//            for (Publicacion p : publicaciones) {
//                datosPublicaciones.add(publicacionMapper.toDto(p));
//            }
//
//            respuesta.put("exito", true);
//            respuesta.put("publicaciones", datosPublicaciones);
//        } catch (PublicacionFallida e) {
//            respuesta.put("exito", false);
//            respuesta.put("error", e.getMessage());
//        }
//    } else {
//        respuesta.put("exito", false);
//        respuesta.put("error", "Usuario no logueado");
//    }
//
//    return respuesta;
//}