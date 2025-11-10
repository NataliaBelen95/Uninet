package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Interaccion;
import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioInteraccion;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
public class RepositorioInteraccionTest {

    @Autowired
    private RepositorioInteraccion repositorioInteraccion;

    @Autowired
    private SessionFactory sessionFactory;

    @BeforeEach
    public void setup() throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        ds.setUrl("jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");

        LocalSessionFactoryBean lsf = new LocalSessionFactoryBean();
        lsf.setDataSource(ds);
        lsf.setPackagesToScan("com.tallerwebi.dominio");
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "create");
        lsf.setHibernateProperties(props);
        lsf.afterPropertiesSet(); // importante
        this.sessionFactory = lsf.getObject();
    }

    @Test
    public void poderGuardarYBuscarInteraccion() {
        // --- Crear datos de prueba ---
        Usuario usuario = new Usuario();
        usuario.setNombre("Luis");
        sessionFactory.getCurrentSession().save(usuario);

        Publicacion publicacion = new Publicacion();
        publicacion.setDescripcion("Contenido prueba");
        sessionFactory.getCurrentSession().save(publicacion);

        Interaccion interaccion = new Interaccion();
        interaccion.setTipo("LIKE");
        interaccion.setContenido("les dejo lo nuevo de js");
        interaccion.setUsuario(usuario);
        interaccion.setPublicacion(publicacion);

        // --- Guardar ---
        repositorioInteraccion.guardar(interaccion);

        // --- Verificar que se guardó ---
        Interaccion recuperada = repositorioInteraccion.encontrarInteraccionPorId(interaccion.getId());
        assertNotNull(recuperada);
        assertEquals("LIKE", recuperada.getTipo());
        assertEquals("les dejo lo nuevo de js", recuperada.getContenido());
        assertEquals(usuario.getId(), recuperada.getUsuario().getId());
        assertEquals(publicacion.getId(), recuperada.getPublicacion().getId());
    }

    @Test
    public void poderListarInteraccionesDeUnUsuario() {
        // --- Crear usuario ---
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        sessionFactory.getCurrentSession().save(usuario);

        // --- Crear publicaciones ---
        Publicacion pub1 = new Publicacion();
        pub1.setDescripcion("Post 1");
        sessionFactory.getCurrentSession().save(pub1);

        Publicacion pub2 = new Publicacion();
        pub2.setDescripcion("Post 2");
        sessionFactory.getCurrentSession().save(pub2);

        // --- Crear interacciones ---
        Interaccion i1 = new Interaccion();
        i1.setTipo("LIKE");
        i1.setContenido("Me gusta");
        i1.setUsuario(usuario);
        i1.setPublicacion(pub1);

        Interaccion i2 = new Interaccion();
        i2.setTipo("COMENTARIO");
        i2.setContenido("Buen post");
        i2.setUsuario(usuario);
        i2.setPublicacion(pub2);

        repositorioInteraccion.guardar(i1);
        repositorioInteraccion.guardar(i2);

        // --- Listar ---
        List<Interaccion> interacciones = repositorioInteraccion.encontrarDeUsuario(usuario);

        assertEquals(2, interacciones.size());
        assertTrue(interacciones.stream().anyMatch(i -> "LIKE".equals(i.getTipo())));
        assertTrue(interacciones.stream().anyMatch(i -> "COMENTARIO".equals(i.getTipo())));
    }
}
