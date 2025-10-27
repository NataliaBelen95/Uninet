package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServicioNotificacion {

    @Autowired
    private RepositorioNotificacion repoNotificacion;
    @Autowired
    private NotificacionService notificacionService;

    public void crear(Usuario receptor, Usuario emisor, Publicacion publicacion, TipoNotificacion tipo) {
        String mensaje = generarMensaje(tipo, emisor, publicacion);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioReceptor(receptor);
        notificacion.setUsuarioEmisor(emisor);
        notificacion.setPublicacion(publicacion);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(LocalDateTime.now());
        String url = generarUrl(tipo, publicacion, receptor, emisor);
        notificacion.setUrl(url);

        repoNotificacion.guardar(notificacion);
        notificacionService.enviarNotificacion(receptor, notificacion);
    }

    private String generarMensaje(TipoNotificacion tipo, Usuario emisor, Publicacion publicacion) {
        String preview = (publicacion != null && publicacion.getDescripcion() != null)
                ? " \"" + publicacion.getDescripcion().substring(0, Math.min(30, publicacion.getDescripcion().length())) + "...\""
                : "";

        switch (tipo) {
            case LIKE:
                return emisor.getNombre() + " le dio like a tu publicación" + preview;
            case COMENTARIO:
                return emisor.getNombre() + " comentó tu publicación" + preview;
            case SOLICITUD_AMISTAD:
                return emisor.getNombre() + " te envió una solicitud de amistad.";
            case INACTIVIDAD:
                return "¡Hace tiempo que no publicás nada! Volvé a participar 😄";
            case RECOMENDACION:
                return "Tenemos una publicación que podría interesarte.";
            default:
                return "Nueva notificación.";
        }
    }

    public List<Notificacion> obtenerPorUsuario(long receptorId) {
        return repoNotificacion.buscarPorReceptor(receptorId);
    }

    public void marcarLeida(long idNoti) {
        repoNotificacion.marcarComoLeida(idNoti);
    }

    public int contarNoLeidas(long usuId) {
        return repoNotificacion.contarPublisNoLeidasPorUsuario(usuId);
    }
    private String generarUrl(TipoNotificacion tipo, Publicacion publicacion, Usuario receptor, Usuario emisor) {
        switch (tipo) {
            case COMENTARIO:
                return "/perfil/" + receptor.getSlug() + "#publicacion-" + publicacion.getId();
            case LIKE:
                // La publicación pertenece al receptor, y tu ruta usa slug
                return "/perfil/" + receptor.getSlug() + "#publicacion-" + publicacion.getId();

            case SOLICITUD_AMISTAD:
                // Querés ir al perfil del emisor (quien envió la solicitud)
                return "/perfil/" + emisor.getSlug();

            default:
                return "/notificaciones";
        }
    }



}
