package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosAmigos;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorAmigoTest {

    private ServicioAmistad servicioAmistadMock;
    private ServicioNotificacion servicioNotificacionMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioUsuario servicioUsuarioMock;
    private UsuarioMapper usuarioMapperMock;

    private ControladorAmistad controladorAmistad;

    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    // Use a real DTO instance for session to avoid mocking primitive-returning getters and null surprises
    private DatosUsuario datosUsuarioSesion;

    @BeforeEach
    public void setUp() {
        servicioAmistadMock = mock(ServicioAmistad.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        usuarioMapperMock = mock(UsuarioMapper.class);

        controladorAmistad = new ControladorAmistad(
                servicioAmistadMock,
                servicioNotificacionMock,
                repositorioUsuarioMock,
                servicioUsuarioMock,
                usuarioMapperMock
        );

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        // create a real DatosUsuario for session (avoid subtle NPEs when using mocked DTO)
        datosUsuarioSesion = new DatosUsuario();

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioSesion);
    }


    @Test
    public void listarAmigosConSesionNulaDeberiaRedirigirALogin() {
        // override session to return null for this test
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(null);

        ModelMap model = new ModelMap();

        String vista = controladorAmistad.listarAmigos(requestMock, model);

        assertEquals("redirect:/login", vista);
    }

    @Test
    public void listarAmigosConDatosEnSesionDeberiaPonerAmigosDTOsEnElModeloYRetornarLista() {
        Long idUsuario = 7L;

        // configure session DTO
        datosUsuarioSesion.setId(idUsuario);
        // ensure session returns this DTO
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioSesion);

        // Entidad usuario obtenida desde repositorio
        Usuario usuarioEntidad = new Usuario();
        usuarioEntidad.setId(idUsuario);
        usuarioEntidad.setNombre("Juan");
        usuarioEntidad.setApellido("Perez");

        // Amigos retornados por el servicio (entidades Usuario)
        Usuario amigo1 = new Usuario();
        amigo1.setId(21L);
        amigo1.setNombre("Ana");
        amigo1.setApellido("Gomez");
        amigo1.setFotoPerfil("foto1.png");

        Usuario amigo2 = new Usuario();
        amigo2.setId(22L);
        amigo2.setNombre("Luis");
        amigo2.setApellido("Martinez");
        amigo2.setFotoPerfil("foto2.png");

        List<Usuario> listaAmigos = Arrays.asList(amigo1, amigo2);

        when(repositorioUsuarioMock.buscarPorId(idUsuario)).thenReturn(usuarioEntidad);
        when(servicioAmistadMock.listarAmigos(usuarioEntidad)).thenReturn(listaAmigos);

        ModelMap model = new ModelMap();

        String vista = controladorAmistad.listarAmigos(requestMock, model);

        assertEquals("lista-amigos", vista);

        // Verificar que el DTO guardado en modelo es el DatosUsuario de sesión
        assertSame(datosUsuarioSesion, model.get("usuario"));

        // Verificar que esPropio está en true
        assertTrue(Boolean.TRUE.equals(model.get("esPropio")));

        // Verificar que se colocaron amigos y que se transformaron a DatosAmigos
        assertTrue(model.containsKey("amigos"));
        Object amigosObj = model.get("amigos");
        assertNotNull(amigosObj);
        assertTrue(amigosObj instanceof List<?>);
        List<?> amigosDTOs = (List<?>) amigosObj;
        assertEquals(2, amigosDTOs.size());

        // Comprobar contenido básico de los DTOs (si la clase DatosAmigos está disponible)
        Object primero = amigosDTOs.get(0);
        assertEquals(DatosAmigos.class, primero.getClass());
        DatosAmigos dtoPrimero = (DatosAmigos) primero;
        assertEquals(amigo1.getId(), dtoPrimero.getId());
        assertEquals(amigo1.getNombre(), dtoPrimero.getNombre());
        assertEquals(amigo1.getApellido(), dtoPrimero.getApellido());

        // Verificar que se actualizó la sesión con el mismo DTO de usuario
        verify(sessionMock).setAttribute("usuarioLogueado", datosUsuarioSesion);

        verify(repositorioUsuarioMock).buscarPorId(idUsuario);
        verify(servicioAmistadMock).listarAmigos(usuarioEntidad);
    }
}