package com.tallerwebi.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.GeminiJsonParser;
import com.tallerwebi.dominio.InteresesGeneradosDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ServicioGeminiJsonParserTest {

    private ObjectMapper objectMapper;
    private GeminiJsonParser geminiJsonParser;

    @BeforeEach
    public void init() {
        this.objectMapper = new ObjectMapper();
        geminiJsonParser = new GeminiJsonParser(objectMapper);
    }

    @Test
    public void poderExtraerInteresesEnJsonLimpio () throws JsonProcessingException {
        String respuestaGeminiEnvuelta =
                "```json\n" + // Formato Markdown que debe ser limpiado
                        "{\n" +
                        "    \"temaPrincipal\": \"Desarrollo Web\",\n" +
                        "    \"tagsIntereses\": [\"Java\", \"Spring\", \"JavaScript\", \"Testing\"],\n" +
                        "    \"resumenPerfil\": \"Usuario con fuerte interés en backend y frontend.\"\n" +
                        "}\n" +
                        "```";
        InteresesGeneradosDTO data = geminiJsonParser.parsearJsonIntereses(respuestaGeminiEnvuelta);
        assertNotNull(data);
        assertEquals("Desarrollo Web", data.getTemaPrincipal());
        assertEquals(4, data.getTagsIntereses().size());
        assertTrue(data.getTagsIntereses().contains("Spring"));


    }


    @Test
    public void queLanceExcepcionSiLaRespuestaNoTieneFormatoJsonValido() {
        String respuestaMala = "Texto plano sin llaves ni formato JSON";


        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            geminiJsonParser.parsearJsonIntereses(respuestaMala);
        });
    }



}
