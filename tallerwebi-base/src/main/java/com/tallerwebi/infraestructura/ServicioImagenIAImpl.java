package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.ServicioImagenIA;
import com.tallerwebi.dominio.ServicioIntegracionIA;
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
    private final ServicioIntegracionIA servicioIntegracionIA;

    public ServicioImagenIAImpl(RestTemplate restTemplate,
                                @Value("${image.generation.api.url}") String apiUrl,
                                @Value("${image.generation.api.key}") String apiKey,
                                ServicioIntegracionIA servicioIntegracionIA) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.servicioIntegracionIA = servicioIntegracionIA;
    }


//    @Override
//    public String generarImagenRelacionada(String temaPrincipal) throws Exception {
//
//        // 1. Crear un prompt más detallado para obtener mejores resultados de la IA
//        String promptDetallado = String.format(
//                "Genera una imagen publicitaria vibrante y moderna, con un estilo de arte digital minimalista, " +
//                        "que represente el concepto de '%s' en un contexto universitario o tecnológico. " +
//                        "Debe ser un fondo abstracto, limpio y atractivo, listo para un anuncio.",
//                temaPrincipal
//        );
//
//        // 2. Llamar al servicio que maneja la API, el Base64 y el guardado de archivos.
//        // Este método devuelve la URL pública del archivo guardado.
//        return servicioIntegracionIA.generarImagenUrl(promptDetallado);
//    }


   public String generarImagenRelacionada(String temaPrincipal) throws Exception {

      //  El prefijo lógico que Spring espera para el Resource Handler
      final String BASE_URL = "/imagenesPublicidad/";

        String temaLimpio = temaPrincipal.toLowerCase();

        System.out.println("🖼️ Generando imagen para el tema: " + temaPrincipal);
//        //  LÓGICA DE MOCK CORREGIDA: Usar el prefijo /imagenesPublicidad/
        if (temaLimpio.contains("programación") || temaLimpio.contains("software") || temaLimpio.contains("tecnología")) {

            return BASE_URL + "dev-background.png";
        }
        if (temaLimpio.contains("economía") || temaLimpio.contains("finanzas") || temaLimpio.contains("gestión")) {

            return BASE_URL + "financial-chart-art.png";
        }
       if (temaLimpio.contains("matemática") || temaLimpio.contains("algoritmos")) {

            return BASE_URL + "math-science-art.png";
        }


       return BASE_URL + "default-university-ad.png";
   }
}


