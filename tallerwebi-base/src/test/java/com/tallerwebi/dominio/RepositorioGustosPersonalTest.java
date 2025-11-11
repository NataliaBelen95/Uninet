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
public class RepositorioGustosPersonalTest {

    @Autowired
    private RepositorioGustoPersonal repositorioGustosPersonal;
    @Autowired
    private SessionFactory sessionFactory;


    @Test
    public void queSePuedaObtenerGustosPersonalesDeUsuario(){
        Usuario uConGUstos = crearUsuario("Juan", "Perez" , "jp@unlam.edu", 214568798);
        sessionFactory.getCurrentSession().save(uConGUstos);

        final String TAGS_ESPERADOS = "spring,javascript,bootstrap,css,java";
        final String RESUMEN_ESPERADO = "Estudiante con alta afinidad por el desarrollo web backend y frontend, enfocado en Java.";
        final String TEMA_ESPERADO = "Desarrollo Web Full Stack";
        GustosPersonal gustosDeUsuarioConGustos = crearGustosPersonal(
                uConGUstos,
                TAGS_ESPERADOS,
                RESUMEN_ESPERADO,
                TEMA_ESPERADO,
                LocalDateTime.now()
        );
        repositorioGustosPersonal.guardarOActualizar(gustosDeUsuarioConGustos);
        GustosPersonal gustosRecuperados =  repositorioGustosPersonal.buscarPorUsuario(uConGUstos);

        assertNotNull(gustosRecuperados, "Se tendria que haber recuperado GustosPersonal");
        assertEquals(TEMA_ESPERADO, gustosRecuperados.getTemaPrincipal(), "El tema principal debe coincidir.");
        assertEquals(TAGS_ESPERADOS, gustosRecuperados.getTagsIntereses(), "Los tags de intereses deben coincidir.");
        assertEquals(uConGUstos.getId(), gustosRecuperados.getUsuario().getId(), "El ID del usuario debe coincidir.");

    }

    @Test
    public void poderObtenerUsuariosAnalizadosId(){
        Usuario uConGUstos = crearUsuario("Juan", "Perez" , "jp@unlam.edu", 214568798);
        sessionFactory.getCurrentSession().save(uConGUstos);

        final String TAGS_ESPERADOS = "spring,javascript,bootstrap,css,java";
        final String RESUMEN_ESPERADO = "Estudiante con alta afinidad por el desarrollo web backend y frontend, enfocado en Java.";
        final String TEMA_ESPERADO = "Desarrollo Web Full Stack";
        GustosPersonal gustosDeUsuarioConGustos = crearGustosPersonal(
                uConGUstos,
                TAGS_ESPERADOS,
                RESUMEN_ESPERADO,
                TEMA_ESPERADO,
                LocalDateTime.now()
        );
        repositorioGustosPersonal.guardarOActualizar(gustosDeUsuarioConGustos);

        List<Long> ususAnalizadosId = repositorioGustosPersonal.obtenerUsuariosAnalizadosId();
        assertEquals(1, ususAnalizadosId.size(), "Debería haber un solo usuario analizado.");
        assertEquals(uConGUstos.getId(), ususAnalizadosId.get(0),
                "El ID del usuario recuperado debe coincidir con el usuario que tiene los gustos.");

}










    //***************** fn creadoras de de entidades ******************//
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

    private GustosPersonal crearGustosPersonal(Usuario usuario, String tagIntereses, String resumenPerfil, String temaPrincipal, LocalDateTime fechaUltimoAnalisis) {
        GustosPersonal g = new GustosPersonal();
        g.setUsuario(usuario);
        g.setTagsIntereses(tagIntereses);
        g.setResumenPerfil(resumenPerfil);
        g.setTemaPrincipal(temaPrincipal);
        g.setFechaUltimoAnalisis(fechaUltimoAnalisis);
        return g;
    }

}
