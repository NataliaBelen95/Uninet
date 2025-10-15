package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
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
    private NotificacionService notificacionServiceMock;

    @BeforeEach
    public void init() {
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        publicacionMapperMock = mock(PublicacionMapper.class);
        notificacionServiceMock = mock(NotificacionService.class);

        controladorPublicacion = new ControladorPublicacion(
                servicioPublicacionMock,
                servicioLikesMock,
                servicioUsuarioMock,
                publicacionMapperMock,
                notificacionServiceMock
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
        MultipartFile archivoMock = new MockMultipartFile(
                "archivo",                 // Nombre del campo del formulario
                "archivo.pdf",             // Nombre del archivo
                "application/pdf",         // Tipo MIME
                "contenido del archivo".getBytes() // Contenido del archivo simulado
        );

        DatosUsuario datosUsuarioMock = mock(DatosUsuario.class);
        when(datosUsuarioMock.getId()).thenReturn(42L);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioMock);

        // Ejecución
        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(
                "Descripción de prueba",    // Descripción para la publicación
                archivoMock,                // El archivo simulado
                requestMock,                // Objeto de la petición mockeado
                redirectAttributesMock      // Atributos de redirección mockeados
        );

        // Validación de la vista y la redirección
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));


        verify(servicioPublicacionMock).realizar(any(Publicacion.class), any(Usuario.class), eq(archivoMock));
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

