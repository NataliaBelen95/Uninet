package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

    @BeforeEach
    public void init() {
        servicioPublicadoMock = mock(ServicioPublicado.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);

        controladorPublicacion = new ControladorPublicacion(
                servicioPublicadoMock,
                servicioLikesMock,
                servicioUsuarioMock
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
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicadoMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        // Datos del usuario 1
        Usuario usuarioMock1 = mock(Usuario.class);
        DatosUsuario datosUsuarioMock1 = mock(DatosUsuario.class);
        HttpSession sessionMock1 = mock(HttpSession.class);
        HttpServletRequest requestMock1 = mock(HttpServletRequest.class);

        when(datosUsuarioMock1.getId()).thenReturn(1L);
        when(requestMock1.getSession()).thenReturn(sessionMock1);
        when(sessionMock1.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock1);
        when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioMock1);

        //usuario 2
        Usuario usuarioMock2 = mock(Usuario.class);
        DatosUsuario datosUsuarioMock2 = mock(DatosUsuario.class);
        HttpSession sessionMock2 = mock(HttpSession.class);
        HttpServletRequest requestMock2 = mock(HttpServletRequest.class);

        when(datosUsuarioMock2.getId()).thenReturn(2L);
        when(requestMock2.getSession()).thenReturn(sessionMock2);
        when(sessionMock2.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock2);
        when(servicioUsuarioMock.buscarPorId(2L)).thenReturn(usuarioMock2);

        // Ejecución 1 usuario
        ModelAndView modelAndView1 = controladorPublicacion.darLike(5L, requestMock1);
        verify(servicioLikesMock).darLike(usuarioMock1, publicacionMock);
        assertEquals("redirect:/home", modelAndView1.getViewName());

        //ejecucion y validacion2 usuario
        ModelAndView modelAndView2 = controladorPublicacion.darLike(5L, requestMock2);
        verify(servicioLikesMock).darLike(usuarioMock2, publicacionMock);
        assertEquals("redirect:/home", modelAndView2.getViewName());

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

}