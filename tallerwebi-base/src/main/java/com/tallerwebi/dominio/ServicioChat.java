package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServicioChat {

    private final SimpMessagingTemplate messagingTemplate;
    private final RepositorioChat repositorioChat;

    @Autowired
    public ServicioChat(SimpMessagingTemplate messagingTemplate, RepositorioChat repositorioChat) {
        this.messagingTemplate = messagingTemplate;
        this.repositorioChat = repositorioChat;
    }

    @Transactional
    public void enviarMensaje(ChatMessage mensaje) {
        if (mensaje.getTimestamp() == null) {
            mensaje.setTimestamp(LocalDateTime.now());
        }

        try {
            if (repositorioChat != null) {
                repositorioChat.guardar(mensaje);
                System.out.println("ServicioChat: mensaje persistido from=" + mensaje.getFromUserId() + " to=" + mensaje.getToUserId() + " id=" + mensaje.getId());
            } else {
                System.out.println("ServicioChat: repositorioChat es null, no se persiste");
            }

            String destino = "/topic/chat/" + mensaje.getToUserId();
            System.out.println("ServicioChat: enviando mensaje al destino " + destino + " payload=" + mensaje.getContent());
            messagingTemplate.convertAndSend(destino, mensaje);
        } catch (Exception e) {
            System.err.println("ServicioChat: error en enviarMensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<ChatMessage> obtenerConversacion(Long userA, Long userB) {
        if (repositorioChat != null) {
            return repositorioChat.obtenerConversacion(userA, userB);
        }
        return List.of();
    }
}