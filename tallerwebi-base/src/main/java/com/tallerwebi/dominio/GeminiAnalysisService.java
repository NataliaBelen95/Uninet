package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async; // ⬅️ IMPORTACIÓN CLAVE para resolver la lentitud
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GeminiAnalysisService {

    // ➡Todos los campos deben ser 'final' si usas inyección por constructor
    // pero deben ser declarados:
    private final RepositorioInteraccion repositorioInteraccion;
    //private final GeminiApiClient geminiApiClient;
    // Asumiendo que tu interfaz es RepositorioGustoPersonal (verificar la 's' o no)
    private final  ServicioGustoPersonal servicioGustoPersonal;
    private final ObjectMapper objectMapper;
    private final ServicioUsuario servicioUsuario;
    private final ConcurrentMap<Long, Boolean> enAnalisis = new ConcurrentHashMap<>();
    private final ServicioIntegracionIA servicioIntegracionIA;

    @Autowired
    public GeminiAnalysisService(
            RepositorioInteraccion repositorioInteraccion,

            ServicioGustoPersonal servicioGustoPersonal,  @Qualifier("objectMapperGemini") ObjectMapper objectMapper,
            ServicioUsuario servicioUsuario, ServicioIntegracionIA servicioIntegracionIA ) { // ⬅️ ¡La dependencia faltante!

        this.repositorioInteraccion = repositorioInteraccion;
        //this.geminiApiClient = geminiApiClient;
        this.servicioGustoPersonal = servicioGustoPersonal;
        this.objectMapper  = objectMapper;
        this.servicioUsuario = servicioUsuario;
        this.servicioIntegracionIA = servicioIntegracionIA;
    }

    @Async("geminiTaskExecutor")
    @Transactional
    public GustosPersonal analizarYGuardarGustos(Usuario usuario) {
        // Evita ejecutar dos análisis en paralelo para el mismo usuario
        if (enAnalisis.putIfAbsent(usuario.getId(), true) != null) {
            System.out.println("Ya hay un análisis en curso para el usuario " + usuario.getId());
            return servicioGustoPersonal.buscarPorUsuario(usuario);
        }
        GustosPersonal resultadoFinal = null;
        try {
            // 1️⃣ Control de frecuencia (no más de una vez cada 6h)
            GustosPersonal gustosExistentes = servicioGustoPersonal.buscarPorUsuario(usuario);
            final int HORAS_PARA_REANALIZAR = 6;

            if (gustosExistentes != null &&
                    gustosExistentes.getFechaUltimoAnalisis() != null &&
                    gustosExistentes.getFechaUltimoAnalisis().isAfter(LocalDateTime.now().minusHours(HORAS_PARA_REANALIZAR))) {

                System.out.println("Análisis omitido: ya actualizado recientemente.");
                resultadoFinal = gustosExistentes;
                return resultadoFinal;
            }

            // 2️⃣ Recolectar interacciones
            final int LIMITE_INTERACCIONES = 50;
            String textoParaAnalizar = repositorioInteraccion.consolidarTextoInteraccionesRecientes(usuario, LIMITE_INTERACCIONES);

            if (textoParaAnalizar.isEmpty()) {
                System.out.println("No hay interacciones para analizar.");
                return gustosExistentes;
            }

            // 3️⃣ Generar prompt y enviar a Gemini
            String promptCompleto = generarPrompt(textoParaAnalizar);
            String respuestaGemini = servicioIntegracionIA.enviarPromptYObtenerJson(promptCompleto);
            // ⬇️ 4️⃣ LÓGICA DE EXTRACCIÓN SEGURA DEL JSON ⬇️
            String respuestaCompleta = respuestaGemini;
            int inicioJson = respuestaCompleta.indexOf('{');
            int finJson = respuestaCompleta.lastIndexOf('}');
            if (inicioJson == -1 || finJson == -1) {
                throw new IllegalStateException("Respuesta de Gemini incompleta o sin formato JSON.");
            }

            GeminiResponseDTO geminiResponse = objectMapper.readValue(respuestaGemini, GeminiResponseDTO.class);
            String interesesJsonString = geminiResponse.getGeneratedText();
            if (interesesJsonString == null || interesesJsonString.isBlank()) {
                System.err.println("Gemini no devolvió texto válido para analizar gustos.");
                return gustosExistentes;
            }
            String jsonLimpio = interesesJsonString
                    .replaceAll("(?s).*?\\{", "{") // elimina texto antes del primer '{'
                    .replaceAll("\\}.*", "}")     // elimina texto después del último '}'
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            System.out.println("JSON limpio de gustos:\n" + jsonLimpio);

            InteresesGeneradosDTO data = objectMapper.readValue(jsonLimpio, InteresesGeneradosDTO.class);

            // 4️⃣ Actualizar o crear el registro
            GustosPersonal gustos = gustosExistentes != null ? gustosExistentes : new GustosPersonal();
            gustos.setUsuario(usuario);
            gustos.setTemaPrincipal(data.getTemaPrincipal());
            gustos.setTagsIntereses(String.join(",", data.getTagsIntereses()));
            gustos.setResumenPerfil(data.getResumenPerfil());
            gustos.setFechaUltimoAnalisis(LocalDateTime.now());

            servicioGustoPersonal.guardarOActualizar(gustos);

            System.out.println("Gustos actualizados correctamente para el usuario " + usuario.getId());
            return gustos;
        } catch (Exception e) {
            System.err.println("------------------------------------------------------------------------");
            System.err.println(" ERROR AL PROCESAR/GUARDAR GUSTOS DE GEMINI:");
            e.printStackTrace();
            System.err.println("------------------------------------------------------------------------");
        } finally {
            // 🔓 Asegura liberar el bloqueo aunque haya error o return antes
            enAnalisis.remove(usuario.getId());
        }
        return resultadoFinal;
    }

    // El método generarPrompt está correcto tal como lo tienes
    public String generarPrompt(String textoInteracciones) {

        // 1.  Incluir el nuevo campo en el formato de salida
        String formatoSalida = "{\n" +
                "  \"temaPrincipal\": \"Ejemplo de Tema Dominante\",\n" +
                "  \"tagsIntereses\": [\"tag1\", \"tag2\", \"tag3\", \"tag4\", \"tag5\"],\n" +
                "  \"resumenPerfil\": \"Breve descripción del perfil del usuario.\"\n" + // ⬅️ Nuevo campo
                "}";

        //prompt para pedirl resumen
        String prompt = "Eres un especialista en marketing universitario. Analiza el historial de interacciones " +
                "e identifica sus 5 intereses principales, un tema dominante, Y UN RESUMEN BREVE DEL PERFIL. " +

                "\n\nTexto a analizar: " + textoInteracciones +
                "\n\nFormato JSON requerido:\n" + formatoSalida;

        return prompt;
    }
    public ServicioGustoPersonal getServicioGustoPersonal() {
        return servicioGustoPersonal;
    }
}