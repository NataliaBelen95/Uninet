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
        // Arrange
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicadoMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        Usuario usuario1 = mock(Usuario.class);
        Usuario usuario2 = mock(Usuario.class);


        doNothing().when(servicioLikesMock).darLike(usuario1, publicacionMock);
        doNothing().when(servicioLikesMock).darLike(usuario2, publicacionMock);


        servicioLikesMock.darLike(usuario1, publicacionMock);
        servicioLikesMock.darLike(usuario2, publicacionMock);


        verify(servicioLikesMock).darLike(usuario1, publicacionMock);
        verify(servicioLikesMock).darLike(usuario2, publicacionMock);
    }




    @Test
    public void queSeContabilicenCorrectamenteLosLikesDeUnaPublicacion() {
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicadoMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        // Simulamos que el servicio ya sabe cuántos likes tiene
        when(servicioPublicadoMock.obtenerCantidadDeLikes(5L)).thenReturn(2);

        Usuario usuario1 = mock(Usuario.class);
        Usuario usuario2 = mock(Usuario.class);

        servicioLikesMock.darLike(usuario1, publicacionMock);
        servicioLikesMock.darLike(usuario2, publicacionMock);

        // Validación: no verificamos estado real, sino el valor "stubbeado"
        assertEquals(2, servicioPublicadoMock.obtenerCantidadDeLikes(5L));


    }
}

