package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.RepositorioComentarioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class ControladorPublicacionTest {

    private ControladorPublicacion controladorPublicacion;
    private ServicioPublicado servicioPublicadoMock;
    private ServicioLike servicioLikesMock;
    private ServicioLogin servicioLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private Usuario usuarioMock;
    private DatosUsuario datosUsuarioMock;
    private ServicioUsuario servicioUsuarioMock;
    private ServicioComentario servicioComentarioMock;

    @BeforeEach
    public void init() {
        servicioPublicadoMock = mock(ServicioPublicado.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        servicioComentarioMock = mock(ServicioComentario.class);

        controladorPublicacion = new ControladorPublicacion(
                servicioPublicadoMock,
                servicioLikesMock,
                servicioUsuarioMock,
                servicioComentarioMock
        );

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        usuarioMock = mock(Usuario.class);
        when(usuarioMock.getId()).thenReturn(42L);

        datosUsuarioMock = mock(DatosUsuario.class);
        when(datosUsuarioMock.getId()).thenReturn(42L);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
    }

    @Test
    public void queSePuedaCrearUnaPublicacionConDescripcionYUsuarioYQueVayaAPublicaciones() throws PublicacionFallida {
        // Preparación
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioMock);

        // Ejecución
        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(publicacionMock, requestMock);

        // Validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(publicacionMock).setUsuario(usuarioMock);
        verify(servicioPublicadoMock).realizar(publicacionMock);
    }

    @Test
    public void queUnaPublicacionPuedaRecibirLikesDeUsuariosDiferentes() {
        // Mocks
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicadoMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        // Usuario 1
        Usuario usuario1 = mock(Usuario.class);
        DatosUsuario datosUsuario1 = mock(DatosUsuario.class);
        when(datosUsuario1.getId()).thenReturn(1L);

        HttpServletRequest request1 = mock(HttpServletRequest.class);
        HttpSession session1 = mock(HttpSession.class);
        when(request1.getSession()).thenReturn(session1);
        when(session1.getAttribute("usuarioLogueado")).thenReturn(datosUsuario1);
        when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuario1);

        // Usuario 2
        Usuario usuario2 = mock(Usuario.class);
        DatosUsuario datosUsuario2 = mock(DatosUsuario.class);
        when(datosUsuario2.getId()).thenReturn(2L);

        HttpServletRequest request2 = mock(HttpServletRequest.class);
        HttpSession session2 = mock(HttpSession.class);
        when(request2.getSession()).thenReturn(session2);
        when(session2.getAttribute("usuarioLogueado")).thenReturn(datosUsuario2);
        when(servicioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);

        // Ejecución usuario 1
        ModelAndView mav1 = controladorPublicacion.darLike(5L, request1);
        verify(servicioLikesMock).darLike(usuario1, publicacionMock);
        assertEquals("redirect:/home", mav1.getViewName());

        // Ejecución usuario 2
        ModelAndView mav2 = controladorPublicacion.darLike(5L, request2);
        verify(servicioLikesMock).darLike(usuario2, publicacionMock);
        assertEquals("redirect:/home", mav2.getViewName());
    }



    @Test
    public void queSeContabilicenCorrectamenteLosLikesDeUnaPublicacion() {

        RepositorioLikeImpl repositorioLike = new RepositorioLikeImpl();

        Publicacion publicacion = new Publicacion();
        Usuario usuario1 = new Usuario();
        Usuario usuario2 = new Usuario();

        // Crear y setear likes
        Like like1 = new Like();
        like1.setUsuario(usuario1);
        like1.setPublicacion(publicacion);

        Like like2 = new Like();
        like2.setUsuario(usuario2);
        like2.setPublicacion(publicacion);

        // Guardar likes
        repositorioLike.guardar(like1);
        repositorioLike.guardar(like2);

        // Contar likes
        int cantidadLikes = repositorioLike.contarPorPublicacion(publicacion);

        // Verificar
        assertEquals(2, cantidadLikes);
    }

    @Test
    public void queUnaPublicacionPuedaRecibirComentariosDeUnUsuario() {
        // Mocks
        long idPublicacion = 10L;
        String textoComentario = "Este es un comentario de prueba";

        // Mock usuario y sesión
        Usuario usuario = mock(Usuario.class);
        DatosUsuario datosUsuario = mock(DatosUsuario.class);
        when(datosUsuario.getId()).thenReturn(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);

        // Mock publicación
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuario);
        when(servicioPublicadoMock.obtenerPublicacionPorId(idPublicacion)).thenReturn(publicacionMock);

        // Simular request.getParameter (si no se usa @RequestParam)
        when(request.getParameter("texto")).thenReturn(textoComentario);

        // Llamar al método del controlador
        ModelAndView modelAndView = controladorPublicacion.comentar(idPublicacion, request);

        // Verificaciones
        verify(servicioComentarioMock).comentar(textoComentario, usuario, publicacionMock);
        assertEquals("redirect:/home", modelAndView.getViewName());
    }
}

