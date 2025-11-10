package com.tallerwebi.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GeminiAnalysisTest {

    private GeminiAnalysisService geminiAnalysisService;
    private  ServicioGustoPersonal servicioGustoPersonalMock;
    private  RepositorioInteraccion repositorioInteraccionMock;
    private ObjectMapper objectMapperMock;
    private GeminiJsonParser jsonParserMock;
    private ServicioIntegracionIA servicioIntegracionIAMock;

    @BeforeEach
    public void init() {
        servicioGustoPersonalMock =  mock(ServicioGustoPersonal.class);
        repositorioInteraccionMock = mock(RepositorioInteraccion.class);
        servicioIntegracionIAMock = mock(ServicioIntegracionIA.class);
        objectMapperMock = mock(ObjectMapper.class);
        jsonParserMock = mock(GeminiJsonParser.class);
        geminiAnalysisService = new GeminiAnalysisService(repositorioInteraccionMock, servicioGustoPersonalMock,
                                                          objectMapperMock,servicioIntegracionIAMock, jsonParserMock);

    }

    @Test
    public void queSePuedanAnalizarLosGustosYSeGuardenEnGustosPersonalEntity() throws JsonProcessingException {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setId(2L);
        //no hay gustos previos, se crea uno
        when(servicioGustoPersonalMock.buscarPorUsuario(u)).thenReturn(null);
        //texto en las ultimas interacciones
        when(repositorioInteraccionMock.consolidarTextoInteraccionesRecientes(any(Usuario.class), anyInt()))
                .thenReturn("Me gustan las publicaciones sobre Java, Spring y bases de datos.");

        //respuesta json
        String respuestaGeminiJson = "{ \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"{\\n  \\\"temaPrincipal\\\": \\\"Programación Java\\\",\\n  \\\"tagsIntereses\\\": [\\\"Spring\\\", \\\"Hibernate\\\", \\\"Backend\\\"],\\n  \\\"resumenPerfil\\\": \\\"Usuario con interés en desarrollo web backend.\\\"\\n}\" } ] } } ] }";
        when(servicioIntegracionIAMock.enviarPromptYObtenerJson(anyString())).thenReturn(respuestaGeminiJson);

        //respuesta del modelo Gemini (como JSON plano)
        GeminiResponseDTO.Part part = new GeminiResponseDTO.Part();
        part.setText("{\"temaPrincipal\": \"Programación Java\", \"tagsIntereses\": [\"Spring\", \"Hibernate\"], \"resumenPerfil\": \"Desarrollador backend.\"}");

        GeminiResponseDTO.Content content = new GeminiResponseDTO.Content();
        content.setParts(List.of(part));

        GeminiResponseDTO.Candidate candidate = new GeminiResponseDTO.Candidate();
        candidate.setContent(content);

        GeminiResponseDTO mockResponseDTO = new GeminiResponseDTO();
        mockResponseDTO.setCandidates(List.of(candidate));

        when(objectMapperMock.readValue(anyString(), eq(GeminiResponseDTO.class)))
                .thenReturn(mockResponseDTO);


        //parseo final del JSON a DTO intereses
        InteresesGeneradosDTO dto = new InteresesGeneradosDTO();
        dto.setTemaPrincipal("Programación Java");
        dto.setTagsIntereses(Arrays.asList("Spring", "Hibernate"));
        dto.setResumenPerfil("Desarrollador backend.");
        when(jsonParserMock.parsearJsonIntereses(anyString())).thenReturn(dto);

        // metodo real
        geminiAnalysisService.analizarInteraccionesYActualizarGustos(u);

        //
        verify(servicioGustoPersonalMock, times(1)).guardarOActualizar(argThat(g ->
                g.getUsuario().equals(u)
                        && g.getTemaPrincipal().equals("Programación Java")
                        && g.getTagsIntereses().contains("Spring")
                        && g.getResumenPerfil().contains("backend")
        ));
    }

    }

