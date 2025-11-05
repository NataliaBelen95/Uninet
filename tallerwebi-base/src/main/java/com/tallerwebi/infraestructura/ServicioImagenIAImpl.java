package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.ServicioImagenIA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioImagenIAImpl implements ServicioImagenIA {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public ServicioImagenIAImpl(RestTemplate restTemplate,
                                @Value("${image.generation.api.url}") String apiUrl,
                                @Value("${image.generation.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
    }

//    @Override
//    public String generarImagenRelacionada(String textoCompletoAnuncio) throws Exception {
//        String promptParaImagen = "Crear un poster universitario moderno y atractivo para un evento: " + textoCompletoAnuncio;

    /// / ⬅ Usa el texto del anuncio para crear un prompt visual de alta calidad
//        // 1. Construir el Cuerpo de la Solicitud (JSON SIMPLE Y ROBUSTO)
//        // 🔑 Usamos los campos esenciales que la API de Imagen necesita:
//        String requestJson = String.format(
//                "{\n" +
//                        "  \"model\": \"imagen-3.0-generate-002\",\n" +
//                        "  \"prompt\": \"%s\"\n" +
//                        "}",
//                // Escapar y limpiar saltos de línea para evitar romper el JSON
//                promptParaImagen.replace("\"", "\\\"").replace("\n", " ")
//        );
//
//        // 2. Encabezados
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        // La autenticación va en la URL, pero es buena práctica tenerla en los headers también si fuera necesario
//
//        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
//
//        // 3. Llamada al Endpoint Real de Imagen 3.0
//        // Usamos la URL que configuraste con la clave de API
//        String fullUrl = apiUrl + "?key=" + apiKey;
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    fullUrl,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//            );
//
//            // 4. Procesar la Respuesta (SOLO si es 200 OK)
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                JsonNode root = objectMapper.readTree(response.getBody());
//
//                // 🔑 CORRECCIÓN: Declarar 'artifact' antes del bloque condicional
//                JsonNode artifact = null;
//
//                // ⚠️ NOTA: El camino JSON es una suposición y puede fallar en Image 3.0
//                JsonNode generatedArray = root.path("generated_images");
//
//                if (generatedArray.isArray() && generatedArray.size() > 0) {
//                    // Asumiendo que el campo con la URL es 'image_url' dentro del primer elemento
//                    artifact = generatedArray.path(0).path("image_url");
//                }
//
//                if (artifact != null && !artifact.isMissingNode() && artifact.isTextual()) {
//                    return artifact.asText(); // ⬅️ Devolver la URL REAL generada
//                }
//            }
//        } catch (HttpStatusCodeException e) {
//            // ❌ Esto se lanza si la API falla por error 400 (Auth/Sintaxis)
//            System.err.println("❌ ERROR API IMAGEN (" + e.getStatusCode() + "): La llamada falló.");
//            // Devolvemos el mock para que el Bot guarde la publicación.
//            return "/imagenesPublicidad/default-university-ad.png";
//        } catch (Exception e) {
//            System.err.println("❌ ERROR GENERAL EN SERVICIO IMAGEN: " + e.getMessage());
//            throw e;
//        }
//
//        // Fallback si la API no devuelve una URL válida (requiere una imagen de recurso estático)
//        System.err.println("⚠️ La respuesta de la API fue 200 OK pero no se encontró la URL en el JSON.");
//        return "/imagenesPublicidad/default-university-ad.png";
//    }
//}

// CON IMG MIAS
    @Override
    public String generarImagenRelacionada(String temaPrincipal) throws Exception {

        // 🔑 El prefijo lógico que Spring espera para el Resource Handler
        final String BASE_URL = "/imagenesPublicidad/";

        String temaLimpio = temaPrincipal.toLowerCase();

        System.out.println("🖼️ Generando imagen para el tema: " + temaPrincipal);

        // 🔑 LÓGICA DE MOCK CORREGIDA: Usar el prefijo /imagenesPublicidad/
        if (temaLimpio.contains("programación") || temaLimpio.contains("software") || temaLimpio.contains("tecnología")) {
            // ✅ CORREGIDO
            return BASE_URL + "dev-background.png";
        }
        if (temaLimpio.contains("economía") || temaLimpio.contains("finanzas") || temaLimpio.contains("gestión")) {
            // ✅ CORREGIDO
            return BASE_URL + "financial-chart-art.png";
        }
        if (temaLimpio.contains("matemática") || temaLimpio.contains("algoritmos")) {
            // ✅ CORREGIDO
            return BASE_URL + "math-science-art.png";
        }

        // ✅ CORREGIDO
        return BASE_URL + "default-university-ad.png";
    }

}
