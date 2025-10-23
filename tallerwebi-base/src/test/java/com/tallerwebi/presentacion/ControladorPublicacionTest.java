package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
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
        servicioComentarioMock = mock(ServicioComentario.class);

        controladorPublicacion = new ControladorPublicacion(
                servicioPublicacionMock,
                servicioLikesMock,
                servicioUsuarioMock,
                publicacionMapperMock,
                notificacionServiceMock,
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

//    @Test
//    public void queSePuedaCrearUnaPublicacionConDescripcionYUsuarioYQueVayaAPublicaciones() throws PublicacionFallida {
//        // Preparación
//        MultipartFile archivoMock = new MockMultipartFile(
//                "archivo",                 // Nombre del campo del formulario
//                "archivo.pdf",             // Nombre del archivo
//                "application/pdf",         // Tipo MIME
//                "contenido del archivo".getBytes() // Contenido del archivo simulado
//        );
//
//        DatosUsuario datosUsuarioMock = mock(DatosUsuario.class);
//        when(datosUsuarioMock.getId()).thenReturn(42L);
//
//        when(requestMock.getSession()).thenReturn(sessionMock);
//        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
//        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioMock);
//
//        // Ejecución
//        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(
//                "Descripción de prueba",    // Descripción para la publicación
//                archivoMock,                // El archivo simulado
//                requestMock,                // Objeto de la petición mockeado
//                redirectAttributesMock      // Atributos de redirección mockeados
//        );
//
//        // Validación de la vista y la redirección
//        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
//
//
//        verify(servicioPublicacionMock).realizar(any(Publicacion.class), any(Usuario.class), eq(archivoMock));
//    }


}
