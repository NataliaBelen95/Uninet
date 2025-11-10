package com.tallerwebi.dominio;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.persistence.OneToMany;

@Service
public class GeminiJsonParser {
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiJsonParser(@Qualifier("objectMapperGemini") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public InteresesGeneradosDTO extraerIntereses(String respuestaGemini) throws JsonProcessingException {
        if (respuestaGemini == null || respuestaGemini.isBlank()) {
            throw new IllegalArgumentException("Respuesta vacía de Gemini");
        }

        // Limpieza de formato (por si viene con ```json ... ```)
        String jsonLimpio = respuestaGemini
                .replaceAll("(?s).*?\\{", "{")
                .replaceAll("\\}.*", "}")
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // Verificamos si tiene llaves JSON válidas
        int inicioJson = jsonLimpio.indexOf("{");
        int finJson = jsonLimpio.lastIndexOf("}");
        if (inicioJson == -1 || finJson == -1) {
            throw new IllegalArgumentException("Respuesta de Gemini incompleta o sin formato JSON");
        }

        // Mapeo directo al DTO que guarda los gustos
        return objectMapper.readValue(jsonLimpio, InteresesGeneradosDTO.class);
    }

}
