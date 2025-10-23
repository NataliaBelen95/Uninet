package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DatosPublicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
@Service
public class NotificacionService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificacionService(@Qualifier("brokerMessagingTemplate") SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Método para enviar mensaje a un topic
    public void enviarMensaje(String destino, Object mensaje) {
        messagingTemplate.convertAndSend(destino, mensaje);
    }

    public void enviarMensajePubli(String destino, DatosPublicacion dtoPublicacion) {
        messagingTemplate.convertAndSend(destino, dtoPublicacion);
    }
}
