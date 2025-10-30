package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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
    private DatosUsuario datosUsuarioMock;

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
        datosUsuarioMock = mock(DatosUsuario.class);

        when(requestMock.getSession()).thenReturn(sessionMock);
    }

    @Test
    public void enviarSolicitudDeberiaLlamarServicioYRedirigirAUsuarios() {
        Long idSolicitante = 1L;
        Long idReceptor = 2L;

        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
        when(datosUsuarioMock.getId()).thenReturn(idSolicitante);

        Usuario solicitante = new Usuario();
        solicitante.setId(idSolicitante);
        Usuario receptor = new Usuario();
        receptor.setId(idReceptor);

        when(servicioUsuarioMock.buscarPorId(idSolicitante)).thenReturn(solicitante);
        when(servicioUsuarioMock.buscarPorId(idReceptor)).thenReturn(receptor);

        String resultado = controladorAmistad.enviarSolicitud(idReceptor, requestMock);

        assertEquals("redirect:/usuarios", resultado);
        verify(servicioUsuarioMock).buscarPorId(idSolicitante);
        verify(servicioUsuarioMock).buscarPorId(idReceptor);
        verify(servicioAmistadMock).enviarSolicitud(solicitante, receptor);
    }

    @Test
    public void aceptarSolicitudDeberiaLlamarServicioYRedirigirASolicitudes() {
        Long idSolicitud = 42L;

        String resultado = controladorAmistad.aceptarSolicitud(idSolicitud);

        assertEquals("redirect:/amistad/solicitudes", resultado);
        verify(servicioAmistadMock).aceptarSolicitud(idSolicitud);
    }

    @Test
    public void rechazarSolicitudDeberiaLlamarServicioYRedirigirASolicitudes() {
        Long idSolicitud = 43L;

        String resultado = controladorAmistad.rechazarSolicitud(idSolicitud);

        assertEquals("redirect:/amistad/solicitudes", resultado);
        verify(servicioAmistadMock).rechazarSolicitud(idSolicitud);
    }

    @Test
    public void listarSolicitudesPendientesDeberiaPonerSolicitudesEnElModeloYRetornarVista() {
        Long idUsuario = 5L;

        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
        when(datosUsuarioMock.getId()).thenReturn(idUsuario);

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        // ServicioAmistadImpl devuelve List<SolicitudAmistad>
        SolicitudAmistad solicitud = new SolicitudAmistad();
        List<SolicitudAmistad> listaSolicitudes = Collections.singletonList(solicitud);

        when(servicioUsuarioMock.buscarPorId(idUsuario)).thenReturn(usuario);
        when(servicioAmistadMock.listarSolicitudesPendientes(usuario)).thenReturn(listaSolicitudes);

        ModelMap model = new ModelMap();

        String vista = controladorAmistad.listarSolicitudesPendientes(requestMock, model);

        assertEquals("solicitudes-amistad", vista);
        assertTrue(model.containsAttribute("solicitudes"));
        assertSame(listaSolicitudes, model.get("solicitudes"));
        verify(servicioUsuarioMock).buscarPorId(idUsuario);
        verify(servicioAmistadMock).listarSolicitudesPendientes(usuario);
    }

    @Test
    public void listarAmigosConSesionNulaDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(null);

        ModelMap model = new ModelMap();

        String vista = controladorAmistad.listarAmigos(requestMock, model);

        assertEquals("redirect:/login", vista);
    }

    @Test
    public void listarAmigosConDatosEnSesionDeberiaPonerAmigosDTOsEnElModeloYRetornarLista() {
        Long idUsuario = 7L;

        // Datos en sesión
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
        when(datosUsuarioMock.getId()).thenReturn(idUsuario);

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
        assertSame(datosUsuarioMock, model.get("usuario"));

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
        verify(sessionMock).setAttribute("usuarioLogueado", datosUsuarioMock);

        verify(repositorioUsuarioMock).buscarPorId(idUsuario);
        verify(servicioAmistadMock).listarAmigos(usuarioEntidad);
    }
}