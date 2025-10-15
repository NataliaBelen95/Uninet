package com.tallerwebi.presentacion;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/hello")
    public void greeting(String message) {
        // Enviar el mensaje a todos los clientes suscritos a /topic/greetings
        messagingTemplate.convertAndSend("/topic/greetings", "Hola desde el servidor: " + message);
    }
}

