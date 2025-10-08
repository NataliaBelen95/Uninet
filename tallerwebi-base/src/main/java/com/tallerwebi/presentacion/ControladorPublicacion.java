package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorPublicacion {

    private final ServicioPublicado servicioPublicado;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final ServicioComentario servicioComentario;

    @Autowired
    public ControladorPublicacion(ServicioPublicado servicioPublicado,
                                  ServicioLike servicioLike,
                                  ServicioUsuario servicioUsuario, ServicioComentario servicioComentario) {
        this.servicioPublicado = servicioPublicado;
        this.servicioLike = servicioLike;
        this.servicioUsuario = servicioUsuario;
        this.servicioComentario = servicioComentario;
    }

    @RequestMapping(path = "/publicaciones", method = RequestMethod.POST)
    public ModelAndView agregarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion,
                                           HttpServletRequest request) throws PublicacionFallida {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datosUsuario != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId()); // ✅ usa ServicioLogin
            publicacion.setUsuario(usuario);
            servicioPublicado.realizar(publicacion);

        }

        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/publicacion/darLike/{id}")
    public ModelAndView darLike(@PathVariable Long id, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datos != null) {
            Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
            Publicacion publicacion = servicioPublicado.obtenerPublicacionPorId(id);

            if (publicacion != null) {
                servicioLike.darLike(usuario, publicacion);


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
                Publicacion publicacion = servicioPublicado.obtenerPublicacionPorId(id);

                if (publicacion != null) {
                    // Llama al servicio pasando el texto
                    servicioComentario.comentar(textoComentario, usuario, publicacion);
                }
            }
        }

        return new ModelAndView("redirect:/home"); // o redirigir de vuelta a la publicación
    }
}
