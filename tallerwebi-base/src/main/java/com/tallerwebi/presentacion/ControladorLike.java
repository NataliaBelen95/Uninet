package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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
    @Transactional
    public String darYQuitarLike(@PathVariable long id, Model model, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datos != null) {
            try {
                Usuario usuario = new Usuario();
                usuario.setId(datos.getId());
                Publicacion publicacion = servicioPublicacion.obtenerPublicacionPorId(id);

                if (publicacion != null) {
                    boolean yaDioLike = servicioLike.yaDioLike(datos.getId(), publicacion.getId());

                    if (yaDioLike) {
                        Like like = servicioLike.obtenerLike(usuario.getId(), publicacion.getId());
                        if (like != null) {
                            servicioLike.quitarLike(like.getId());
                        }
                    } else {
                        servicioLike.darLike(datos.getId(), publicacion.getId());
                    }

                    int cantLikes = servicioLike.contarLikes(publicacion.getId());
                    notificacionService.enviarMensaje("/topic/publicacion/" + publicacion.getId(), String.valueOf(cantLikes));

                    DatosPublicacion dto = publicacionMapper.toDto(publicacion, datos.getId());
                    dto.setDioLike(!yaDioLike);

                    model.addAttribute("dtopubli", dto);
                    model.addAttribute("cantLikes", cantLikes);

                    return "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, cantidadLikes=${cantLikes})";
                } else {
                    System.err.println("Publicación no encontrada.");
                    return "error";
                }
            } catch (Exception e) {
                System.err.println("EXCEPCIÓN DETECTADA EN darLikeFragment:");
                e.printStackTrace();  // <-- esto imprimirá el error real
                throw e;  // importante para que Spring lo marque como rollback si es necesario
            }
        }

        return "error";
    }


    }
