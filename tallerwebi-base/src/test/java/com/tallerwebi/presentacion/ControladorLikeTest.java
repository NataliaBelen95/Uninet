package com.tallerwebi.presentacion;

import com.google.cloud.aiplatform.v1.Model;
import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.apache.http.impl.BHttpConnectionBase;
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
import static org.mockito.Mockito.*;


public class ControladorLikeTest {

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
    @Test
    public void deberiaDevolverLaCantindadDeLikeYDtoDeLaPublicacionCuandoObtengoUnIdDeUnaPublicacionQueNoLeDiLike() {
    }

}