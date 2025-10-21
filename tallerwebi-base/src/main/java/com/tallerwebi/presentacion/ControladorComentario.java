package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.PublicacionNoEncontrada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class ControladorComentario {
    private final ServicioComentario servicioComentario;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioLike servicioLike;
    private final NotificacionService notificacionService;
    private final PublicacionMapper publicacionMapper;

    @Autowired
    public ControladorComentario(ServicioComentario servicioComentario, ServicioPublicacion servicioPublicacion, ServicioUsuario servicioUsuario, ServicioLike servicioLike, NotificacionService notificacionService, PublicacionMapper publicacionMapper) {
        this.servicioComentario = servicioComentario;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioLike = servicioLike;
        this.notificacionService = notificacionService;
        this.publicacionMapper = publicacionMapper;


    }

    @PostMapping("/publicacion/comentar/{id}")
    @ResponseBody
    public Map<String, Object> comentarPublicacion(
            @PathVariable long id,
            @RequestBody DatosComentario dto,
            HttpServletRequest request
    ) throws PublicacionNoEncontrada {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Usuario usuario = servicioUsuario.buscarPorId(datos.getId());

        String texto = dto.getTexto();
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Comentario vacío");
        }

        Publicacion publicacion = servicioPublicacion.obtenerPublicacion(id);
        if (publicacion == null) {
            throw new PublicacionNoEncontrada("Publicación no encontrada");
        }

        Comentario comentario = servicioComentario.comentar(dto, usuario, publicacion);

        DatosComentario comentarioDTO = publicacionMapper.toComentarioDto(comentario);
        int cantidadLikes = servicioLike.contarLikes(publicacion.getId());
        int cantidadComentarios = servicioComentario.contarComentarios(publicacion.getId());

        notificacionService.enviarMensaje("/topic/publicacion/" + publicacion.getId(), "comentarioNuevo");

        Map<String, Object> response = new HashMap<>();
        response.put("comentario", comentarioDTO);
        response.put("cantidadComentarios", cantidadComentarios);
        response.put("cantidadLikes", cantidadLikes);
        return response;
    } //manejar los if en otros lados.


    @GetMapping("/publicacion/comentarios/{id}")
    @ResponseBody
    public Map<String, Object> obtenerComentarios(@PathVariable long id) {
        List<Comentario> lista = servicioComentario.encontrarComentariosPorId(id);

        // Usamos el mapper para convertir cada Comentario a DatosComentario
        List<DatosComentario> comentariosDTO = lista.stream()
                .map(publicacionMapper::toComentarioDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("comentarios", comentariosDTO);

        return response;
    }



    /*ACA VA ELIMINAR COMENTARIO **/
    //@PostMapping ("/comentario/{id})
    //ACORDARSE DE ELIMINARLO DE PUBLICACION AL HACER LOGICA DE ELIMINAR, REFERENCIA LIKE//
}