package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class ControladorLike {
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final PublicacionMapper publicacionMapper;
    private final NotificacionService notificacionService;

    @Autowired
        public  ControladorLike(ServicioPublicacion servicioPublicacion,
                                      ServicioLike servicioLike,
                                      ServicioUsuario servicioUsuario,
                                      PublicacionMapper publicacionMapper, NotificacionService notificacionService) {
            this.servicioPublicacion = servicioPublicacion;
            this.servicioLike = servicioLike;
            this.servicioUsuario = servicioUsuario;
            this.publicacionMapper = publicacionMapper;
            this.notificacionService = notificacionService;


        }

    @PostMapping("/publicacion/darLike/{id}")
    public String darYQuitarLike(@PathVariable long id, Model model, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datos != null) {
            try {                          //usuarioid, publiid
                servicioLike.toggleLike(datos.getId(), id);

                int cantLikes = servicioLike.contarLikes(id);
                notificacionService.enviarMensaje("/topic/publicacion/" + id, String.valueOf(cantLikes));

                Publicacion publicacion = servicioPublicacion.obtenerPublicacion(id);

                DatosPublicacion dto = publicacionMapper.toDto(publicacion, datos.getId());
                dto.setDioLike(servicioLike.yaDioLike(datos.getId(), id)); // actualizar el estado real
                Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
                model.addAttribute("dtopubli", dto);
                model.addAttribute("cantLikes", cantLikes);
                model.addAttribute("usuario", usuario);
                return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, usuario=${usuario})";
            } catch (Exception e) {
                System.err.println("EXCEPCIÓN DETECTADA EN darLikeFragment:");
                e.printStackTrace();
                throw e;
            }
        }
        return "error";
    }

    }
