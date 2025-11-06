package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.GeminiRequest;
import com.tallerwebi.dominio.GeminiResponseDTO;
import com.tallerwebi.dominio.ImagenRequest;
import com.tallerwebi.dominio.ImagenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de integración unificado para la API de Google (Texto y Generación de Imágenes).
 * Esta clase maneja las llamadas HTTP de bajo nivel.
 */
@Service
public class ServicioIntegracionIA {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String geminiTextApiUrl;
  //  private final String imagenApiUrl;
  //  private final String imagenApiKey;
    private final String imageBaseDirectory; // Ya no es final por la corrección de inicialización

    @Autowired
    public ServicioIntegracionIA(
            RestTemplate restTemplate,
            @Qualifier("objectMapperGemini") ObjectMapper objectMapper,
            // Configuración para la API de Texto (Gemini Flash)
            @Value("${gemini.model.name}") String geminiModelText,
            @Value("${gemini.base-url}") String geminiBaseUrl,
            @Value("${gemini.api-key}") String geminiApiKey,
            // Configuración para la API de Imagen (Imagen 3.0)
            @Value("${image.generation.api.url}") String imagenApiUrl,
            @Value("${image.generation.api.key}") String imagenApiKey,
            @Value("${image.generation.path.base}") String imageBaseDirectory,
            ResourceLoader resourceLoader) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.geminiTextApiUrl = geminiBaseUrl + "models/" + geminiModelText + ":generateContent?key=" + geminiApiKey;
        //this.imagenApiKey = imagenApiKey;
        //this.imagenApiUrl = imagenApiUrl + "?key=" + imagenApiKey; // La clave API se añade en el header para este endpoint

        // ⚠️ INICIALIZACIÓN DE VARIABLES FINALES/NO FINALES


        // CORRECCIÓN DE ERROR DE VARIABLE FINAL (Se inicializa una vez)
        String finalImagePath;
        try {
            // Usa la ruta base inyectada para construir el Path absoluto
            Path path = Paths.get(imageBaseDirectory).toAbsolutePath();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            finalImagePath = path.toString() + File.separator;
            System.out.println("Directorio de imágenes IA configurado en: " + finalImagePath);

        } catch (IOException e) {
            System.err.println("Error al configurar el directorio de imágenes IA. Se usará un fallback simple: " + e.getMessage());
            finalImagePath = "imagenesGeneradas" + File.separator; // Fallback
        }

        // Asignación ÚNICA/Inicial al campo de la clase
        this.imageBaseDirectory = finalImagePath;
    }

    /**
     * Envía un prompt a la API de texto de Gemini (gemini-2.5-flash) y devuelve
     * la respuesta JSON completa (String). Es utilizado por GeminiAnalysisService.
     */
    public String enviarPromptYObtenerJson(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Uso de DTOs para la serialización de la petición de texto
        GeminiRequest request = new GeminiRequest(prompt);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(geminiTextApiUrl, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error al llamar a la API de texto de Gemini: " + e.getMessage());
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }



    /**
     * Genera una imagen usando Imagen 3.0, la guarda localmente y devuelve la URL relativa.
     */
//    public String generarImagenUrl(String prompt) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // NO usamos Bearer Token, ya que la clave API está en el URL
//
//        ImagenRequest request = new ImagenRequest(prompt);
//        HttpEntity<ImagenRequest> entity = new HttpEntity<>(request, headers);
//
//        try {
//            // 1. Llamar a la API de Imagen 3.0
//            ResponseEntity<ImagenResponse> response = restTemplate.postForEntity(imagenApiUrl, entity, ImagenResponse.class);
//
//            ImagenResponse body = response.getBody();
//
//            if (body == null || body.getGeneratedImages() == null || body.getGeneratedImages().isEmpty() ||
//                    body.getGeneratedImages().get(0).getImage() == null || body.getGeneratedImages().get(0).getImage().getImageBytes() == null) {
//
//                System.err.println("Respuesta de Imagen 3.0 vacía o incompleta. Cuerpo: " + (body != null ? objectMapper.writeValueAsString(body) : "NULO"));
//                return "/img/placeholder.png"; // Fallback
//            }
//
//            // 2. Obtener los bytes codificados en Base64
//            String base64Image = body.getGeneratedImages().get(0).getImage().getImageBytes();
//
//            // 3. Decodificar Base64 a bytes binarios
//            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
//
//            // 4. Guardar el archivo localmente
//            String fileName = UUID.randomUUID().toString() + ".png";
//            Path filePath = Paths.get(this.imageBaseDirectory, fileName);
//
//            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
//                fos.write(imageBytes);
//            }
//
//            // 5. Devolver la URL relativa (ej: /imagenesGeneradas/abc-123.png)
//            // Esto asume que la configuración de Spring MVC sirve la carpeta 'imagenesGeneradas/'
//            return "/" + Paths.get(this.imageBaseDirectory).getFileName().toString() + "/" + fileName;
//
//        } catch (Exception e) {
//            System.err.println("Error en el proceso de generación/guardado de imagen: " + e.getMessage());
//            e.printStackTrace();
//            return "/img/placeholder.png"; // Fallback
//        }
//    }
}