package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionNoEncontrada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ControladorComentarioTest {
    private ServletContext servletContextMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioPublicacion servicioPublicacionMock;
    private ServicioComentario servicioComentarioMock;
    private ServicioUsuario servicioUsuarioMock;
    private PublicacionMapper publicacionMapperMock;
    private NotificacionService notificacionServiceMock;
    private DatosUsuario datosUsuario;
    private ServicioLike servicioLikeMock;
    private ControladorComentario controladorComentario;
    private Usuario usuarioMock;
    private Publicacion publicacionMock;
    private Comentario comentarioMock;

    @BeforeEach
    public void init() {
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioLikeMock = mock(ServicioLike.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        publicacionMapperMock = mock(PublicacionMapper.class);
        notificacionServiceMock = mock(NotificacionService.class);
        servicioComentarioMock = mock(ServicioComentario.class);
        publicacionMock = mock(Publicacion.class);
        comentarioMock = mock(Comentario.class);
        sessionMock = mock(HttpSession.class);
        requestMock = mock(HttpServletRequest.class);
        usuarioMock = mock(Usuario.class);


        // Mocks de request y sesión
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpSession sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);


        datosUsuario = new DatosUsuario();
        datosUsuario.setId(42L);
        datosUsuario.setNombre("Usuario 1");
        datosUsuario.setApellido("Usuario1Apellido");
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);

        controladorComentario = new ControladorComentario(
                servicioComentarioMock,
                servicioPublicacionMock,
                servicioUsuarioMock,
                servicioLikeMock,
                notificacionServiceMock,
                publicacionMapperMock

        );




    }


    @Test
    public void queAlComentarUnaPublicacionConIdeDevuelvaElComentarioYLaCantidadDeComentariosParaActualizarLaVista() throws PublicacionNoEncontrada {

        long publiId = 1L;
        String textoComentario = "Comentario 1";

        DatosComentario dto = new DatosComentario();
        dto.setTexto(textoComentario);


        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuario);


        // Mocks de servicios
        when(servicioUsuarioMock.buscarPorId(42L)).thenReturn(usuarioMock);
        when(publicacionMock.getId()).thenReturn(publiId);

        Comentario comentarioReal = new Comentario();
        comentarioReal.setTexto(textoComentario);
        when(servicioComentarioMock.comentar(dto, usuarioMock, publicacionMock)).thenReturn(comentarioReal);

        DatosComentario comentarioDevueltoDTO = new DatosComentario();
        comentarioDevueltoDTO.setTexto(textoComentario);
        comentarioDevueltoDTO.setNombreUsuario("Natalia");
        comentarioDevueltoDTO.setApellidoUsuario("García");

        when(publicacionMapperMock.toComentarioDto(comentarioReal)).thenReturn(comentarioDevueltoDTO);

        when(servicioLikeMock.contarLikes(publiId)).thenReturn(5);
        when(servicioComentarioMock.contarComentarios(publiId)).thenReturn(3);
        when(servicioPublicacionMock.obtenerPublicacion(publiId)).thenReturn(publicacionMock);

        // Ejecutar método
        Map<String, Object> respuesta = controladorComentario.comentarPublicacion(publiId, dto, requestMock);

        // Asserts
        assertNotNull(respuesta);

        DatosComentario comentarioDevuelto = (DatosComentario) respuesta.get("comentario");
        assertNotNull(comentarioDevuelto);
        assertEquals(textoComentario, comentarioDevuelto.getTexto());
        assertEquals("Natalia", comentarioDevuelto.getNombreUsuario());
        assertEquals("García", comentarioDevuelto.getApellidoUsuario());

        assertEquals(3, respuesta.get("cantidadComentarios"));
        assertEquals(5, respuesta.get("cantidadLikes"));


        // Verificar WebSocket

        verify(notificacionServiceMock).enviarMensaje("/topic/publicacion/" + publiId, "comentarioNuevo");
    }


    @Test
    public void queSePuedanObtenerLosComentariosDeUnaPublicacionPorId(){

        long publiId = 1L;

        //Comentarios Simulados
        Comentario comentario1Mock = mock(Comentario.class);Comentario comentario2Mock = mock(Comentario.class);
        List<Comentario> comentarios = List.of(comentario1Mock, comentario2Mock);

        //Simulacion que el servicio devuelve los comentarios
        when(servicioComentarioMock.encontrarComentariosPorId(publiId)).thenReturn(comentarios);

        //Dto simulado para devolver
        DatosComentario dto1 = new DatosComentario();
        dto1.setTexto("Comentario 1");
        dto1.setNombreUsuario("Natalia");
        dto1.setApellidoUsuario("Ram");

        DatosComentario dto2 = new DatosComentario();
        dto2.setTexto("Comentario 2");
        dto2.setNombreUsuario("Ramiro");
        dto2.setApellidoUsuario("López");

        //Simular que el mapper los convierte a dto
        when(publicacionMapperMock.toComentarioDto(comentario1Mock)).thenReturn(dto1);
        when(publicacionMapperMock.toComentarioDto(comentario2Mock)).thenReturn(dto2);

        //ejecutar el metodo del controlador
        Map<String, Object> respuesta = controladorComentario.obtenerComentarios(publiId);
        assertNotNull(respuesta);

        List<DatosComentario> comentariosDTO = (List<DatosComentario>) respuesta.get("comentarios");

        assertNotNull(comentariosDTO);
        assertEquals(2, comentariosDTO.size());

        assertEquals("Comentario 1", comentariosDTO.get(0).getTexto());
        assertEquals("Natalia", comentariosDTO.get(0).getNombreUsuario());
        assertEquals("Ram", comentariosDTO.get(0).getApellidoUsuario());

        assertEquals("Comentario 2", comentariosDTO.get(1).getTexto());
        assertEquals("Ramiro", comentariosDTO.get(1).getNombreUsuario());
        assertEquals("López", comentariosDTO.get(1).getApellidoUsuario());
        // System.out.println("Comentarios obtenidos: " + comentariosDTO);


    }
}