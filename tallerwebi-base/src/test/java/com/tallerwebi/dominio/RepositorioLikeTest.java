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
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
@Transactional
public class RepositorioLikeTest {
    @Autowired
    private RepositorioLike repositorioLike;
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
    public void queUnUsuarioPuedarDarLikeAUnaPublicacion() {
        // 1. Preparación
        Usuario usuario = crearYGuardarUsuario("user1@like.com", 11111111);
        Publicacion publicacion = crearYGuardarPublicacion(usuario, "Contenido a gustar.");

        // 2. Ejecución (Guardar el Like)
        Like nuevoLike = new Like();
        nuevoLike.setUsuario(usuario);
        nuevoLike.setPublicacion(publicacion);
        nuevoLike.setFecha(LocalDateTime.now());

        Like likeGuardado = repositorioLike.guardar(nuevoLike);

        // 3. Verificación
        assertNotNull(likeGuardado.getId(), "El Like debe tener un ID asignado después de guardarse.");
        sessionFactory.getCurrentSession().clear(); //esencial para forzar que la Publicacion se recargue de la base de datos
        // Verificamos que la Publicacion se actualizó con el Like (bidireccionalidad)
        Publicacion publiRecargada = sessionFactory.getCurrentSession().get(Publicacion.class, publicacion.getId());
        assertEquals(1, publiRecargada.getLikesDePublicacion().size(), "La colección de Likes debe contener 1 like.");
    }

    @Test
    public void poderPoderDeshacerLike() {
        // 1. Preparación
        Usuario usuario = crearYGuardarUsuario("user2@unlike.com", 22222222);
        Publicacion publicacion = crearYGuardarPublicacion(usuario, "Publicación con like a eliminar.");

        // Crear y guardar el Like
        Like likeInicial = new Like();
        likeInicial.setUsuario(usuario);
        likeInicial.setPublicacion(publicacion);
        repositorioLike.guardar(likeInicial);

        sessionFactory.getCurrentSession().clear();

        //  Ejecución (Eliminar el Like)
        repositorioLike.eliminar(likeInicial.getId());


        // Verificación
        Like likeEliminado = repositorioLike.buscarPorId(likeInicial.getId());
        Publicacion publiRecargada = sessionFactory.getCurrentSession().get(Publicacion.class, publicacion.getId());

        // El Like ya no debe existir en la DB
        assertNull(likeEliminado, "El Like debe ser null después de la eliminación.");

        // La colección de la Publicacion debe estar limpia
        assertEquals(0, publiRecargada.getLikesDePublicacion().size(), "La colección de Likes de la Publicación debe estar vacía.");
    }

    @Test
    public void poderChequearSiExisteLikePorPubliYUsuario() {
        // 1. Preparación
        Usuario usuarioTarget = crearYGuardarUsuario("user3@check.com", 33333333);
        Usuario otroUsuario = crearYGuardarUsuario("otro@check.com", 44444444);
        Publicacion publicacionTarget = crearYGuardarPublicacion(usuarioTarget, "Publicación a chequear.");
        Publicacion publicacionNoTarget = crearYGuardarPublicacion(usuarioTarget, "Publicación vacía.");

        // Guardar el Like: Solo entre usuarioTarget y publicacionTarget
        Like likeExistente = new Like();
        likeExistente.setUsuario(usuarioTarget);
        likeExistente.setPublicacion(publicacionTarget);
        repositorioLike.guardar(likeExistente);
        sessionFactory.getCurrentSession().flush();

        // 2. Ejecución y Verificación

        // a) CASO POSITIVO: Existe el like
        boolean existeTarget = repositorioLike.existePorUsuarioYPublicacion(usuarioTarget.getId(), publicacionTarget.getId());
        assertTrue(existeTarget, "Debe retornar TRUE si el Like existe.");

        // b) CASO NEGATIVO 1: No existe Like en otra publicación del mismo usuario
        boolean existeNoTarget = repositorioLike.existePorUsuarioYPublicacion(usuarioTarget.getId(), publicacionNoTarget.getId());
        assertFalse(existeNoTarget, "Debe retornar FALSE si la publicación no tiene like.");

        // c) CASO NEGATIVO 2: No existe Like del otro usuario
        boolean existeOtroUsuario = repositorioLike.existePorUsuarioYPublicacion(otroUsuario.getId(), publicacionTarget.getId());
        assertFalse(existeOtroUsuario, "Debe retornar FALSE si el Like pertenece a otro usuario.");
    }
}