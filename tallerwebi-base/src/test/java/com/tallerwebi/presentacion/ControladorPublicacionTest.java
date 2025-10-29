package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        datosUsuarioMock = mock(DatosUsuario.class);
        when(datosUsuarioMock.getId()).thenReturn(42L);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);
    }

    @Test
    public void agregarPublicacion_redirigeAPerfil_siRefererEsPerfil() throws PublicacionFallida, IOException {
        // Preparación
        Usuario usuario = new Usuario();
        usuario.setId(datosUsuarioMock.getId());
        when(servicioUsuarioMock.buscarPorId(datosUsuarioMock.getId())).thenReturn(usuario);

        MultipartFile archivoMock = new MockMultipartFile(
                "archivo",
                "archivo.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );

        // Simular referer a perfil
        when(requestMock.getHeader("Referer")).thenReturn("http://localhost:8080/perfil/42");

        DatosPublicacion dtoMock = mock(DatosPublicacion.class);
        when(publicacionMapperMock.toDto(any(Publicacion.class), eq(42L))).thenReturn(dtoMock);

        ModelAndView result = controladorPublicacion.agregarPublicacion(
                "Descripción",
                archivoMock,
                null,
                requestMock,
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/perfil/42", result.getViewName());
        verify(servicioPublicacionMock).realizar(any(Publicacion.class), eq(usuario), eq(archivoMock));
        verify(notificacionServiceMock).enviarMensajePubli(eq("/topic/publicaciones"), eq(dtoMock));
    }


    @Test
    public void agregarPublicacion_redirigeAHome_siRefererNoEsPerfil() throws PublicacionFallida, IOException {
        // Preparación
        Usuario usuario = new Usuario();
        usuario.setId(datosUsuarioMock.getId());
        when(servicioUsuarioMock.buscarPorId(datosUsuarioMock.getId())).thenReturn(usuario);

        MultipartFile archivoMock = new MockMultipartFile(
                "archivo",
                "archivo.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );

        // Simular referer a home o null
        when(requestMock.getHeader("Referer")).thenReturn("http://localhost:8080/home");

        DatosPublicacion dtoMock = mock(DatosPublicacion.class);
        when(publicacionMapperMock.toDto(any(Publicacion.class), eq(42L))).thenReturn(dtoMock);

        ModelAndView result = controladorPublicacion.agregarPublicacion(
                "Descripción",
                archivoMock,
                null,
                requestMock,
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/home", result.getViewName());
        verify(servicioPublicacionMock).realizar(any(Publicacion.class), eq(usuario), eq(archivoMock));
        verify(notificacionServiceMock).enviarMensajePubli(eq("/topic/publicaciones"), eq(dtoMock));
    }

    @Test
    public void eliminar_publicacion_usuarioDueño_redirigeHome() throws NoSeEncuentraPublicacion {
        // Preparación
        long publicacionId = 10L;

        Usuario usuario = new Usuario();
        usuario.setId(42L);

        Publicacion publicacion = new Publicacion();
        publicacion.setId(publicacionId);
        publicacion.setUsuario(usuario);

        // mock sesion y usu logueado
        when(requestMock.getSession()).thenReturn(sessionMock);
        DatosUsuario datosUsuario = mock(DatosUsuario.class);
        when(datosUsuario.getId()).thenReturn(42L);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);

        //mock serviciopubli
        when(servicioPublicacionMock.obtenerPublicacion(publicacionId)).thenReturn(publicacion);

        // Ejecución
        ModelAndView result = controladorPublicacion.eliminar(publicacionId, requestMock);

        // Verificacion
        assertNotNull(result);
        assertEquals("redirect:/home", result.getViewName());
        verify(servicioPublicacionMock).eliminarPublicacionEntera(publicacion);
    }

    @Test
    public void eliminar_publicacion_usuarioNoDueño_devuelveError() throws NoSeEncuentraPublicacion {
        // Preparación
        long publicacionId = 10L;

        Usuario duenio = new Usuario();
        duenio.setId(99L); // diferente del logueado

        Publicacion publicacion = new Publicacion();
        publicacion.setId(publicacionId);
        publicacion.setUsuario(duenio);

        // mock sesion y usu logueado
        when(requestMock.getSession()).thenReturn(sessionMock);
        DatosUsuario datosUsuario = mock(DatosUsuario.class);
        when(datosUsuario.getId()).thenReturn(42L); // usuario logueado
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);

        // Mock serviciopubli
        when(servicioPublicacionMock.obtenerPublicacion(publicacionId)).thenReturn(publicacion);

        // Ejecución
        ModelAndView result = controladorPublicacion.eliminar(publicacionId, requestMock);

        // Verificacion
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals("No tienes permisos para eliminar esta publicación.",
                result.getModel().get("mensaje"));

        verify(servicioPublicacionMock, never()).eliminarPublicacionEntera(any());
    }

    @Test
    public void obtenerTarjetaPublicacion_debeAgregarTodosLosAtributosAlModel() {
        // Preparación
        long publicacionId = 10L;

        // Publicación simulada
        Usuario duenio = new Usuario();
        duenio.setId(42L);

        Publicacion publicacion = new Publicacion();
        publicacion.setId(publicacionId);
        publicacion.setUsuario(duenio);

        DatosUsuario datosUsuario = mock(DatosUsuario.class);
        when(datosUsuario.getId()).thenReturn(42L);
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);

        when(servicioPublicacionMock.obtenerPublicacion(publicacionId)).thenReturn(publicacion);

        // DTO de publicación
        DatosPublicacion dtopubliMock = mock(DatosPublicacion.class);
        when(publicacionMapperMock.toDto(publicacion, 42L)).thenReturn(dtopubliMock);

        // Usuario real
        Usuario usuarioReal = new Usuario();
        usuarioReal.setId(42L);
        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioReal);

        // Comentarios y likes
        List<Comentario> comentarios = Arrays.asList(new Comentario(), new Comentario());
        when(servicioPublicacionMock.obtenerComentariosDePublicacion(publicacionId))
                .thenReturn(comentarios);
        when(servicioLikesMock.contarLikes(publicacionId)).thenReturn(5);
        when(servicioComentarioMock.contarComentarios(publicacionId)).thenReturn(2);

        Model modelMock = mock(Model.class);

        // Ejecución
        String view = controladorPublicacion.obtenerTarjetaPublicacion(publicacionId, modelMock, requestMock);

        // Verificacion
        assertEquals("templates/divTarjetaPublicacion :: tarjetaPublicacion(dtopubli=${dtopubli}, comentarios=${comentarios}, likes=${cantLikes}, cantComentarios=${cantComentarios}, usuario=${usuario})", view);
        verify(modelMock).addAttribute("dtopubli", dtopubliMock);
        verify(modelMock).addAttribute("usuario", usuarioReal);
        verify(modelMock).addAttribute("comentarios", comentarios);
        verify(modelMock).addAttribute("cantLikes", 5);
        verify(modelMock).addAttribute("cantComentarios", 2);
    }


}