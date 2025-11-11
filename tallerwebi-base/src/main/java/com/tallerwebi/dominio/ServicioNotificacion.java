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

    // Modifica el método crearAmistad para que incluya emisorId y receptorId en la URL
    public void crearAmistad(Usuario receptor, Usuario emisor, TipoNotificacion tipo, Long solicitudId) {
        String mensaje;
        if (tipo == TipoNotificacion.INACTIVIDAD) {
            mensaje = generarMensajeInactividad(receptor);
        } else {
            mensaje = generarMensajeAmigo(tipo, emisor, receptor);
        }

        if (mensaje == null || mensaje.isEmpty()) {
            return; // no se crea notificación
        }

        // dedupe simple: evitar crear notificación idéntica pendiente
        List<Notificacion> existentes = repoNotificacion.buscarPorReceptor(receptor.getId());
        boolean existeSimilar = existentes.stream()
                .anyMatch(n ->
                        n.getTipo() == tipo &&
                                n.getUsuarioEmisor() != null &&
                                emisor != null &&
                                java.util.Objects.equals(n.getUsuarioEmisor().getId(), emisor.getId()) &&
                                !n.isLeida() &&
                                n.getMensaje() != null &&
                                n.getMensaje().equals(mensaje)
                );
        if (existeSimilar) {
            // ya existe una notificación pendiente idéntica -> no crear otra
            return;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioReceptor(receptor);
        notificacion.setUsuarioEmisor(emisor);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(java.time.LocalDateTime.now());

        // generar URL base (por ejemplo el perfil del emisor) y anexar emisorId/receptorId/solicitudId
        String url = generarUrlAmistad(tipo, emisor, receptor); // mantén la lógica existente que apunta a perfil
        // añadir parámetros para que el frontend o controlador puedan leer los ids
        StringBuilder sb = new StringBuilder(url != null ? url : "/notificaciones");
        if (!sb.toString().contains("?")) sb.append("?");
        else if (!sb.toString().endsWith("&") && !sb.toString().endsWith("?")) sb.append("&");

        // anexar emisorId y receptorId siempre (si existen)
        if (emisor != null && emisor.getId() != null) sb.append("emisorId=").append(emisor.getId()).append("&");
        if (receptor != null && receptor.getId() != null) sb.append("receptorId=").append(receptor.getId()).append("&");
        // anexar solicitudId si la tuvieras (opcional)
        if (solicitudId != null) sb.append("solicitudId=").append(solicitudId).append("&");

        // quitar posible & final
        if (sb.charAt(sb.length()-1) == '&') sb.setLength(sb.length()-1);

        notificacion.setUrl(sb.toString());

        repoNotificacion.guardar(notificacion);
        notificacionService.enviarNotificacion(receptor, notificacion);
    }

    private String generarMensajeAmigo(TipoNotificacion tipo, Usuario emisor, Usuario receptor) {
        String preview = "Quiere ser tu amichi";
        switch (tipo) {
            case SOLICITUD_AMISTAD:
                return emisor.getNombre() + " te envió una solicitud de amistad.";
            default:
                return "Nueva notificación.";
        }
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
                // al hacer click en una solicitud: ir a notificaciones y abrir la pestaña 'solicitudes'
                return "/notificaciones?tab=solicitudes";

            default:
                return "/notificaciones";
        }
    }

    private String generarUrlAmistad(TipoNotificacion tipo, Usuario receptor, Usuario emisor) {
        boolean receptorEsDuenio = receptor != null
                && emisor != null;

        switch (tipo) {
            case SOLICITUD_AMISTAD:
                // Cuando recibes una solicitud de amistad queremos llevar al usuario a la pestaña de solicitudes
                return "/notificaciones?tab=solicitudes";
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