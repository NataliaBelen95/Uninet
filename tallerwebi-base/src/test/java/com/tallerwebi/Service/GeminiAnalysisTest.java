package com.tallerwebi.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GeminiAnalysisTest {

    private GeminiAnalysisService geminiAnalysisService;
    private ServicioGustoPersonal servicioGustoPersonalMock;
    private RepositorioInteraccion repositorioInteraccionMock;
    private ObjectMapper objectMapperMock;
    private GeminiJsonParser jsonParserMock;
    private ServicioIntegracionIA servicioIntegracionIAMock;

    @BeforeEach
    public void init() {
        servicioGustoPersonalMock = mock(ServicioGustoPersonal.class);
        repositorioInteraccionMock = mock(RepositorioInteraccion.class);
        servicioIntegracionIAMock = mock(ServicioIntegracionIA.class);
        objectMapperMock = mock(ObjectMapper.class);
        jsonParserMock = mock(GeminiJsonParser.class);
        geminiAnalysisService = new GeminiAnalysisService(repositorioInteraccionMock, servicioGustoPersonalMock,
                objectMapperMock, servicioIntegracionIAMock, jsonParserMock);

    }

    @Test
    public void queSePuedanAnalizarLosGustosYSeGuardenEnGustosPersonalEntity() throws JsonProcessingException {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setId(2L);

        // 1. Configuración de Mocks de ENTRADA
        when(servicioGustoPersonalMock.buscarPorUsuario(u)).thenReturn(null);
        when(repositorioInteraccionMock.consolidarTextoInteraccionesRecientes(any(Usuario.class), anyInt()))
                .thenReturn("Me gustan las publicaciones sobre Java, Spring y bases de datos.");

        // Respuesta JSON que simula lo que devuelve el servicio de IA (String completa)
        final String respuestaGeminiJsonCompleta = "{ \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\n  \\\"temaPrincipal\\\": \\\"Programación Java\\\",\\n  \\\"tagsIntereses\\\": [\\\"Spring\\\", \\\"Hibernate\\\", \\\"Backend\\\"],\\n  \\\"resumenPerfil\\\": \\\"Usuario con interés en desarrollo web backend.\\\"\\n}\" } ] } } ] }";
        when(servicioIntegracionIAMock.enviarPromptYObtenerJson(anyString())).thenReturn(respuestaGeminiJsonCompleta);

        // 2. Mockear el FLUJO INTERNO (ObjectMapper y GeminiResponseDTO)

        // El JSON plano que está ANIDADO en la respuesta de la IA.
        final String jsonGeneradoPorGemini = "{\"temaPrincipal\": \"Programación Java\", \"tagsIntereses\": [\"Spring\", \"Hibernate\", \"Backend\"], \"resumenPerfil\": \"Usuario con interés en desarrollo web backend.\"}";

        // Crear un mock del DTO de respuesta de Gemini.
        GeminiResponseDTO mockGeminiResponse = mock(GeminiResponseDTO.class);
        // Cuando el servicio llame a getGeneratedText(), devuelve el JSON de intereses.
        when(mockGeminiResponse.getGeneratedText()).thenReturn(jsonGeneradoPorGemini);

        // Mockear el ObjectMapper para que, al deserializar la String, devuelva el mock.
        when(objectMapperMock.readValue(eq(respuestaGeminiJsonCompleta), eq(GeminiResponseDTO.class)))
                .thenReturn(mockGeminiResponse);

        //  PARSER del DTO final
        InteresesGeneradosDTO dto = new InteresesGeneradosDTO();
        dto.setTemaPrincipal("Programación Java");
        dto.setTagsIntereses(Arrays.asList("Spring", "Hibernate"));
        dto.setResumenPerfil("Desarrollador backend.");

        // Mockea la llamada al parser con el JSON generado por Gemini.
        when(jsonParserMock.parsearJsonIntereses(eq(jsonGeneradoPorGemini))).thenReturn(dto);

        // 4. Ejecución
        geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        // 5. Verificación
        verify(servicioGustoPersonalMock, times(1)).guardarOActualizar(argThat(g ->
                g.getUsuario().equals(u)
                        && g.getTemaPrincipal().equals("Programación Java")
                        && g.getTagsIntereses().contains("Spring")
                        && g.getResumenPerfil().contains("backend")
        ));

        // Verificar que los pasos de integración y parseo intermedio fueron llamados
        verify(servicioIntegracionIAMock, times(1)).enviarPromptYObtenerJson(anyString());
        verify(objectMapperMock, times(1)).readValue(anyString(), eq(GeminiResponseDTO.class));
        verify(jsonParserMock, times(1)).parsearJsonIntereses(anyString());
    }

    @Test
    public void queElPromptSeGenereCorrectamenteConElTextoYFormatoRequerido() {
        //
        String textoConsolidado = "Me gusta la física cuántica, la programación en C++ y las novelas de ciencia ficción.";

        // 2. Ejecución del método
        String promptGenerado = geminiAnalysisService.generarPrompt(textoConsolidado);

        // 3. Verificaciones

        // a) Debe contener el texto de las interacciones.
        assertTrue(promptGenerado.contains(textoConsolidado),
                "El prompt debe incluir el texto de interacciones.");

        // b) Debe contener las instrucciones clave.
        assertTrue(promptGenerado.contains("especialista en marketing universitario"),
                "El prompt debe definir el rol del especialista.");
        assertTrue(promptGenerado.contains("5 intereses principales"),
                "El prompt debe solicitar los 5 intereses.");
        assertTrue(promptGenerado.contains("UN RESUMEN BREVE DEL PERFIL"),
                "El prompt debe solicitar el resumen del perfil.");

        // c) Debe contener la estructura de formato JSON solicitada.
        assertTrue(promptGenerado.contains("\"temaPrincipal\""),
                "El prompt debe incluir el campo temaPrincipal.");
        assertTrue(promptGenerado.contains("\"tagsIntereses\""),
                "El prompt debe incluir el campo tagsIntereses.");
        assertTrue(promptGenerado.contains("\"resumenPerfil\""),
                "El prompt debe incluir el campo resumenPerfil.");
        assertTrue(promptGenerado.contains("Formato JSON requerido"),
                "El prompt debe indicar claramente el formato de salida.");
    }


    @Test
    public void queOmiteElAnalisisSiFueRecientementeActualizado() throws JsonProcessingException {
        Usuario u = new Usuario();

        //  Gustos con fecha reciente (hace 1 hora)
        GustosPersonal gustosRecientes = new GustosPersonal();
        gustosRecientes.setFechaUltimoAnalisis(LocalDateTime.now().minusHours(1));
        when(servicioGustoPersonalMock.buscarPorUsuario(u)).thenReturn(gustosRecientes);

        // Ejecución
        geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        // verif, no llamar a obtener interacciones ni a la IA.
        verify(repositorioInteraccionMock, never()).consolidarTextoInteraccionesRecientes(any(), anyInt());
        verify(servicioIntegracionIAMock, never()).enviarPromptYObtenerJson(anyString());
    }

    @Test
    public void queRealizaElAnalisisSiElUltimoFueHaceMuchoTiempo() throws JsonProcessingException {
        Usuario u = new Usuario();

        // gustos con mas de 7 hs
        GustosPersonal gustosAntiguos = new GustosPersonal();
        gustosAntiguos.setFechaUltimoAnalisis(LocalDateTime.now().minusHours(7));
        when(servicioGustoPersonalMock.buscarPorUsuario(u)).thenReturn(gustosAntiguos);

        // Asegurarse de mockear el flujo completo para que no falle después de 'debeReanalizar'
        when(repositorioInteraccionMock.consolidarTextoInteraccionesRecientes(any(), anyInt()))
                .thenReturn("texto para analizar");

        // Mockear la respuesta de la IA (puedes reutilizar los mocks del primer test)
        final String respuestaGeminiJsonCompleta = "{...}"; // Define tu JSON completo
        final String jsonGeneradoPorGemini = "{...}";       // Define tu JSON plano
        GeminiResponseDTO mockGeminiResponse = mock(GeminiResponseDTO.class);
        when(mockGeminiResponse.getGeneratedText()).thenReturn(jsonGeneradoPorGemini);
        when(servicioIntegracionIAMock.enviarPromptYObtenerJson(anyString())).thenReturn(respuestaGeminiJsonCompleta);
        when(objectMapperMock.readValue(anyString(), eq(GeminiResponseDTO.class))).thenReturn(mockGeminiResponse);
        when(jsonParserMock.parsearJsonIntereses(anyString())).thenReturn(new InteresesGeneradosDTO());


        // Ejecución
        geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        // Verificación: DEBE llamar a obtener interacciones y a la IA.
        verify(repositorioInteraccionMock, times(1)).consolidarTextoInteraccionesRecientes(any(), anyInt());
        verify(servicioIntegracionIAMock, times(1)).enviarPromptYObtenerJson(anyString());
    }

}