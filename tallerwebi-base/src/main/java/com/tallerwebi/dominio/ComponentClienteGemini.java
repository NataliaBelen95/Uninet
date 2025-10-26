package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.excepcion.NoSePudoGenerarResumenDelPDFException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class ComponentClienteGemini {

    //primero le paso los valores de aplication properties
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    //el objeto HttpClient es la herramienta que Java te da para realizar peticiones HTTP
    // (como GET, POST, PUT, DELETE) a servicios web externos (como la API de Gemini).
    private static final HttpClient client = HttpClient.newHttpClient();

    //el objeto ObjectMapper proviene de la librería Jackson (que se usa para manejar JSON en Java)
    // y es responsable de la serialización(convertir objetos java en cadena de texto json)
    // y deserialización de JSON [Convierte la cadena de texto JSON que te devuelve Gemini en un
    // objeto Java (Map) para que puedas extraer el texto del resumen (candidates, parts, text).].
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String generarContenido(String instruccion) throws NoSePudoGenerarResumenDelPDFException{
        try{
            //1. Creo el body JSON con el prompt(instruccion)
            Map<String,Object> mensaje=Map.of("parts",new Object[]{
                    Map.of("text",instruccion)
            });
            Map<String, Object> requestBody = Map.of("contents", new Object[] { mensaje });

            String url = geminiApiUrl + "?key=" + apiKey;

            // 2. Crear y ejecutar la petición HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Manejo de la Respuesta y Excepciones
            return parsearRespuesta(response);

        } catch (Exception e) {
            // Captura errores de I/O, JSON, etc.
            String detalleError=("Error de comunicación o parsing con Gemini: " + e.getMessage());
            throw new NoSePudoGenerarResumenDelPDFException(detalleError);
        }
    }

    private String parsearRespuesta(HttpResponse<String> response) throws NoSePudoGenerarResumenDelPDFException {
        int statusCode = response.statusCode();
        String errorBody = response.body();

        // Manejo de Códigos de Error HTTP (incluyendo el 429 de Rate Limit)
        if (statusCode != 200) {
            if (statusCode == 429) {
                String error_429= ("Error 429 (Límite de Cuota/Rate Limit) de Gemini: Has excedido el límite de peticiones.");
                throw new NoSePudoGenerarResumenDelPDFException(error_429);
            }
            // Otros errores HTTP (ej. 400 Bad Request, 500 Server Error)
            String errorCodigoDif=("Error HTTP " + statusCode + " de Gemini. Cuerpo: " + errorBody);
            throw new NoSePudoGenerarResumenDelPDFException(errorCodigoDif);
        }
        // Parsing de la Respuesta 200 OK
        try {
            Map<String, Object> json = objectMapper.readValue(errorBody, Map.class);

            // Verificar error explícito en el cuerpo JSON
            if (json.containsKey("error")) {
                Map<String, Object> errorDetails = (Map<String, Object>) json.get("error");
                String errorMessage = "Error explícito de la API de Gemini: " +(String) errorDetails.get("message");
                throw new NoSePudoGenerarResumenDelPDFException(errorMessage);
            }

            // Extracción del texto del resumen
            var candidates = (java.util.List<Map<String, Object>>) json.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> first = candidates.get(0);

                // Control de bloqueo de contenido (Safety)
                if (first.containsKey("finishReason") && "SAFETY".equals(first.get("finishReason"))) {
                    String contenBloq =("El contenido fue bloqueado por las políticas de seguridad de Gemini.");
                    throw new NoSePudoGenerarResumenDelPDFException(contenBloq);
                }

                Map<String, Object> content = (Map<String, Object>) first.get("content");
                if (content != null) {
                    java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                        return (String) parts.get(0).get("text"); // ÉXITO
                    }
                }
            }

            String rtaInesperada= ("La respuesta de Gemini fue incompleta o inesperada. No se pudo encontrar el texto del resumen.");
            throw new NoSePudoGenerarResumenDelPDFException(rtaInesperada);

        } catch (Exception e) {
            String errorParseo= ("Error al parsear la respuesta JSON de Gemini: " + e.getMessage());
            throw new NoSePudoGenerarResumenDelPDFException(errorParseo);
        }
    }
}









