package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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

    @Autowired
    public ControladorComentario(ServicioComentario servicioComentario, ServicioPublicacion servicioPublicacion, ServicioUsuario servicioUsuario, ServicioLike servicioLike, NotificacionService notificacionService    ) {
        this.servicioComentario = servicioComentario;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioLike = servicioLike;
        this.notificacionService = notificacionService;


}

    @PostMapping("/publicacion/comentar/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> comentar(
            @PathVariable Long id,
            @RequestBody DatosComentario dto,
            HttpServletRequest request
    ) {
        // 1. Obtener usuario logueado desde sesión
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }

        Usuario usuario = servicioUsuario.buscarPorId(datos.getId());

        // 2. Validar el texto del comentario recibido por JSON
        String texto = dto.getTexto();
        if (texto == null || texto.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Comentario vacío"));
        }

        // 3. Verificar publicación
        Publicacion publicacion = servicioPublicacion.obtenerPublicacion(id);
        if (publicacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Publicación no encontrada"));
        }

        try {
            // 4. Guardar comentario
            Comentario comentario = servicioComentario.comentar(texto, usuario, publicacion);

            // 5. Armar DTO de respuesta
            DatosComentario comentarioDTO = new DatosComentario();
            comentarioDTO.setTexto(comentario.getTexto());
            comentarioDTO.setNombreUsuario(usuario.getNombre());
            comentarioDTO.setApellidoUsuario(usuario.getApellido());

            int cantidadLikes = servicioLike.contarLikes(publicacion.getId());
            int cantidadComentarios = servicioComentario.contarComentarios(publicacion.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("comentario", comentarioDTO);
            response.put("cantidadComentarios", cantidadComentarios);
            response.put("cantidadLikes", cantidadLikes);

            // 6. WebSocket
            notificacionService.enviarMensaje("/topic/publicacion/" + publicacion.getId(), "comentarioNuevo");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al agregar el comentario"));
        }
    }



@GetMapping("/publicacion/comentarios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerComentarios(@PathVariable Long id) {
        List<Comentario> lista = servicioComentario.encontrarComentariosPorId(id);

    List<DatosComentario> comentariosDTO = lista.stream().map(comentario -> {
        DatosComentario dto = new DatosComentario();
        dto.setTexto(comentario.getTexto());
        dto.setNombreUsuario(comentario.getUsuario().getNombre());
        dto.setApellidoUsuario(comentario.getUsuario().getApellido());
        return dto;
    }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("comentarios", comentariosDTO);

        return ResponseEntity.ok(response);
    }

    /*ACA VA ELIMINAR COMENTARIO **/
    //@PostMapping ("/comentario/{id})
    //ACORDARSE DE ELIMINARLO DE PUBLICACION AL HACER LOGICA DE ELIMINAR, REFERENCIA LIKE//

}
