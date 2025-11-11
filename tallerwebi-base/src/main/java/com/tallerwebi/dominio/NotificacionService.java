package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
@Service
public class NotificacionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RepositorioNotificacion repositorioNotificacion;

    @Autowired
    public NotificacionService(@Qualifier("brokerMessagingTemplate") SimpMessagingTemplate messagingTemplate,  RepositorioNotificacion repositorioNotificacion) {
        this.messagingTemplate = messagingTemplate;
        this.repositorioNotificacion = repositorioNotificacion;
    }

    // Método para enviar mensaje a un topic
    public void enviarMensaje(String destino, Object mensaje) {
        messagingTemplate.convertAndSend(destino, mensaje);
    }

    public void enviarMensajePubli(String destino, DatosPublicacion dtoPublicacion) {
        messagingTemplate.convertAndSend(destino, dtoPublicacion);
    }


    // 1️⃣ Método para contar notificaciones no leídas de un usuario
    public int contarNoLeidas(Usuario usuario) {
        return repositorioNotificacion.contarPublisNoLeidasPorUsuario(usuario.getId());
    }
    public void enviarNotificacion(Usuario receptor, Notificacion notificacion) {
        // Guardás la notificación normalmente
        repositorioNotificacion.guardar(notificacion);

        // Contás las no leídas
        int cantidadNoLeidas = contarNoLeidas(receptor);

        // Enviás al WebSocket del usuario
        messagingTemplate.convertAndSend(
                "/topic/notificaciones-" + receptor.getId(),
                cantidadNoLeidas
        );

    }


    public void marcarLeidaYActualizarContador(Usuario usuario, Long notificacionId) {
        repositorioNotificacion.marcarComoLeida(notificacionId);

        int cantidadNoLeidas = contarNoLeidas(usuario);

        messagingTemplate.convertAndSend(
                "/topic/notificaciones-" + usuario.getId(),
                cantidadNoLeidas
        );
    }
}
