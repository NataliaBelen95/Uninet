package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
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
    private UsuarioMapper usuarioMapperMock;
    private ServicioNotificacion servicioNotificacionMock;

    @BeforeEach
    public void init() {
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioLikesMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        publicacionMapperMock = mock(PublicacionMapper.class);
        notificacionServiceMock = mock(NotificacionService.class);
        usuarioMapperMock = mock(UsuarioMapper.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);

        controladorLike = new ControladorLike(
                servicioPublicacionMock,
                servicioLikesMock,
                servicioUsuarioMock,
                publicacionMapperMock,
                notificacionServiceMock,
                usuarioMapperMock,
                servicioNotificacionMock
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

        Usuario receptor = new Usuario();
        receptor.setId(20L); // el propietario de la publicación
        publicacion.setUsuario(receptor);

        DatosPublicacion dto = new DatosPublicacion();
        dto.setDioLike(false);

        when(servicioUsuarioMock.buscarPorId(10L)).thenReturn(usuario);
        when(servicioPublicacionMock.obtenerPublicacion(idEjemplo)).thenReturn(publicacion);
        when(servicioLikesMock.yaDioLike(usuario.getId(), publicacion.getId())).thenReturn(true);
        when(publicacionMapperMock.toDto(publicacion, usuario.getId())).thenReturn(dto);
        when(servicioLikesMock.contarLikes(idEjemplo)).thenReturn(5);

        Model modelMock = mock(Model.class);

        // Llamada al mtodo day y quitar like
        String resultado = controladorLike.darYQuitarLike(idEjemplo, modelMock, requestMock);

        verify(servicioPublicacionMock).obtenerPublicacion(idEjemplo);
        verify(servicioLikesMock).toggleLike(usuario.getId(), idEjemplo);
        //ya llamo a toggle
        //verify(servicioLikesMock).darLike(usuario.getId(), publicacion.getId());
        verify(servicioLikesMock).toggleLike(usuario.getId(), idEjemplo);
        verify(servicioLikesMock).contarLikes(idEjemplo);
        verify(publicacionMapperMock).toDto(publicacion, usuario.getId());

        verify(notificacionServiceMock).enviarMensaje("/topic/publicacion/" + idEjemplo, "5");
        verify(modelMock).addAttribute("dtopubli", dto);
        verify(modelMock).addAttribute("cantLikes", 5);
        verify(modelMock).addAttribute("usuario", usuario);

        assertEquals( "templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, usuario=${usuario})", resultado);
        assertTrue(dto.getDioLike());
    }
}