package com.tallerwebi.presentacion;



import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ControladorHomeTest {

//    private ControladorHome controlador;
//    private ServicioPublicacion servicioPublicacionMock;
//    private HttpServletRequest requestMock;
//    private HttpSession sessionMock;
//    private Usuario usuarioReal;
//    private ServicioLike servicioLikeMock;
//    private BotPublisherService botPublisherServiceMock;
//    private PublicacionMapper publicacionMapperMock;
//    private ServicioUsuario servicioUsuarioMock;
//    private ServicioRecomendaciones servicioRecomendacionesMock;
//    private GeminiAnalysisService geminiAnalysisServiceMock;
//
//    private DatosUsuario datosUsuarioLogueado;
//
//    @BeforeEach
//    public void init() {
//        // Mocks
//        servicioPublicacionMock = mock(ServicioPublicacion.class);
//        requestMock = mock(HttpServletRequest.class);
//        sessionMock = mock(HttpSession.class);
//        when(requestMock.getSession()).thenReturn(sessionMock);
//
//        servicioUsuarioMock = mock(ServicioUsuario.class);
//        servicioLikeMock = mock(ServicioLike.class);
//        botPublisherServiceMock = mock(BotPublisherService.class);
//        publicacionMapperMock = mock(PublicacionMapper.class);
//        servicioRecomendacionesMock = mock(ServicioRecomendaciones.class);
//        geminiAnalysisServiceMock = mock(GeminiAnalysisService.class);
//
//        // Usuario real del dominio
//        usuarioReal = new Usuario();
//        usuarioReal.setId(1L);
//        usuarioReal.setNombre("Ana");
//        usuarioReal.setApellido("Perez");
//        Carrera c1 = new Carrera();
//        c1.setNombre("Carrera prueba");
//        usuarioReal.setCarrera(c1);
//
//        // DatosUsuario para la sesión
//        datosUsuarioLogueado = new DatosUsuario();
//        datosUsuarioLogueado.setId(usuarioReal.getId());
//        datosUsuarioLogueado.setNombre(usuarioReal.getNombre());
//        datosUsuarioLogueado.setApellido(usuarioReal.getApellido());
//
//        // Simular sesión
//        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioLogueado);
//        when(servicioUsuarioMock.buscarPorId(datosUsuarioLogueado.getId())).thenReturn(usuarioReal);
//
//        // Mock publicaciones
//        Publicacion pub1 = new Publicacion();
//        pub1.setId(1L);
//        pub1.setDescripcion("Publicación 1");
//        pub1.setUsuario(usuarioReal);
//
//        Publicacion pub2 = new Publicacion();
//        pub2.setId(2L);
//        pub2.setDescripcion("Publicación 2");
//        pub2.setUsuario(usuarioReal);
//
//        when(servicioPublicacionMock.findAll()).thenReturn(List.of(pub1, pub2));
//
//        // Mock mapeo a DTO
//        DatosPublicacion dto1 = new DatosPublicacion();
//        dto1.setId(1L);
//        dto1.setDescripcion("Publicación 1");
//
//        DatosPublicacion dto2 = new DatosPublicacion();
//        dto2.setId(2L);
//        dto2.setDescripcion("Publicación 2");
//
//        when(publicacionMapperMock.toDto(pub1, datosUsuarioLogueado.getId())).thenReturn(dto1);
//        when(publicacionMapperMock.toDto(pub2, datosUsuarioLogueado.getId())).thenReturn(dto2);
//
//        // Crear controlador
//        controlador = new ControladorHome(
//                servicioUsuarioMock,
//                servicioPublicacionMock,
//                servicioLikeMock,
//                publicacionMapperMock,
//                servicioRecomendacionesMock,
//                geminiAnalysisServiceMock,
//                botPublisherServiceMock
//        );
//    }
//
//    @Test
//    public void home_UsuarioLogueadoVeDatosPublicacionesYLikesYNoEsBot() {
//        // Ejecutar home con filtro principal
//        ModelAndView mav = controlador.home(requestMock, "p");
//
//        // Validar vista
//        assertEquals("home", mav.getViewName());
//
//        // Validar usuario en modelo
//        DatosUsuario usuarioEnModelo = (DatosUsuario) mav.getModel().get("usuario");
//        assertNotNull(usuarioEnModelo);
//        assertEquals("Ana", usuarioEnModelo.getNombre());
//        assertEquals("Perez", usuarioEnModelo.getApellido());
//
//        // Validar publicaciones
//        List<DatosPublicacion> datosPublicaciones = (List<DatosPublicacion>) mav.getModel().get("datosPublicaciones");
//        assertNotNull(datosPublicaciones);
//        assertEquals(2, datosPublicaciones.size());
//    }
//
//    @Test
//    public void home_FiltroRecomendaciones_CargaPublisParaTi() throws Exception {
//        // Preparar publicaciones de Lucene (recomendadas)
//        Publicacion lucene1 = new Publicacion();
//        lucene1.setId(101L);
//        lucene1.setDescripcion("Recomendada 1");
//        lucene1.setUsuario(usuarioReal);
//
//        Publicacion lucene2 = new Publicacion();
//        lucene2.setId(102L);
//        lucene2.setDescripcion("Recomendada 2");
//        lucene2.setUsuario(usuarioReal);
//
//        List<Publicacion> publisLucene = List.of(lucene1, lucene2);
//        when(servicioRecomendacionesMock.recomendarParaUsuario(usuarioReal, 5)).thenReturn(publisLucene);
//
//        // Preparar publicaciones de bots
//        Publicacion bot1 = new Publicacion();
//        bot1.setId(201L);
//        bot1.setDescripcion("Bot 1");
//        bot1.setUsuario(usuarioReal);
//
//        List<Publicacion> publisBots = List.of(bot1);
//        when(servicioPublicacionMock.obtenerPublisBotsParaUsuario(usuarioReal)).thenReturn(publisBots);
//
//        // Mapeo a DTOs
//        DatosPublicacion dtoLucene1 = new DatosPublicacion();
//        dtoLucene1.setId(101L);
//        dtoLucene1.setDescripcion("Recomendada 1");
//
//        DatosPublicacion dtoLucene2 = new DatosPublicacion();
//        dtoLucene2.setId(102L);
//        dtoLucene2.setDescripcion("Recomendada 2");
//
//        DatosPublicacion dtoBot1 = new DatosPublicacion();
//        dtoBot1.setId(201L);
//        dtoBot1.setDescripcion("Bot 1");
//
//        when(publicacionMapperMock.toDto(lucene1, datosUsuarioLogueado.getId())).thenReturn(dtoLucene1);
//        when(publicacionMapperMock.toDto(lucene2, datosUsuarioLogueado.getId())).thenReturn(dtoLucene2);
//        when(publicacionMapperMock.toDto(bot1, datosUsuarioLogueado.getId())).thenReturn(dtoBot1);
//
//        // Ejecutar home con filtro "r"
//        ModelAndView mav = controlador.home(requestMock, "r");
//
//        // Validar vista
//        assertEquals("home", mav.getViewName());
//
//        // Validar publicaciones "Para ti"
//        List<DatosPublicacion> publisParaTi = (List<DatosPublicacion>) mav.getModel().get("publisParaTi");
//        assertNotNull(publisParaTi);
//        assertEquals(3, publisParaTi.size()); // 2 Lucene + 1 Bot
//
//        // Validar contenido exacto
//        assertTrue(publisParaTi.stream().anyMatch(p -> p.getDescripcion().equals("Recomendada 1")));
//        assertTrue(publisParaTi.stream().anyMatch(p -> p.getDescripcion().equals("Recomendada 2")));
//        assertTrue(publisParaTi.stream().anyMatch(p -> p.getDescripcion().equals("Bot 1")));
//    }

}