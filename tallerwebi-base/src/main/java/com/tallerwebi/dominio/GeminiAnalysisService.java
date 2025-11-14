package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.excepcion.ExtraccionTextoFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final ServicioArchivoUtils  servicioArchivosUtils;

    @Autowired
    public GeminiAnalysisService(
            RepositorioInteraccion repositorioInteraccion,

            ServicioGustoPersonal servicioGustoPersonal,  @Qualifier("objectMapperGemini") ObjectMapper objectMapper,
            ServicioIntegracionIA servicioIntegracionIA, GeminiJsonParser geminiJsonParser, ServicioArchivoUtils servicioArchivoutils  ) {

        this.repositorioInteraccion = repositorioInteraccion;
        this.servicioGustoPersonal = servicioGustoPersonal;
        this.objectMapper  = objectMapper;
        this.servicioIntegracionIA = servicioIntegracionIA;
        this.geminiJsonParser = geminiJsonParser;
        this.servicioArchivosUtils = servicioArchivoutils;
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

            String texto = obtenerTextoCompletoParaAnalisis(usuario);
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
// GeminiAnalysisService.java (NUEVO MÉTODO DE CONSOLIDACIÓN)

    private String obtenerTextoCompletoParaAnalisis(Usuario usuario) {
        final int LIMITE_INTERACCIONES = 50;

        // Obtener la lista de interacciones COMPLETAS (con Publicación y Archivo)
        List<Interaccion> interacciones = repositorioInteraccion.obtenerPublicacionesRecientesConArchivo(usuario, LIMITE_INTERACCIONES); // Asumiendo este nuevo nombre

        StringBuilder textoConsolidado = new StringBuilder();

        for (Interaccion i : interacciones) {
            // 1. Añadir el contenido del comentario/interacción si existe
            if (i.getContenido() != null && !i.getContenido().isEmpty()) {
                textoConsolidado.append(i.getContenido()).append(". ");
            }

            // 2. Revisar si la Publicación asociada tiene un PDF y extraer el texto
            Publicacion p = i.getPublicacion();
            ArchivoPublicacion archivo = p != null ? p.getArchivo() : null;

            if (archivo != null && "application/pdf".equals(archivo.getTipoContenido())) {
                try {
                    String textoPdf = servicioArchivosUtils.extraerTextoDePdf(archivo.getRutaArchivo());
                    textoConsolidado.append("\n\n [CONTENIDO PDF] ").append(textoPdf);
                } catch (ExtraccionTextoFallida e) {
                    System.err.println("Error al extraer texto del PDF: " + e.getMessage());
                }
            }
        }

        return textoConsolidado.toString().trim();
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