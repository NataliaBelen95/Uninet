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
    public void obtenerElFeedPrincipal_debeObtenerSoloPublicacionesDeAmigosYpropiasYnoBots() {

        Long usuarioId = 1L;

        // Usuario Logueado (ID 1L)
        Usuario usuarioLogueado = crearUsuario("Yo", "Mismo", "yo@un", 123);
        usuarioLogueado.setId(usuarioId);

        Usuario usuarioAmigo = crearUsuario("Amigo", "Uno", "amigo@un", 456);



        Usuario usuarioBot = crearUsuario("Bot", "Malo", "bot@ia", 789);
        usuarioBot.setId(3L);
        usuarioBot.setEsBot(true);


        Publicacion pPropia = crearPublicacion(usuarioLogueado, false, LocalDateTime.now());
        pPropia.setDescripcion("Propia");
        Publicacion pAmigo = crearPublicacion(usuarioAmigo, false, LocalDateTime.now().minusHours(1));
        pAmigo.setDescripcion("Amigo");


        List<Publicacion> publicacionesFiltradas = Arrays.asList(pPropia, pAmigo);


        // DTOs esperados
        DatosPublicacion dtoPropia = new DatosPublicacion();
        DatosPublicacion dtoAmigo = new DatosPublicacion();
        // No necesitamos DTO para pBot porque no debería ser devuelta por el mock

        // 2. Mocking Corregido: Simular la llamada al nuevo método del servicio de publicación
        when(servicioPublicacionMock.publicacionesDeAmigos(eq(usuarioId)))
                .thenReturn(publicacionesFiltradas); // El mock devuelve solo las 2 válidas

        // Simular mapeo a DTO de las 2 publicaciones válidas
        when(publicacionMapperMock.toDto(eq(pPropia), eq(usuarioId))).thenReturn(dtoPropia);
        when(publicacionMapperMock.toDto(eq(pAmigo), eq(usuarioId))).thenReturn(dtoAmigo);


        // 3. Ejecución
        List<DatosPublicacion> resultado = servicioFeed.obtenerFeedPrincipal(usuarioId);

        // 4. Verificación
        // a) Verificar que se llamó al método correcto
        verify(servicioPublicacionMock, times(1)).publicacionesDeAmigos(eq(usuarioId));

        // b) Verificar el tamaño
        assertEquals(2, resultado.size(), "El feed principal debe devolver 2 publicaciones (propia y de amigo).");

        // c) Verificar que el mapeo se hizo para las publicaciones correctas
        verify(publicacionMapperMock, times(1)).toDto(eq(pPropia), eq(usuarioId));
        verify(publicacionMapperMock, times(1)).toDto(eq(pAmigo), eq(usuarioId));

        // d) Asegurar que la lista contiene los DTOs correctos
        assertTrue(resultado.contains(dtoPropia), "Debe contener la publicación propia.");
        assertTrue(resultado.contains(dtoAmigo), "Debe contener la publicación del amigo.");

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

        // Publicaciones de Bots  ---
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