package com.tallerwebi.dominio;

import com.tallerwebi.infraestructura.config.HibernateTestInfraesructuraConfig;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
@Transactional
public class RespositorioPublicacionTest {
    @Autowired
    private RepositorioPublicacion repositorioPublicacion;
    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void poderGuardarUnsPublicacion() {
        Usuario u = crearUsuario("Juan", "Perez", "jp@unlam.net", 12457896);
        Publicacion p = crearPublicacion(u, false, LocalDateTime.now());
        repositorioPublicacion.guardar(p);
        assertNotNull(repositorioPublicacion.buscarPorId(p.getId()));

    }

    @Test
    public void queSeListenTodasLasPublicacionesConComentarioLikeUsuarioyArchivo_siTiene() {

            Usuario uComentador = crearUsuario("Juan", "Perez", "jp@unlam.net", 2);
            sessionFactory.getCurrentSession().save(uComentador);

            Usuario uDuenioP2 = crearUsuario("Ana", "Sanchez", "js@unlam.net", 3);
            sessionFactory.getCurrentSession().save(uDuenioP2);


            Publicacion p1 = crearPublicacion(uComentador, false, LocalDateTime.now().minusHours(1));
            p1.setDescripcion("Miren esto de js (SIN comentario)");
            repositorioPublicacion.guardar(p1);


            Publicacion p2 = crearPublicacion(uDuenioP2, false, LocalDateTime.now());
            p2.setDescripcion("spring y hibernate (CON comentario)");
            repositorioPublicacion.guardar(p2);


            Comentario c = crearCOmentario("gracias por compartir", LocalDateTime.now(), uComentador, p2);
            sessionFactory.getCurrentSession().save(c);

            // se vacia la sesión para forzar la lectura desde la DB y el FETCH JOIN
            sessionFactory.getCurrentSession().flush();
            sessionFactory.getCurrentSession().clear();

            //Ejecución y Verificación ---
            List<Publicacion> publicaciones = repositorioPublicacion.listarTodas();

            assertEquals(2, publicaciones.size(), "Debería listar las 2 publicaciones guardadas.");

            // ver si  la publicación que SÍ tiene el comentario (p2)
            Publicacion pConComentario = publicaciones.stream()
                    .filter(pu -> pu.getDescripcion().contains("CON comentario"))
                    .findFirst()
                    .get();

            // 1. Verificación del FETCH JOIN y Conteo
            assertTrue(Hibernate.isInitialized(pConComentario.getComentarios()), "Los comentarios de p2 deben estar cargados (FETCH JOIN).");
            assertEquals(1, pConComentario.getComentarios().size(), "La publicación con el comentario (p2) debe tener 1 comentario.");

            // se verifica  el Dueño (pConComentario debe ser de uDuenioP2)
            assertEquals(uDuenioP2.getNombre(), pConComentario.getUsuario().getNombre(), "El dueño de la publicación debe ser: " + uDuenioP2.getNombre());
            assertEquals(uDuenioP2.getDni(), pConComentario.getUsuario().getDni(), "El DNI del dueño debe coincidir.");


    }

    @Test
    public void obtenerPublicacionesLikeadasDeUsuario() {
        Usuario uLikeado = crearUsuario("Juan", "Perez", "jp@unlam.net", 2);
        sessionFactory.getCurrentSession().save(uLikeado);
        Publicacion p2 = crearPublicacion(uLikeado, false, LocalDateTime.now());
        p2.setDescripcion("spring y hibernate (CON Like)");
        repositorioPublicacion.guardar(p2);

        Usuario uQuedaLike = crearUsuario("Ana", "Sanchez", "js@unlam.net", 3);
        sessionFactory.getCurrentSession().save(uQuedaLike);

        Like l = crearLike(uQuedaLike, p2, LocalDateTime.now());
        sessionFactory.getCurrentSession().save(l);

        List<Publicacion> publicacionesLikeadasPorUqueDaLike = repositorioPublicacion.obtenerPublicacionesConLikeDeUsuario(uQuedaLike.getId());

        assertEquals(1, publicacionesLikeadasPorUqueDaLike.size(), "deberia listar 1");

    }
    @Test
    public void obtenerPublicacionesDeUsuarioPorSuId(){

    }
    @Test
    public void obtenerPublicacionesDirigidasAUsuario_publicidadesHechasEnEspecialParaEseUsuario(){

    }






    private Usuario crearUsuario(String nombre, String apellido, String email, int dni) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setDni(dni);
        u.setEsBot(false);
        u.setPassword("password");
        return u;
    }

    private Publicacion crearPublicacion(Usuario usuario, Boolean esPublicida, LocalDateTime fecha) {
        Publicacion p = new Publicacion();
        p.setUsuario(usuario);
        p.setEsPublicidad(esPublicida);
        p.setFechaPublicacion(fecha);
        return p;
    }

    private Comentario crearCOmentario(String texto, LocalDateTime fecha, Usuario usuario, Publicacion publicacion) {
      Comentario c = new Comentario();
      c.setTexto(texto);
      c.setUsuario(usuario);
      c.setFechaComentario(fecha);
      c.setPublicacion(publicacion);
      return c;
    }
      private Like crearLike(Usuario usuario, Publicacion publicacion, LocalDateTime fecha) {
        Like l = new Like();
        l.setUsuario(usuario);
        l.setPublicacion(publicacion);
        l.setFecha(fecha);
        return l;
   }



}
