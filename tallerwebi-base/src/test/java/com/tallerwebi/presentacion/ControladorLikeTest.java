package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorLikeTest {

    private ServicioPublicacion servicioPublicacionMock;
    private ServicioLike servicioLikesMock;
    private ServicioUsuario servicioUsuarioMock;
    private PublicacionMapper publicacionMapperMock;
    private NotificacionService notificacionServiceMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private DatosUsuario datosUsuarioMock;
    private ControladorLike controladorLike;

    @BeforeEach
    public void init() {
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        publicacionMapperMock = mock(PublicacionMapper.class);
        notificacionServiceMock = mock(NotificacionService.class);

        controladorLike = new ControladorLike(
                servicioPublicacionMock,
                servicioLikesMock,
                servicioUsuarioMock,
                publicacionMapperMock,
                notificacionServiceMock
        );

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        datosUsuarioMock = mock(DatosUsuario.class);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
        when(datosUsuarioMock.getId()).thenReturn(10L);
    }

    @Test
    public void deberiaDevolverLaCantindadDeLikeYDtoDeLaPublicacionCuandoObtengoUnIdDeUnaPublicacionQueNoLeDiLike() {
        Long idEjemplo = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(10L);

        Publicacion publicacion = new Publicacion();
        publicacion.setId(idEjemplo);

        DatosPublicacion dto = new DatosPublicacion();

        when(servicioUsuarioMock.buscarPorId(10L)).thenReturn(usuario);
        when(servicioPublicacionMock.obtenerPublicacionPorId(idEjemplo)).thenReturn(publicacion);
        when(servicioLikesMock.yaDioLike(usuario.getId(), publicacion.getId())).thenReturn(false);
        when(publicacionMapperMock.toDto(publicacion, usuario.getId())).thenReturn(dto);
        when(servicioLikesMock.contarLikes(idEjemplo)).thenReturn(5);

        Model modelMock = mock(Model.class);

        // Llamada al mtodo day y quitar like
        String resultado = controladorLike.darYQuitarLike(idEjemplo, modelMock, requestMock);

        verify(servicioPublicacionMock).obtenerPublicacionPorId(idEjemplo);
        verify(servicioLikesMock).yaDioLike(usuario.getId(), publicacion.getId());
        verify(servicioLikesMock).darLike(usuario.getId(), publicacion.getId());
        verify(servicioLikesMock).contarLikes(idEjemplo);
        verify(publicacionMapperMock).toDto(publicacion, usuario.getId());
        verify(notificacionServiceMock).enviarMensaje("/topic/publicacion/" + idEjemplo, "5");
        verify(modelMock).addAttribute("dtopubli", dto);
        verify(modelMock).addAttribute("cantLikes", 5);

        assertEquals("templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, cantidadLikes=${cantLikes})", resultado);
        assertTrue(dto.getDioLike());
    }
}