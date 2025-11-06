package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioChat {

    private final SimpMessagingTemplate messagingTemplate;
    private final RepositorioChat repositorioChat;
    private final ServicioUsuario servicioUsuario;

    @Autowired
    public ServicioChat(SimpMessagingTemplate messagingTemplate, RepositorioChat repositorioChat, ServicioUsuario servicioUsuario) {
        this.messagingTemplate = messagingTemplate;
        this.repositorioChat = repositorioChat;
        this.servicioUsuario = servicioUsuario;
    }

    @Transactional
    public void enviarMensaje(ChatMessage mensaje) {
        if (mensaje.getTimestamp() == null) {
            mensaje.setTimestamp(LocalDateTime.now());
        }

        try {
            // Si no viene fromName, intentar recuperarlo del servicio de usuarios
            if (mensaje.getFromName() == null && mensaje.getFromUserId() != null) {
                try {
                    var u = servicioUsuario.buscarPorId(mensaje.getFromUserId());
                    if (u != null) mensaje.setFromName(u.getNombre() + (u.getApellido() != null ? (" " + u.getApellido()) : ""));
                } catch (Exception e) {
                    // no crítico: si falla, dejamos el fromName nulo
                    System.err.println("ServicioChat: no se pudo recuperar nombre de usuario: " + e.getMessage());
                }
            }

            if (repositorioChat != null) {
                repositorioChat.guardar(mensaje);
                System.out.println("ServicioChat: mensaje persistido from=" + mensaje.getFromUserId() + " to=" + mensaje.getToUserId() + " id=" + mensaje.getId());
            } else {
                System.out.println("ServicioChat: repositorioChat es null, no se persiste");
            }

            // Enviar la versión persistida al receptor y al emisor
            final String destinoReceptor = "/topic/chat/" + mensaje.getToUserId();
            System.out.println("ServicioChat: enviando mensaje al destino " + destinoReceptor + " payload=" + mensaje.getContent());
            messagingTemplate.convertAndSend(destinoReceptor, mensaje);

            final String destinoEmisor = "/topic/chat/" + mensaje.getFromUserId();
            if (!mensaje.getFromUserId().equals(mensaje.getToUserId())) {
                System.out.println("ServicioChat: enviando mensaje al emisor " + destinoEmisor);
                messagingTemplate.convertAndSend(destinoEmisor, mensaje);
            }
        } catch (Exception e) {
            System.err.println("ServicioChat: error en enviarMensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> obtenerConversacion(Long userA, Long userB) {
        if (repositorioChat != null) {
            return repositorioChat.obtenerConversacion(userA, userB);
        }
        return List.of();
    }
}