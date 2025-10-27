package com.tallerwebi.presentacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionNoEncontrada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class ControladorNotificacionTest {

    private ControladorNotificacion controlador;
    private MockMvc mockMvc;

    private DatosUsuario usuarioMock;
    private Usuario usuarioReal;
    private ServicioUsuario servicioUsuarioMock;
    private ServicioNotificacion servicioNotificacionMock;
    private SimpMessagingTemplate simpMessagingTemplateMock;
    private NotificacionService notificacionServiceMock;
    @BeforeEach
    void init() {
        // Crear mocks
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        servicioUsuarioMock = mock(ServicioUsuario.class);
        simpMessagingTemplateMock = mock(SimpMessagingTemplate.class);
        notificacionServiceMock = mock(NotificacionService.class);

        // Inyectar mocks en el controlador
        controlador = new ControladorNotificacion(
                servicioNotificacionMock,
                servicioUsuarioMock,
                notificacionServiceMock
        );

        // Inicializar MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(controlador).build();
        // Datos de prueba
        usuarioMock = new DatosUsuario();
        usuarioMock.setId(1L);
        usuarioMock.setNombre("admin");
        usuarioMock.setEmail("admin@correo.com");

        usuarioReal = new Usuario();
        usuarioReal.setId(1L);
        usuarioReal.setNombre("Admin");
    }


    @Test
    void queMarqueNotificacionComoLeida() throws Exception {
        // Dado
        Long idNotificacion = 1L;
        when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioReal);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuarioLogueado", usuarioMock);

        // Cuando y Entonces
        mockMvc.perform(post("/marcar-leida/{id}", idNotificacion).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        // Verificar que se haya llamado al servicio
        verify(servicioNotificacionMock).marcarLeida(idNotificacion);
        verify(simpMessagingTemplateMock).convertAndSend(eq("/topic/notificaciones-1"), anyInt());
    }

    @Test
    void queDevuelvaNotificacionesDropdown() throws Exception {

        Notificacion noti = new Notificacion();
        noti.setId(1L);
        noti.setMensaje("Admin comentó tu publicación");
        noti.setLeida(false);
        noti.setUsuarioEmisor(usuarioReal);

        when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioReal);
        when(servicioNotificacionMock.obtenerPorUsuario(1L)).thenReturn(List.of(noti));

        // Session con objeto completo
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuarioLogueado", usuarioMock);

        // Cuando
        String response = mockMvc.perform(get("/notificaciones-dropdown").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // Convertir JSON a array
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // <-- importante para LocalDateTime


        DatosNotificacion[] notificaciones = mapper.readValue(response, DatosNotificacion[].class);

        // Entonces
        assertEquals(1, notificaciones.length);
        assertEquals("Admin comentó tu publicación", notificaciones[0].getMensaje());
        assertFalse(notificaciones[0].isLeida());
    }
}