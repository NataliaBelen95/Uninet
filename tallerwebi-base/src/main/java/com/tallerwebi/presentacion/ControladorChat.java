package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosAmigos;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ControladorChat {

    private final ServicioChat servicioChat;
    private final ServicioUsuario servicioUsuario;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioAmistad servicioAmistad;

    @Autowired
    public ControladorChat(ServicioChat servicioChat, ServicioUsuario servicioUsuario, RepositorioUsuario repositorioUsuario,ServicioAmistad servicioAmistad) {
        this.servicioChat = servicioChat;
        this.servicioUsuario = servicioUsuario;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioAmistad = servicioAmistad;
    }

    @GetMapping("/chat")
    public String verChat(HttpServletRequest request, Model model) {
        // 1) Obtener DTO de sesión y validar
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }

        // 2) Poner usuario en el modelo (para nav / datos de la vista)
        model.addAttribute("usuario", datos);

        // 3) Cargar entidad Usuario desde BD
        Usuario usuario = servicioUsuario.buscarPorId(datos.getId());
        if (usuario == null) {
            // usuario no encontrado (caso raro) -> volver al login o a lista
            return "redirect:/login";
        }

        // 4) Obtener sólo amigos (lista de entidades Usuario)
        List<Usuario> amigos = servicioAmistad.listarAmigos(usuario);

        // 5) Convertir a DTO para la vista, evitando nulos y a mí mismo
        List<DatosAmigos> amigosDTO = amigos.stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != usuario.getId()) // no mostrarme a mí mismo
                .map(a -> new DatosAmigos(a.getId(), a.getNombre(), a.getApellido(), a.getFotoPerfil()))
                .collect(Collectors.toList());

        model.addAttribute("amigos", amigosDTO);
        model.addAttribute("esPropio", true);

        return "chat";
    }

    @MessageMapping("/chat/enviar")
    public void recibirMensaje(ChatMessage mensaje) {
        System.out.println("STOMP -> recibirMensaje (RAW): " + mensaje);
        System.out.println("STOMP -> recibirMensaje: from=" + mensaje.getFromUserId() + " to=" + mensaje.getToUserId() + " content=" + mensaje.getContent());
        servicioChat.enviarMensaje(mensaje);
    }

    @PostMapping("/chat/test/enviar")
    @ResponseBody
    public String testEnviar(@RequestBody ChatMessage mensaje) {
        servicioChat.enviarMensaje(mensaje);
        return "ok";
    }

    @GetMapping("/chat/conversacion")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> conversacion(@RequestParam Long withUser, HttpServletRequest request) {
        try {
            var datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
            if (datos == null) {
                System.out.println("ChatController.conversacion: usuario no logueado");
                return ResponseEntity.ok(List.of());
            }
            Long mine = datos.getId();
            System.out.println("ChatController.conversacion: cargando conversacion entre " + mine + " y " + withUser);
            List<ChatMessage> lista = servicioChat.obtenerConversacion(mine, withUser);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            System.err.println("ControladorChat.conversacion: error al obtener conversacion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }
}