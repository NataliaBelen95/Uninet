package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class ControladorPublicacionTest {

    private ControladorPublicacion controladorPublicacion;
    private ServicioPublicacion servicioPublicacionMock;
    private ServicioLike servicioLikesMock;
    private ServicioLogin servicioLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private Usuario usuarioMock;
    private DatosUsuario datosUsuarioMock;
    private ServicioUsuario servicioUsuarioMock;
    private ServicioComentario servicioComentarioMock;
    private RedirectAttributes redirectAttributesMock;
    private PublicacionMapper publicacionMapperMock;

    @BeforeEach
    public void init() {
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        servicioComentarioMock = mock(ServicioComentario.class);
        publicacionMapperMock = mock(PublicacionMapper.class);

        controladorPublicacion = new ControladorPublicacion(
                servicioPublicacionMock,
                servicioLikesMock,
                servicioUsuarioMock,
                servicioComentarioMock,
                publicacionMapperMock
        );

        requestMock = mock(HttpServletRequest.class);
        redirectAttributesMock = mock(RedirectAttributes.class);
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
        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(publicacionMock, requestMock, redirectAttributesMock);

        //Validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));

       verify(servicioPublicacionMock).realizar(publicacionMock, usuarioMock);
    }



    /*preguntar donde van test , no son de controlador publicaion */
    @Test
    public void queUnaPublicacionPuedaRecibirLikesDeUsuariosDiferentes() {
        // Arrange
        Publicacion publicacionMock = mock(Publicacion.class);
        when(servicioPublicacionMock.obtenerPublicacionPorId(5L)).thenReturn(publicacionMock);

        Usuario usuario1 = mock(Usuario.class);
        Usuario usuario2 = mock(Usuario.class);

        servicioLikesMock.darLike(usuario1, publicacionMock);
        servicioLikesMock.darLike(usuario2, publicacionMock);


        verify(servicioLikesMock).darLike(usuario1, publicacionMock);
        verify(servicioLikesMock).darLike(usuario2, publicacionMock);
    }



    @Test
    public void queAlVolverADarleLikeSeQuiteLaPublicacion() {
        // Arrange
        Usuario usuario = mock(Usuario.class);
        Publicacion publicacion = mock(Publicacion.class);
        Like likeMock = mock(Like.class);

        when(servicioLikesMock.obtenerLike(usuario, publicacion)).thenReturn(likeMock);
        when(likeMock.getId()).thenReturn(123L);

        // Act
        servicioLikesMock.darLike(usuario, publicacion);
        servicioLikesMock.quitarLike(123L);

        // Assert
        verify(servicioLikesMock).darLike(usuario, publicacion);
        verify(servicioLikesMock).quitarLike(123L);
    }
}

