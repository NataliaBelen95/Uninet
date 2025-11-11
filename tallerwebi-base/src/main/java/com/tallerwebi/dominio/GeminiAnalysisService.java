package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GeminiAnalysisService {


    private final RepositorioInteraccion repositorioInteraccion;
    private final  ServicioGustoPersonal servicioGustoPersonal;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<Long, Boolean> enAnalisis = new ConcurrentHashMap<>();
    private final ServicioIntegracionIA servicioIntegracionIA;
    private final GeminiJsonParser geminiJsonParser;

    @Autowired
    public GeminiAnalysisService(
            RepositorioInteraccion repositorioInteraccion,

            ServicioGustoPersonal servicioGustoPersonal,  @Qualifier("objectMapperGemini") ObjectMapper objectMapper,
            ServicioIntegracionIA servicioIntegracionIA, GeminiJsonParser geminiJsonParser  ) {

        this.repositorioInteraccion = repositorioInteraccion;
        this.servicioGustoPersonal = servicioGustoPersonal;
        this.objectMapper  = objectMapper;
        this.servicioIntegracionIA = servicioIntegracionIA;
        this.geminiJsonParser = geminiJsonParser;
    }


    @Async("geminiTaskExecutor")
    @Transactional
    public GustosPersonal analizarInteraccionesYActualizarGustos(Usuario usuario) {
        GustosPersonal resultadoFinal = null; // Inicializa el resultado fuera del try
        if (enAnalisis.putIfAbsent(usuario.getId(), true) != null) {
            System.out.println("Ya hay un análisis en curso para el usuario " + usuario.getId());
            return servicioGustoPersonal.buscarPorUsuario(usuario);

        }
        try {
            if (!debeReanalizar(usuario)) {
                System.out.println("Análisis omitido: ya actualizado recientemente.");
                resultadoFinal = servicioGustoPersonal.buscarPorUsuario(usuario);
                return resultadoFinal; // Retorno temprano
            }

            String texto = obtenerTextoInteracciones(usuario);
            if (texto.isEmpty()) {
                System.out.println("No hay interacciones para analizar.");
                resultadoFinal = servicioGustoPersonal.buscarPorUsuario(usuario);
                return resultadoFinal; // Retorno temprano
            }

            String prompt = generarPrompt(texto);
            String respuesta = servicioIntegracionIA.enviarPromptYObtenerJson(prompt);

// Primero parseás la respuesta completa al objeto GeminiResponseDTO
            GeminiResponseDTO geminiResponse = objectMapper.readValue(respuesta, GeminiResponseDTO.class);

// Extraés el texto generado por el modelo (dentro de candidates → content → parts → text)
            String textoGenerado = geminiResponse.getGeneratedText();

// Ahora sí, ese texto es el JSON que querés procesar
            InteresesGeneradosDTO data = geminiJsonParser.parsearJsonIntereses(textoGenerado);


            resultadoFinal = guardarOActualizarGustosDeUsuario(usuario, data);
            System.out.println("Gustos actualizados correctamente para el usuario " + usuario.getId());


        } catch (Exception e) {
            System.err.println("------------------------------------------------------------------------");
            System.err.println(" ERROR AL PROCESAR/GUARDAR GUSTOS DE GEMINI:");
            e.printStackTrace();
            System.err.println("------------------------------------------------------------------------");
            return null;
        } finally {
            // 🔓 Asegura liberar el bloqueo aunque haya error o return antes
            enAnalisis.remove(usuario.getId());
        }
        return resultadoFinal;
    }


    public String generarPrompt(String textoInteracciones) {


        String formatoSalida = "{\n" +
                "  \"temaPrincipal\": \"Ejemplo de Tema Dominante\",\n" +
                "  \"tagsIntereses\": [\"tag1\", \"tag2\", \"tag3\", \"tag4\", \"tag5\"],\n" +
                "  \"resumenPerfil\": \"Breve descripción del perfil del usuario.\"\n" + //
                "}";

        //prompt para pedir resumen
        String prompt = "Eres un especialista en marketing universitario. Analiza el historial de interacciones " +
                "e identifica sus 5 intereses principales, un tema dominante, Y UN RESUMEN BREVE DEL PERFIL. " +

                "\n\nTexto a analizar: " + textoInteracciones +
                "\n\nFormato JSON requerido:\n" + formatoSalida;

        return prompt;
    }

    private boolean debeReanalizar(Usuario usuario) {
        GustosPersonal gustos = servicioGustoPersonal.buscarPorUsuario(usuario);
        if (gustos == null || gustos.getFechaUltimoAnalisis() == null) return true;

        final int HORAS_PARA_REANALIZAR = 6;
        return gustos.getFechaUltimoAnalisis().isBefore(LocalDateTime.now().minusHours(HORAS_PARA_REANALIZAR));
    }

    private String obtenerTextoInteracciones(Usuario usuario) {
        final int LIMITE_INTERACCIONES = 50;
        //Devuelvo true si el último análisis fue hace más de 6 horas,
        //y false si fue hace menos de 6 horas
        return repositorioInteraccion.consolidarTextoInteraccionesRecientes(usuario, LIMITE_INTERACCIONES);
    }

    private GustosPersonal guardarOActualizarGustosDeUsuario(Usuario usuario, InteresesGeneradosDTO data) {
        GustosPersonal gustos = servicioGustoPersonal.buscarPorUsuario(usuario);
        if (gustos == null) {
            gustos = new GustosPersonal();
            gustos.setUsuario(usuario);
        }

        gustos.setTemaPrincipal(data.getTemaPrincipal());
        gustos.setTagsIntereses(String.join(",", data.getTagsIntereses()));
        gustos.setResumenPerfil(data.getResumenPerfil());
        gustos.setFechaUltimoAnalisis(LocalDateTime.now());

        servicioGustoPersonal.guardarOActualizar(gustos);
        return gustos;
    }


    public ServicioGustoPersonal getServicioGustoPersonal() {
        return servicioGustoPersonal;
    }
}