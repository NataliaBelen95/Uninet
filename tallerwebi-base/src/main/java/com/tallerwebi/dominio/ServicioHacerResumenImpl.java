package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoSePudoExtraerElTextoDelPDFException;
import com.tallerwebi.dominio.excepcion.NoSePudoGenerarResumenDelPDFException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
public class ServicioHacerResumenImpl implements ServicioHacerResumen {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";
    private static final String API_KEY = "AIzaSyDD38z_bbC2QOt36pppeU0fp19vHxvwq2I"; // Aquí pones tu API Key

    @Override
    public String extraerTexto(String rutaArchivo) throws NoSePudoExtraerElTextoDelPDFException {
        File archivoPDF = new File(rutaArchivo);
        //con esto voy a intentar abrir el pdf para poder leerlo
        try (PDDocument documento = PDDocument.load(archivoPDF)) {
            PDFTextStripper stripper = new PDFTextStripper();//esto extrae tdo el texto del pdf
            return stripper.getText(documento);//devuelvo el texto como un string
        } catch (Exception e) {
            throw new NoSePudoExtraerElTextoDelPDFException();
        }
    }

    @Override
    public String generarResumen(String texto) {
        try {
            // Creamos la request a Gemini
            String prompt = "Generá un resumen detallado de este texto usando formato **Markdown** (con encabezados, negritas y listas) para hacerlo más legible. El resumen debe estar estructurado en secciones:\n" + texto;

            Map<String, Object> message = Map.of("parts", new Object[] {
                    Map.of("text", prompt) // Usa la variable prompt con la nueva instrucción
            });
            Map<String, Object> requestBody = Map.of("contents", new Object[] { message });

            String url = GEMINI_API_URL + API_KEY;

            // Usamos HttpClient para hacer la petición POST
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta de Gemini:");
            System.out.println(response.body());

            // --- 2. Validación y Parsing de la Respuesta ---

            // Si el código de estado HTTP no es 200 (OK), lanzamos una excepción
            if (response.statusCode() != 200) {
                String errorBody = response.body();
                String errorMessage = "Error HTTP " + response.statusCode() + " de Gemini. Cuerpo: " + (errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody);
                System.out.println(errorMessage);
                throw new NoSePudoGenerarResumenDelPDFException();
            }

            // Parseamos la respuesta JSON y extraemos el resumen
            // Aquí dependemos de cómo devuelve Gemini el texto final
            Map<String, Object> json = new ObjectMapper().readValue(response.body(), Map.class);

            // 3. Verificar si el JSON contiene un campo 'error' (aunque el status sea 200, a veces se incluye)
            if (json.containsKey("error")) {
                Map<String, Object> errorDetails = (Map<String, Object>) json.get("error");
                String errorMessage = (String) errorDetails.get("message");
                System.out.println("Error explícito de la API de Gemini: " +errorMessage);
                throw new NoSePudoGenerarResumenDelPDFException();
            }
            // 4. Intentar extraer el resumen de 'candidates' (Respuesta exitosa)
            var candidates = (java.util.List<Map<String, Object>>) json.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> first = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) first.get("content");

                // Verificar si hay bloqueo de contenido antes de intentar obtener 'parts'
                if (first.containsKey("finishReason") && "SAFETY".equals(first.get("finishReason"))) {
                    System.out.println("El contenido fue bloqueado por las políticas de seguridad de Gemini.");
                    throw new NoSePudoGenerarResumenDelPDFException();
                }

                if (content != null) {
                    java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                        return (String) parts.get(0).get("text"); // ÉXITO
                    }
                }
            }

            // 5. Si no se pudo extraer el texto, lanzamos la excepción
            System.out.println("La respuesta de Gemini fue incompleta o inesperada. No se pudo encontrar el texto del resumen.");
            throw new NoSePudoGenerarResumenDelPDFException();

        } catch (NoSePudoGenerarResumenDelPDFException e) {
            // Propagamos las excepciones específicas que ya lanzamos
            throw e;
        } catch (Exception e) {
            // Captura cualquier otro error (I/O, parsing, etc.) y lo convierte en la excepción de dominio
            // Nota: Aquí podrías incluir un logger para guardar el error original (e)
            System.err.println("Error general en generarResumen: Fallo al comunicarse o procesar la respuesta del servicio externo. " + e.getMessage());
            throw new NoSePudoGenerarResumenDelPDFException();
        }
    }
}


