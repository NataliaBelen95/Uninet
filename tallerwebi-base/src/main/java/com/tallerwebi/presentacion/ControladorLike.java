package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Controller
public class ControladorLike {
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final PublicacionMapper publicacionMapper;
    private final NotificacionService notificacionService;
    private final UsuarioMapper usuarioMapper;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
        public  ControladorLike(ServicioPublicacion servicioPublicacion,
                                      ServicioLike servicioLike,
                                      ServicioUsuario servicioUsuario,
                                      PublicacionMapper publicacionMapper, NotificacionService notificacionService,
                                      UsuarioMapper usuarioMapper,
                                      ServicioNotificacion servicioNotificacion) {
            this.servicioPublicacion = servicioPublicacion;
            this.servicioLike = servicioLike;
            this.servicioUsuario = servicioUsuario;
            this.publicacionMapper = publicacionMapper;
            this.notificacionService = notificacionService;
            this.usuarioMapper = usuarioMapper;
            this.servicioNotificacion = servicioNotificacion;


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

                Usuario receptor = publicacion.getUsuario();
                // ✅ Crear notificación solo si dio like (no si quitó)

                if (dto.getDioLike() && !Objects.equals(usuario.getId(), receptor.getId())) {
                    servicioNotificacion.crear(
                            receptor,
                            usuario,
                            publicacion,
                            TipoNotificacion.LIKE

                    );
                }


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
    @GetMapping("/mis-likes")
    public ModelAndView mostrarMisLikes(HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return new ModelAndView("redirect:/login");
        }

        // Traer todas las publicaciones donde el usuario dio like
        List<Publicacion> publicacionesConLike =
                servicioPublicacion.obtenerPorLikeDeUsuario(datos.getId());

        // Mapear a DTO
        List<DatosPublicacion> dtos = publicacionesConLike.stream()
                .map(p -> publicacionMapper.toDto(p, datos.getId()))
                .collect(Collectors.toList());

        ModelAndView mav = new ModelAndView("mis-likes");
        mav.addObject("publicaciones", dtos); // ojo, no usuario → Thymeleaf mostrará publicaciones
        mav.addObject("usuario", datos);
        return mav;
    }

    }
