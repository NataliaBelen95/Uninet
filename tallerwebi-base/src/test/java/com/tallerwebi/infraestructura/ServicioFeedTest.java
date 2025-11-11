package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ServicioFeedTest {

    private ServicioPublicacion servicioPublicacionMock;
    private ServicioRecomendaciones servicioRecomendacionesMock;
    private PublicacionMapper publicacionMapperMock; // Nombre consistente con el uso en el @Test
    private ServicioFeed servicioFeed;

    @BeforeEach
    public void setUp() {
        // Inicialización de los MOCKS
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        servicioRecomendacionesMock = mock(ServicioRecomendaciones.class);
        publicacionMapperMock = mock(PublicacionMapper.class);

        // Inicialización del servicio BAJO PRUEBA (SUT) con los mocks
        servicioFeed = new ServicioFeed(servicioPublicacionMock, servicioRecomendacionesMock, publicacionMapperMock);
    }


    @Test
    public void obtenerElFeedPrincipal_debeObtenerSoloPublicacionesNoPublicitariasYMapearlasADTO() {
        // ID de usuario que se pasará al feed
        Long usuarioId = 1L;
        Usuario usuario = crearUsuario("Juan", "Perez", "jp@un", 12456789);
        usuario.setId(usuarioId);


        Publicacion p1 = crearPublicacion(usuario, false, LocalDateTime.now());
        Publicacion p2 = crearPublicacion(usuario, false, LocalDateTime.now());
        List<Publicacion> publicacionesFiltradas = Arrays.asList(p1, p2);


        DatosPublicacion dto1 = new DatosPublicacion();
        DatosPublicacion dto2 = new DatosPublicacion();

        // mockear Fujo

        // Simular la llamada al servicio de publicación
        when(servicioPublicacionMock.obtenerTodasPublicacionesIgnorandoPublicidades())
                .thenReturn(publicacionesFiltradas);

        // simular mapeo a dto de c/u de las publis
        when(publicacionMapperMock.toDto(eq(p1), anyLong())).thenReturn(dto1);
        when(publicacionMapperMock.toDto(eq(p2), anyLong())).thenReturn(dto2);


        // 3. Ejecución
        List<DatosPublicacion> resultado = servicioFeed.obtenerFeedPrincipal(usuarioId);

        // 4. Verificación
        verify(servicioPublicacionMock, times(1)).obtenerTodasPublicacionesIgnorandoPublicidades();

        //lista dto de publicaciones retorne 2
        assertEquals(2, resultado.size(), "El feed principal debe devolver 2 publicaciones.");

        // que el mapeo se haya hecho
        verify(publicacionMapperMock, times(1)).toDto(eq(p1), eq(usuarioId));
        verify(publicacionMapperMock, times(1)).toDto(eq(p2), eq(usuarioId));

        // que la lista contenga los DTOs correctos
        assertEquals(dto1, resultado.get(0));
        assertEquals(dto2, resultado.get(1));

    }
    @Test
    public void obtenerFeedRecomendado_debeCombinarPublisRecomendadasYPublisDeBots() throws Exception {
        // 1. Preparación de Datos
        Long usuarioId = 10L;
        Usuario usuarioActual = crearUsuario("Test", "User", "test@un", 1234);
        usuarioActual.setId(usuarioId);

        //Publicaciones Recomendadas organicas---
        Publicacion rec1 = crearPublicacion(usuarioActual, false, LocalDateTime.now());
        Publicacion rec2 = crearPublicacion(usuarioActual, false, LocalDateTime.now());
        List<Publicacion> publisRecomendadas = Arrays.asList(rec1, rec2);

        // Publicaciones de Bots (Publicidad Dirigida) ---
        Usuario bot = crearUsuario("Bot", "X", "bot@ia", 999);
        Publicacion bot1 = crearPublicacion(bot, true, LocalDateTime.now());
        Publicacion bot2 = crearPublicacion(bot, true, LocalDateTime.now());
        List<Publicacion> publisBots = Arrays.asList(bot1, bot2);

        //DTOs esperados
        DatosPublicacion dtoRec1 = new DatosPublicacion();
        DatosPublicacion dtoRec2 = new DatosPublicacion();
        DatosPublicacion dtoBot1 = new DatosPublicacion();
        DatosPublicacion dtoBot2 = new DatosPublicacion();

        final int totalEsperado = 4;

        // 2. Mocking del Flujo

        // Mock 1: Simular la respuesta de las recomendaciones
        when(servicioRecomendacionesMock.recomendarParaUsuario(eq(usuarioActual), eq(5)))
                .thenReturn(publisRecomendadas);

        // Mock 2: Simular la respuesta de las publicaciones de bots dirigidas
        when(servicioPublicacionMock.obtenerPublisBotsParaUsuario(eq(usuarioActual)))
                .thenReturn(publisBots);

        // Mock 3: Simular el Mapeo a DTO (debe ocurrir para los 4 objetos, usando el ID del usuario)
        when(publicacionMapperMock.toDto(eq(rec1), eq(usuarioId))).thenReturn(dtoRec1);
        when(publicacionMapperMock.toDto(eq(rec2), eq(usuarioId))).thenReturn(dtoRec2);
        when(publicacionMapperMock.toDto(eq(bot1), eq(usuarioId))).thenReturn(dtoBot1);
        when(publicacionMapperMock.toDto(eq(bot2), eq(usuarioId))).thenReturn(dtoBot2);


        // 3. Ejecución
        List<DatosPublicacion> resultado = servicioFeed.obtenerFeedRecomendado(usuarioActual, usuarioId);

        // 4. Verificación

        // a) Verificar que se llamaron a los servicios de obtención de datos
        verify(servicioRecomendacionesMock, times(1)).recomendarParaUsuario(eq(usuarioActual), eq(5));
        verify(servicioPublicacionMock, times(1)).obtenerPublisBotsParaUsuario(eq(usuarioActual));

        // b) Verificar que el tamaño de la lista combinada es correcto
        assertEquals(totalEsperado, resultado.size(), "El feed debe combinar 2 recomendadas y 2 de bots.");

        // c) Verificar que el mapeo ocurrió exactamente 4 veces , 4 publis
        verify(publicacionMapperMock, times(totalEsperado)).toDto(any(Publicacion.class), eq(usuarioId));

        // Verificar que la lista contenga todos los DTOs esperados (verificando la combinación)
        assertTrue(resultado.contains(dtoRec1));
        assertTrue(resultado.contains(dtoBot2));
    }

    // --- MÉTODOS UTILITARIOS ---

    private Usuario crearUsuario(String nombre, String apellido, String email, int dni) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setDni(dni);
        u.setEsBot(false);
        u.setPassword("password");
        return u;
    }

    private Publicacion crearPublicacion(Usuario usuario, Boolean esPublicida, LocalDateTime fecha) {
        Publicacion p = new Publicacion();
        p.setUsuario(usuario);
        p.setEsPublicidad(esPublicida);
        p.setFechaPublicacion(fecha);
        return p;
    }

}