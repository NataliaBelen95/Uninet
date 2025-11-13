package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;


import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private ServicioArchivoUtils servicioArchivoUtilsMock;

    @BeforeEach
    public void init() {
        servicioGustoPersonalMock = mock(ServicioGustoPersonal.class);
        repositorioInteraccionMock = mock(RepositorioInteraccion.class);
        servicioIntegracionIAMock = mock(ServicioIntegracionIA.class);
        objectMapperMock = mock(ObjectMapper.class);
        jsonParserMock = mock(GeminiJsonParser.class);
        geminiAnalysisService = new GeminiAnalysisService(repositorioInteraccionMock, servicioGustoPersonalMock,
                objectMapperMock, servicioIntegracionIAMock, jsonParserMock, servicioArchivoUtilsMock);

    }

    @Test
    public void queSePuedanAnalizarLosGustosYSeGuardenEnGustosPersonalEntity() throws Exception {

        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setId(2L);

        //  el usuario no tiene gustos previos y tiene interacciones
        when(servicioGustoPersonalMock.buscarPorUsuario(any(Usuario.class))).thenReturn(null);
        when(repositorioInteraccionMock.consolidarTextoInteraccionesRecientes(any(Usuario.class), anyInt()))
                .thenReturn("Me gustan las publicaciones sobre Java, Spring y bases de datos.");

        // Simular la respuesta JSON RAW de Gemini
        final String jsonInteresesLimpio = "{\n  \"temaPrincipal\": \"Programación Java\",\n  \"tagsIntereses\": [\"Spring\", \"Hibernate\", \"Backend\"],\"resumenPerfil\": \"Usuario con interés en desarrollo web backend.\"\n}";
        final String respuestaGeminiJsonCompleta = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \""+ jsonInteresesLimpio.replace("\"", "\\\"").replace("\n", "\\n") +"\"}]}}]}";
        when(servicioIntegracionIAMock.enviarPromptYObtenerJson(anyString())).thenReturn(respuestaGeminiJsonCompleta);

        // Mock de PARSEO : Simula la respuesta de los DTOs

        //  Simular la PRIMERA respuesta del ObjectMapper (GeminiResponseDTO)
        // mock de GeminiResponseDTO
        GeminiResponseDTO mockGeminiResponse = mock(GeminiResponseDTO.class);
        when(mockGeminiResponse.getGeneratedText()).thenReturn(jsonInteresesLimpio);

        // Simular la SEGUNDA respuesta/resultado (InteresesGeneradosDTO)
        InteresesGeneradosDTO data = new InteresesGeneradosDTO();
        data.setTemaPrincipal("Programación Java");
        data.setTagsIntereses(Arrays.asList("Spring", "Hibernate", "Backend"));
        data.setResumenPerfil("Usuario con interés en desarrollo web backend.");


        // Encadenamiento de Mocks: Le decimos a Mockito qué devolver para cada clase:
        // LÍNEA 70: objectMapper.readValue(respuesta, GeminiResponseDTO.class);
        when(objectMapperMock.readValue(anyString(), eq(GeminiResponseDTO.class)))
                .thenReturn(mockGeminiResponse); // Devuelve el mock configurado


        // El parser recibe el texto limpio que viene del mock de getGeneratedText()
        when(jsonParserMock.parsearJsonIntereses(eq(jsonInteresesLimpio))).thenReturn(data);


        // Ejecución
        GustosPersonal resultado = geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        //erificación
        assertNotNull(resultado, "Debe devolver un objeto GustosPersonal no nulo.");

        // Verificar que se intentó guardar el resultado final en la BDD
        verify(servicioGustoPersonalMock, times(1)).guardarOActualizar(argThat(g ->
                g.getUsuario().equals(u)
                        && g.getTemaPrincipal().equals("Programación Java")
                        && g.getTagsIntereses().contains("Spring") // Verifica que contenga al menos un tag
                        && g.getResumenPerfil().contains("backend") // Verifica que contenga parte del resumen
        ));

        // 2. Verificar que se llamó al servicio de integración
        verify(servicioIntegracionIAMock, times(1)).enviarPromptYObtenerJson(anyString());

        // 3. Verificar que se usó el ObjectMapper para la primera fase (extracción del JSON limpio)
        verify(objectMapperMock, times(1)).readValue(eq(respuestaGeminiJsonCompleta), eq(GeminiResponseDTO.class));

        // 4. Verificar que se llamó al parser con el texto generado
        verify(jsonParserMock, times(1)).parsearJsonIntereses(eq(jsonInteresesLimpio));

        // 5. Verificar que se llamó al getter del mock para obtener el texto
        verify(mockGeminiResponse, times(1)).getGeneratedText();
    }


    @Test
    public void queElPromptSeGenereCorrectamenteConElTextoYFormatoRequerido() {

        String textoConsolidado = "Me gusta la física cuántica, la programación en C++ y las novelas de ciencia ficción.";

        // Ejecución metodo
        String promptGenerado = geminiAnalysisService.generarPrompt(textoConsolidado);

        // Verificaciones:

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

    @Test
    public void queOmiteElAnalisisSiNoHayInteracciones() throws JsonProcessingException {
        Usuario u = new Usuario();
        // 1. Mockear para que DEBA analizar (nuevo usuario o tiempo expirado)
        when(servicioGustoPersonalMock.buscarPorUsuario(u)).thenReturn(null);

        // 2. Mockear el repositorio para que devuelva texto vacío
        when(repositorioInteraccionMock.consolidarTextoInteraccionesRecientes(any(), anyInt()))
                .thenReturn("");

        // Ejecución
        geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        // Verificación: No debe llamar a generar el prompt ni a la IA.
        verify(servicioIntegracionIAMock, never()).enviarPromptYObtenerJson(anyString());

        // Debe intentar buscar los gustos para devolverlos (si existen, aunque el mock devuelva null)
        verify(servicioGustoPersonalMock, times(2)).buscarPorUsuario(u); // 1 en debeReanalizar, 1 en el bloque 'if'
    }

}