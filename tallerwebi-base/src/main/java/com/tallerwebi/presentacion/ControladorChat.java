package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ChatMessage;
import com.tallerwebi.dominio.ServicioChat;
import com.tallerwebi.dominio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorChat {

    private final ServicioChat servicioChat;
    private final ServicioUsuario servicioUsuario;

    @Autowired
    public ControladorChat(ServicioChat servicioChat, ServicioUsuario servicioUsuario) {
        this.servicioChat = servicioChat;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/chat")
    public String verChat(HttpServletRequest request, Model model) {
        var datos = (com.tallerwebi.presentacion.DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", datos);
        model.addAttribute("usuariosNuevos", servicioUsuario.mostrarTodos().stream()
                .filter(u -> !java.util.Objects.equals(u.getId(), datos.getId()))
                .map(u -> {
                    com.tallerwebi.presentacion.DatosUsuario du = new com.tallerwebi.presentacion.DatosUsuario();
                    du.setId(u.getId());
                    du.setNombre(u.getNombre());
                    du.setApellido(u.getApellido());
                    du.setFotoPerfil(u.getFotoPerfil());
                    return du;
                }).collect(java.util.stream.Collectors.toList()));
        return "chat";
    }

    @MessageMapping("/chat/enviar")
    public void recibirMensaje(ChatMessage mensaje) {
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
    public List<ChatMessage> conversacion(@RequestParam Long withUser, HttpServletRequest request) {
        var datos = (com.tallerwebi.presentacion.DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            System.out.println("ChatController.conversacion: usuario no logueado");
            return List.of();
        }
        Long mine = datos.getId();
        System.out.println("ChatController.conversacion: cargando conversacion entre " + mine + " y " + withUser);
        return servicioChat.obtenerConversacion(mine, withUser);
    }
}