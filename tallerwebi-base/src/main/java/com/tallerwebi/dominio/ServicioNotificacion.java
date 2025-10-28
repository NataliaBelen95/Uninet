package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DatosUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class ServicioNotificacion {

    @Autowired
    private RepositorioNotificacion repoNotificacion;
    @Autowired
    private NotificacionService notificacionService;
    public void crear(Usuario receptor, Usuario emisor, Publicacion publicacion, TipoNotificacion tipo) {
        String mensaje;
        if (tipo == TipoNotificacion.INACTIVIDAD) {
            mensaje = generarMensajeInactividad(receptor);
        } else {
            mensaje = generarMensaje(tipo, emisor, publicacion, receptor);
        }

        if (mensaje == null || mensaje.isEmpty()) {
            return; // no se crea notificación
        }

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


    private String generarMensaje(TipoNotificacion tipo, Usuario emisor, Publicacion publicacion, Usuario receptor) {
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
                return generarMensajeInactividad(receptor);
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
        boolean receptorEsDuenio = receptor != null
                && publicacion != null
                && receptor.getId() == (publicacion.getUsuario().getId());

        switch (tipo) {
            case COMENTARIO:
            case LIKE:
                if (receptorEsDuenio) {
                    // si la publicación es mía → voy a miPerfil
                    return "/miPerfil#publicacion-" + publicacion.getId();
                } else {
                    // si la publicación es de otro → voy al perfil del dueño
                    return "/perfil/" + publicacion.getUsuario().getSlug() + "#publicacion-" + publicacion.getId();
                }

            case SOLICITUD_AMISTAD:
                // si alguien me envía solicitud → ver su perfil
                return "/perfil/" + emisor.getSlug();



            default:
                return "/notificaciones";
        }
    }


    private String generarMensajeInactividad(Usuario usuario) {
        LocalDate ultimaPub = usuario.getUltimaPublicacion();
        long diasInactivos = (ultimaPub != null)
                ? ChronoUnit.DAYS.between(ultimaPub, LocalDate.now())
                : 999; // si nunca publicó,  muchos días

        // Mensajes dinámicos según tiempo de inactividad
        if (diasInactivos < 7) {
            return null; // no enviar notificación si es reciente
        } else if (diasInactivos < 15) {
            return "¡Hola " + usuario.getNombre() + "! Hace " + diasInactivos +
                    " días que no compartís nada. ¡Animate a publicar algo!";
        } else if (diasInactivos < 30) {
            return "¡Extrañamos tus publicaciones, " + usuario.getNombre() + "! " +
                    "Hace " + diasInactivos + " días que no compartís nada. Revisá las publicaciones populares y sumate de nuevo.";
        } else {
            return "¡Hola " + usuario.getNombre() + "! Notamos que hace mucho tiempo que no publicás. " +
                    "¡Te invitamos a crear una nueva publicación ahora y mostrar lo que hacés!";
        }
    }


}
