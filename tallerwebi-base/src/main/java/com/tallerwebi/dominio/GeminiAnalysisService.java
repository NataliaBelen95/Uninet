package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async; // ⬅️ IMPORTACIÓN CLAVE para resolver la lentitud
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GeminiAnalysisService {

    // ➡️ Todos los campos deben ser 'final' si usas inyección por constructor
    // pero deben ser declarados:
    private final RepositorioInteraccion repositorioInteraccion;
    private final GeminiApiClient geminiApiClient;
    // Asumiendo que tu interfaz es RepositorioGustoPersonal (verificar la 's' o no)
    private final  ServicioGustoPersonal servicioGustoPersonal;
    private final ObjectMapper objectMapper;
    private final ServicioUsuario servicioUsuario;
    private final ConcurrentMap<Long, Boolean> enAnalisis = new ConcurrentHashMap<>();

    @Autowired
    public GeminiAnalysisService(
            RepositorioInteraccion repositorioInteraccion,
            GeminiApiClient geminiApiClient,
           ServicioGustoPersonal servicioGustoPersonal, ObjectMapper objectMapper,
            ServicioUsuario servicioUsuario ) { // ⬅️ ¡La dependencia faltante!

        this.repositorioInteraccion = repositorioInteraccion;
        this.geminiApiClient = geminiApiClient;
        this.servicioGustoPersonal = servicioGustoPersonal;
        this.objectMapper  = objectMapper;
        this.servicioUsuario = servicioUsuario;
    }

    @Async("geminiTaskExecutor")
    @Transactional
    public GustosPersonal analizarYGuardarGustos(Usuario usuario) {
        // Evita ejecutar dos análisis en paralelo para el mismo usuario
        if (enAnalisis.putIfAbsent(usuario.getId(), true) != null) {
            System.out.println("Ya hay un análisis en curso para el usuario " + usuario.getId());
            return null;
        }

        try {
            // 1️⃣ Control de frecuencia (no más de una vez cada 6h)
            GustosPersonal gustosExistentes = servicioGustoPersonal.buscarPorUsuario(usuario);
            final int HORAS_PARA_REANALIZAR = 6;

            if (gustosExistentes != null &&
                    gustosExistentes.getFechaUltimoAnalisis() != null &&
                    gustosExistentes.getFechaUltimoAnalisis().isAfter(LocalDateTime.now().minusHours(HORAS_PARA_REANALIZAR))) {

                System.out.println("Análisis omitido: ya actualizado recientemente.");
                return gustosExistentes;
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
            String respuestaGemini = geminiApiClient.enviarPromptAGemini(promptCompleto);

            GeminiResponseDTO geminiResponse = objectMapper.readValue(respuestaGemini, GeminiResponseDTO.class);
            String interesesJsonString = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
            String jsonLimpio = interesesJsonString.replace("```json\n", "").replace("```", "").trim();

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

        } catch (Exception e) {
            System.err.println("------------------------------------------------------------------------");
            System.err.println(" ERROR AL PROCESAR/GUARDAR GUSTOS DE GEMINI:");
            e.printStackTrace();
            System.err.println("------------------------------------------------------------------------");
        } finally {
            // 🔓 Asegura liberar el bloqueo aunque haya error o return antes
            enAnalisis.remove(usuario.getId());
        }
        return null;
    }

    // El método generarPrompt está correcto tal como lo tienes
    public String generarPrompt(String textoInteracciones) {

        // 1.  Incluir el nuevo campo en el formato de salida
        String formatoSalida = "{\n" +
                "  \"temaPrincipal\": \"Ejemplo de Tema Dominante\",\n" +
                "  \"tagsIntereses\": [\"tag1\", \"tag2\", \"tag3\", \"tag4\", \"tag5\"],\n" +
                "  \"resumenPerfil\": \"Breve descripción del perfil del usuario.\"\n" + // ⬅️ Nuevo campo
                "}";

        // 2.  Modificar la instrucción para pedir el resumen
        String prompt = "Eres un especialista en marketing universitario. Analiza el historial de interacciones " +
                "y genera una publicación publicitaria CORTA (máximo 200 caracteres, sin hashtags) que use una LLAMADA A LA ACCIÓN directa." +
                "El anuncio debe motivar al usuario a inscribirse o aprender más sobre el tema clave: " +
                "Identifica sus 5 intereses principales, un tema dominante, Y UN RESUMEN BREVE DEL PERFIL. " +
                "Utiliza frases como '¡Aprende con nosotros!' o '¡Inscríbete hoy!' en el contexto de la universidad." + // ⬅️ INSTRUCCIÓN DE CTA
                "\n\nTexto a analizar: " + textoInteracciones +
                "\n\nFormato JSON requerido:\n" + formatoSalida;

        return prompt;
    }
    public ServicioGustoPersonal getServicioGustoPersonal() {
        return servicioGustoPersonal;
    }
}