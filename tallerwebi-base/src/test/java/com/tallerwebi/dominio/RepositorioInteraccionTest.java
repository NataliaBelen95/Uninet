package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Interaccion;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioInteraccion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateTestInfraesructuraConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraesructuraConfig.class})
@Transactional
public class RepositorioInteraccionTest {

    @Autowired
    private RepositorioInteraccion repositorioInteraccion;
    @Autowired
    private SessionFactory sessionFactory;



    @Test
    public void poderGuardarUnsNuevaInteraccionDeUsuario() {

        Usuario usuario = crearUsuario("Luis", "Perez", "luis@uninet.com", 123456);
        sessionFactory.getCurrentSession().save(usuario);

        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion("Contenido prueba");
        sessionFactory.getCurrentSession().save(publicacion);

        //fecha
        Interaccion interaccion = crearInteraccion(usuario, publicacion, "LIKE", "les dejo lo nuevo de js", LocalDateTime.now());

        repositorioInteraccion.guardar(interaccion);


        Interaccion recuperada = repositorioInteraccion.encontrarInteraccionPorId(interaccion.getId());
        assertNotNull(recuperada);
        assertEquals("LIKE", recuperada.getTipo());
        assertEquals("les dejo lo nuevo de js", recuperada.getContenido());
        assertEquals(usuario.getId(), recuperada.getUsuario().getId());
        assertEquals(publicacion.getId(), recuperada.getPublicacion().getId());
    }

    @Test
    public void queSeEncuntreInteraccionPorId() {
        Usuario usuario = crearUsuario("Luis", "Perez", "luis@uninet.com", 123456);
        sessionFactory.getCurrentSession().save(usuario);
        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion("Contenido prueba");
        sessionFactory.getCurrentSession().save(publicacion);

        Interaccion interaccion = crearInteraccion(usuario, publicacion, "LIKE", "les dejo lo nuevo de js", LocalDateTime.now());
        repositorioInteraccion.guardar(interaccion);

                                                                                                //devuelve interaccion y despues getid
        assertEquals(interaccion.getId(), repositorioInteraccion.encontrarInteraccionPorId(interaccion.getId()).getId());
    }

    @Test
    public void queSePuedaArmarUnSoloTextoDeLasUltimas50InteraccionesDelUsuario(){
        Usuario usuario = crearUsuario("Luis", "Limite", "luis.limit@uninet.com", 5);
        sessionFactory.getCurrentSession().save(usuario);

        Publicacion pub = new Publicacion();
        pub.setDescripcion("Post de test");
        sessionFactory.getCurrentSession().save(pub);


        sessionFactory.getCurrentSession().clear();
        final int LIMITE_A_PROBAR = 3;  //real son 50
        LocalDateTime ahora = LocalDateTime.now();
        //excluidas
        Interaccion i1 = crearInteraccion(usuario, pub, "VISTA", "Texto antiguo excluido", ahora.minus(5, ChronoUnit.MINUTES));
        Interaccion i2 = crearInteraccion(usuario, pub, "VISTA", "Texto viejo que no pasa el limite", ahora.minus(4, ChronoUnit.MINUTES));

        //recientes// chronounit permite  saber y forzar cuál es el registro más reciente y más antiguo, de forma totalmente predecible.
        Interaccion i3 = crearInteraccion(usuario, pub, "LIKE", "El tercer item mas nuevo (tercero)", ahora.minus(3, ChronoUnit.MINUTES));
        Interaccion i4 = crearInteraccion(usuario, pub, "COMENTARIO", "El segundo item mas nuevo (segundo)", ahora.minus(2, ChronoUnit.MINUTES));
        Interaccion i5 = crearInteraccion(usuario, pub, "LIKE", "EL MAS NUEVO (primero en el resultado)", ahora.minus(1, ChronoUnit.MINUTES));

        repositorioInteraccion.guardar(i1);
        repositorioInteraccion.guardar(i2);
        repositorioInteraccion.guardar(i3);
        repositorioInteraccion.guardar(i4);
        repositorioInteraccion.guardar(i5);


        String textoConsolidado = repositorioInteraccion.consolidarTextoInteraccionesRecientes(usuario, LIMITE_A_PROBAR);
        //separador ". " (punto y espacio)
        //funcion que tiene en cuenta cuantas oraciones hay por " . " ;
        long numFrases = textoConsolidado.chars().filter(ch -> ch == '.').count();
        //limite son 3 , y la funcion nuymfrases determina que son 3 tambien
        assertEquals(LIMITE_A_PROBAR, numFrases, "El número de frases consolidadas debe ser igual al límite.");

        //empezar por la mas reciente
        assertTrue(textoConsolidado.startsWith("EL MAS NUEVO (primero en el resultado)"),
                "El texto debe iniciar con la interacción más reciente (i5) debido al ORDER BY DESC.");

        assertTrue(textoConsolidado.contains("El segundo item mas nuevo"), "i4 debe estar incluida.");
        assertTrue(textoConsolidado.contains("El tercer item mas nuevo"), "i3 debe estar incluida.");
        assertFalse(textoConsolidado.contains("Texto antiguo excluido"), "i1 debe haber sido excluido por el límite.");
        assertFalse(textoConsolidado.contains("Texto viejo que no pasa el limite"), "i2 debe haber sido excluido por el límite.");
    }


    private Interaccion crearInteraccion(Usuario usuario, Publicacion publicacion, String tipo, String contenido, LocalDateTime fecha) {
        Interaccion interaccion = new Interaccion();
        interaccion.setTipo(tipo);
        interaccion.setContenido(contenido);
        interaccion.setUsuario(usuario);
        interaccion.setPublicacion(publicacion);
        interaccion.setFecha(fecha);
        return interaccion;
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
}
