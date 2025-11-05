package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component; // ¡Necesitas esta anotación!
import org.springframework.web.client.RestTemplate;

@Component // ⬅️ Esto la convierte en un bean de Spring
public class GeminiApiClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    // Inyección por Constructor (recomendada)
    @Autowired
    public GeminiApiClient(RestTemplate restTemplate,
                           @Value("${gemini.api-key}") String apiKey,
                           @Value("${gemini.model.name}") String modelName) {

        this.restTemplate = restTemplate;

        // La URL para la API de Gemini
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" +
                modelName + ":generateContent?key=" + apiKey;
    }

    /**
     * Envía el prompt a la API de Gemini y devuelve la respuesta JSON completa.
     */
    public String enviarPromptAGemini(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON de solicitud para Gemini (lo mismo que definimos antes)
// Reemplaza el bloque de texto """...""" con una cadena tradicional:
        String requestJson = String.format(
                "{\n" +
                        "  \"contents\": [\n" +
                        "    {\n" +
                        "      \"parts\": [\n" +
                        "        {\n" +
                        "          \"text\": \"%s\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", prompt.replace("\"", "\\\"").replace("\n", "\\n"));

// Resto del método...

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        // Llamada a la API
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        // Devolvemos el cuerpo de la respuesta, que es un JSON grande
        return response.getBody();
    }
}