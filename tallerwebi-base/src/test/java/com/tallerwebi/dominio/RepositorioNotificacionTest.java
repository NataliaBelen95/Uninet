package com.tallerwebi.dominio;

import com.tallerwebi.infraestructura.config.HibernateTestInfraesructuraConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
@Transactional
public class RepositorioNotificacionTest {

    @Autowired
    private RepositorioNotificacion repositorioNotificacion;
    @Autowired
    private SessionFactory sessionFactory; // Usado para persistir entidades base



    private Usuario crearUsuario(String nombre, String apellido, boolean esBot, int dni, String email) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEsBot(esBot);
        u.setDni(dni);
        u.setEmail(email);
        u.setPassword("password");
        return u;
    }

    private Notificacion crearNotificacion(Usuario receptor, Usuario emisor, TipoNotificacion tipo, boolean leida, LocalDateTime fecha, String mensaje) {
        Notificacion n = new Notificacion();
        n.setUsuarioReceptor(receptor);
        n.setUsuarioEmisor(emisor);
        n.setTipo(tipo);
        n.setLeida(leida);
        n.setFechaCreacion(fecha);
        n.setMensaje(mensaje);
        return n;
    }



    @Test
    public void poderBuscarNotificacionesPorReceptorID_YOrdenarlasPorFecha() {
        Usuario receptor = crearUsuario("Nat", "Ram", false, 12456789, "nat@test.com");
        Usuario emisor1 = crearUsuario("Emi", "Uno", false, 11111111, "emi1@test.com");
        Usuario emisor2 = crearUsuario("Emi", "Dos", false, 22222222, "emi2@test.com");
        sessionFactory.getCurrentSession().save(receptor);
        sessionFactory.getCurrentSession().save(emisor1);
        sessionFactory.getCurrentSession().save(emisor2);

        // 2. Preparación de Notificaciones (creadas fuera de orden)
        Notificacion nAntigua = crearNotificacion(receptor, emisor1, TipoNotificacion.LIKE, false, LocalDateTime.now().minusHours(2), "A Emisor1 le gustó tu publicación antigua.");
        Notificacion nReciente = crearNotificacion(receptor, emisor2, TipoNotificacion.COMENTARIO, true, LocalDateTime.now().minusHours(1), "Emisor2 comentó tu publicación reciente.");
        Notificacion nOtro = crearNotificacion(emisor1, emisor2, TipoNotificacion.COMENTARIO, false, LocalDateTime.now(), "Mensaje para otro usuario.");
        // 3. Guardar usando el repositorio bajo prueba (o la sesión)
        repositorioNotificacion.guardar(nAntigua);
        repositorioNotificacion.guardar(nReciente);
        sessionFactory.getCurrentSession().save(nOtro);


        // 4. Ejecución
        List<Notificacion> resultado = repositorioNotificacion.buscarPorReceptor(receptor.getId());

        // 5. Verificación
        // Debe haber 3 resultados
        assertEquals(2, resultado.size(), "Debe haber 2 notificaciones para el receptor.");

        // Verificación de orden: La más reciente debe estar primero (DESC)

        assertEquals(TipoNotificacion.COMENTARIO, resultado.get(0).getTipo(), "La más reciente (COMENTARIO) debe estar primera (índice 0).");
        assertEquals(TipoNotificacion.LIKE, resultado.get(1).getTipo(), "La más antigua (LIKE) debe estar segunda (índice 1).");

        // Verificación de carga (JOIN FETCH)
        assertNotNull(resultado.get(0).getUsuarioEmisor().getNombre(), "El usuario emisor debe estar cargado ");
    }

    @Test
    public void queSePuedaMarcarUnaNotificacionComoLeida() {
        // 1. Preparación
        Usuario receptor = crearUsuario("Recep", "Test", false, 33333333, "recept@unlam");
        Usuario emisor = crearUsuario("Emi", "Test", false, 44444444, "emiunlam@edu");
        sessionFactory.getCurrentSession().save(receptor);
        sessionFactory.getCurrentSession().save(emisor);

        Notificacion notificacion = crearNotificacion(receptor, emisor, TipoNotificacion.LIKE, false, LocalDateTime.now(), "Te dieron like.");
        repositorioNotificacion.guardar(notificacion);

        sessionFactory.getCurrentSession().clear(); // Limpia la caché para forzar la recarga/actualización

        // 2. Ejecución
        repositorioNotificacion.marcarComoLeida(notificacion.getId());


        // 3. Verificación (Recargando el objeto de la DB)
        Notificacion notificacionActualizada = sessionFactory.getCurrentSession().get(Notificacion.class, notificacion.getId());
        assertTrue(notificacionActualizada.isLeida(), "La notificación debe estar marcada como leída (true).");
    }

    @Test
    public void poderContarNotificacionesNoLeidasParaUnUsuario() {
        // 1. Preparación
        Usuario receptor = crearUsuario("Nat", "Test", false, 55555555, "nt@unlam");
        Usuario emisor = crearUsuario("Emi", "Test", false, 66666666, "emisor@unlam");
        sessionFactory.getCurrentSession().save(receptor);
        sessionFactory.getCurrentSession().save(emisor);

        // 2. Notificaciones de prueba

        repositorioNotificacion.guardar(crearNotificacion(receptor, emisor, TipoNotificacion.LIKE, false, LocalDateTime.now(), "Like no leído."));
        repositorioNotificacion.guardar(crearNotificacion(receptor, emisor, TipoNotificacion.COMENTARIO, true, LocalDateTime.now(), "Comentario leído."));


        // 3. Ejecución
        int conteo = repositorioNotificacion.contarPublisNoLeidasPorUsuario(receptor.getId());

        // 4. Verificación
        assertEquals(1, conteo, "El conteo de notificaciones NO leídas debe ser 1.");
    }
}