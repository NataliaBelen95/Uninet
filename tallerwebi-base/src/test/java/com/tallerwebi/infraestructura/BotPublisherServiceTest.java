package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BotPublisherServiceTest {

    private RepositorioGustoPersonal repositorioGustoPersonalMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioPublicacion servicioPublicacionMock;
    private ObjectMapper objectMapperMock;
    private ServicioImagenIA servicioImagenIAMock;
    private LuceneService luceneServiceMock;
    private ServicioIntegracionIA servicioIntegracionIAMock;
    private GeminiAnalysisService geminiAnalysisServiceMock;
    private BotPublisherServiceImpl botPublisherService;

    @BeforeEach
    public void init() {
        repositorioGustoPersonalMock = mock(RepositorioGustoPersonal.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        objectMapperMock = mock(ObjectMapper.class);
        servicioImagenIAMock = mock(ServicioImagenIA.class);
        luceneServiceMock = mock(LuceneService.class);
        servicioIntegracionIAMock = mock(ServicioIntegracionIA.class);
        botPublisherService = new BotPublisherServiceImpl(repositorioGustoPersonalMock, repositorioUsuarioMock,
                servicioPublicacionMock, objectMapperMock, servicioImagenIAMock, servicioIntegracionIAMock);
        {


        }

    }


    @Test
    public void queUnUsuarioBotPuedaPublicarPublicidadParaUnUsuarioEnParticular() throws Exception {

        // 1. Datos de prueba:
        final long targetUserId = 5L;
        Usuario usuarioReceptor = new Usuario();
        usuarioReceptor.setId(targetUserId);
        usuarioReceptor.setEsBot(false);

        Usuario botUsuario = new Usuario();
        botUsuario.setId(99L);
        botUsuario.setEsBot(true); // el autor es bot

        // Gustos del usuario receptor
        GustosPersonal gustos = new GustosPersonal();
        gustos.setTemaPrincipal("Java");
        gustos.setTagsIntereses("Spring, Backend");
        gustos.setResumenPerfil("Desarrollador web backend.");

        // 2. Mockear Repositorios y Servicios (Flujo de Negocio)
        when(repositorioUsuarioMock.buscarBots()).thenReturn(List.of(botUsuario)); // Lista de bots disponibles
        when(repositorioUsuarioMock.buscarPorId(eq(targetUserId))).thenReturn(usuarioReceptor);
        when(repositorioGustoPersonalMock.buscarPorUsuario(eq(usuarioReceptor))).thenReturn(gustos);
        when(servicioImagenIAMock.generarImagenRelacionada(anyString())).thenReturn("url_imagen_generada.jpg");

        // 3. Mockear la Cadena de Deserialización de la IA

        final String contenidoGeneradoIA = "¡No te pierdas el evento de Spring Boot en Madrid!";
        final String jsonResponse = "{ \"candidates\": [...] }"; // El contenido real del JSON

        // Mocks anidados para simular la estructura de GeminiResponseDTO
        GeminiResponseDTO.Part mockPart = mock(GeminiResponseDTO.Part.class);
        when(mockPart.getText()).thenReturn(contenidoGeneradoIA);

        GeminiResponseDTO.Content mockContent = mock(GeminiResponseDTO.Content.class);
        when(mockContent.getParts()).thenReturn(List.of(mockPart));

        GeminiResponseDTO.Candidate mockCandidate = mock(GeminiResponseDTO.Candidate.class);
        when(mockCandidate.getContent()).thenReturn(mockContent);

        GeminiResponseDTO mockGeminiResponse = mock(GeminiResponseDTO.class);
        when(mockGeminiResponse.getCandidates()).thenReturn(List.of(mockCandidate));

        // Mockear la integración con la IA
        when(servicioIntegracionIAMock.enviarPromptYObtenerJson(anyString())).thenReturn(jsonResponse);

        // Mockear el ObjectMapper para que devuelva la estructura mockeada de Gemini
        when(objectMapperMock.readValue(anyString(), eq(GeminiResponseDTO.class)))
                .thenReturn(mockGeminiResponse);

        // 4. Ejecución (publicarContenidoParaUsuario)
        botPublisherService.publicarContenidoParaUsuario(targetUserId);

        // 5. Verificación (Verificamos que el método final de guardado fue llamado)
        verify(servicioPublicacionMock, times(1)).guardarPubliBot(
                argThat(p -> p.getDescripcion().contains("Spring Boot")), // Se generó y se asignó el contenido
                eq(botUsuario),
                eq("url_imagen_generada.jpg")
        );

        // Verificamos que se llamó a la IA con el prompt correcto (que usa los datos de GustosPersonal)
        verify(servicioIntegracionIAMock, times(1)).enviarPromptYObtenerJson(
                argThat(prompt -> prompt.contains("Java") && prompt.contains("Spring, Backend") && prompt.contains("Desarrollador web backend."))
        );
        }





}
