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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
@Transactional
public class RepositorioComentarioTest {

    @Autowired
    private RepositorioComentario repositorioComentario;
    @Autowired
    private SessionFactory sessionFactory;


    private Publicacion crearYGuardarPublicacion(Usuario usuario, String descripcion) {
        Publicacion p = new Publicacion();
        p.setUsuario(usuario);
        p.setDescripcion(descripcion);
        p.setEsPublicidad(false);
        p.setFechaPublicacion(LocalDateTime.now());
        sessionFactory.getCurrentSession().save(p);
        return p;
    }

    private Usuario crearYGuardarUsuario(String email, int dni) {
        Usuario u = new Usuario();
        u.setNombre("Test");
        u.setApellido("User");
        u.setEmail(email);
        u.setDni(dni);
        u.setEsBot(false);
        u.setPassword("pass");
        sessionFactory.getCurrentSession().save(u);
        return u;
    }

    @Test
    public void queSePuedaContarComentariosDeUnaPublicacionPorId () {
        Usuario usuario = crearYGuardarUsuario("user1@like.com", 11111111);
        Publicacion publicacion = crearYGuardarPublicacion(usuario, "Contenido a gustar.");

        // 2. Ejecución (Guardar el comentario)
        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        comentario.setPublicacion(publicacion);
        comentario.setFechaComentario(LocalDateTime.now());
        comentario.setTexto("Primer comentario.");
        Comentario comentario2 = new Comentario();
        comentario2.setUsuario(usuario);
        comentario2.setPublicacion(publicacion);
        comentario2.setFechaComentario(LocalDateTime.now());
        comentario2.setTexto("Segundo comentario.");

        Comentario comentarioGuardado= repositorioComentario.guardar(comentario);
        Comentario comentarioGuardado2= repositorioComentario.guardar(comentario2);

        // 3. Verificación
        assertNotNull(comentarioGuardado.getId(), "El Comentario debe tener un Id luego de ser guardado.");
        assertNotNull(comentarioGuardado2.getId(), "El Comentario2 debe tener un Id luego de ser guardado.");
        sessionFactory.getCurrentSession().clear();
        // Verificamos que la Publicacion se actualizó con el Like (bidireccionalidad)
        Publicacion publiRecargada = sessionFactory.getCurrentSession().get(Publicacion.class, publicacion.getId());
        assertEquals(2, publiRecargada.getComentarios().size(), "La colección de Comentarios debe contener 2 coment.");
    }

    @Test
    public void poderObtenerLosComentariosDeUnaPublicacionPorSuId() {
        // 1. Preparación de Entidades
        Usuario autorPubli = crearYGuardarUsuario("autor@pub.com", 22222222);
        Usuario comentador = crearYGuardarUsuario("comentador@test.com", 33333333);
        Publicacion targetPubli = crearYGuardarPublicacion(autorPubli, "Publicacion Target.");
        Publicacion otraPubli = crearYGuardarPublicacion(autorPubli, "Otra Publicacion.");

        // 2. Preparación de Comentarios: USAR UNA BASE DE TIEMPO FIJA
        LocalDateTime horaBase = LocalDateTime.now();

        // cAntiguo: Hace 2 horas
        Comentario cAntiguo = new Comentario();
        cAntiguo.setUsuario(comentador);
        cAntiguo.setPublicacion(targetPubli);
        cAntiguo.setFechaComentario(horaBase.minusHours(2)); // Más antiguo
        cAntiguo.setTexto("Antiguo.");

        // cReciente: Hace 5 minutos
        Comentario cReciente = new Comentario();
        cReciente.setUsuario(comentador);
        cReciente.setPublicacion(targetPubli);
        cReciente.setFechaComentario(horaBase.minusMinutes(5)); // Más reciente
        cReciente.setTexto("Reciente.");

        // cOtro: Para OTRA Publicacion
        Comentario cOtro = new Comentario();
        cOtro.setUsuario(comentador);
        cOtro.setPublicacion(otraPubli);
        cOtro.setFechaComentario(horaBase.minusMinutes(1)); // Intermedio en el tiempo total
        cOtro.setTexto("No debe aparecer.");

        // 3. Guardar Comentarios (El orden de guardado no importa, solo la fecha seteada)
        repositorioComentario.guardar(cAntiguo);
        repositorioComentario.guardar(cReciente);
        repositorioComentario.guardar(cOtro);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // 4. Ejecución
        List<Comentario> resultado = repositorioComentario.findComentariosByPublicacionId(targetPubli.getId());

        // 5. Verificación
        assertEquals(2, resultado.size(), "Solo debe obtener 2 comentarios para la publicación Target.");

        // Si tu HQL tiene ORDER BY c.fechaComentario DESC, esto DEBE funcionar:
        assertEquals("Reciente.", resultado.get(0).getTexto(), "El comentario más reciente debe estar primero.");
        assertEquals("Antiguo.", resultado.get(1).getTexto(), "El comentario más antiguo debe ser el segundo.");

        assertNotNull(resultado.get(0).getUsuario().getNombre(), "El objeto Usuario debe estar cargado.");
    }
}
