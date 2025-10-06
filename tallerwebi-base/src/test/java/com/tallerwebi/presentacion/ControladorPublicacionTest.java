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
    public void poderDarLikeAUnaPublicacionYQueSeRefleje() {
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicadoMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        // Datos del usuario en sesión
        when(datosUsuarioMock.getId()).thenReturn(42L);
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);

        // Mock del servicio por id (mismo id que la sesión)
        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioMock);

        // Ejecución
        ModelAndView modelAndView = controladorPublicacion.darLike(5L, requestMock);

        // Validación
        verify(servicioLikesMock).darLike(usuarioMock, publicacionMock);

        // También podrías verificar que el controlador redirige al endpoint de likes
        assertEquals("redirect:/home", modelAndView.getViewName());
    }

}
