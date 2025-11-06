package com.tallerwebi.infraestructura;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


@Service
@Transactional
public class BotPublisherServiceImpl implements BotPublisherService {

    private final RepositorioGustoPersonal repositorioGustoPersonal;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioPublicacion servicioPublicacion;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final ServicioImagenIA servicioImagenIA;
    private final LuceneService luceneService;

    @Autowired
    public BotPublisherServiceImpl (RepositorioGustoPersonal repositorioGustoPersonal, RepositorioUsuario repositorioUsuario, ServicioPublicacion servicioPublicacion,
                                    GeminiApiClient geminiApiClient, ObjectMapper objectMapper, ServicioImagenIA servicioImagenIA, LuceneService luceneService) {
        this.repositorioGustoPersonal = repositorioGustoPersonal;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioPublicacion = servicioPublicacion;
        this.geminiApiClient = geminiApiClient;
        this.objectMapper = objectMapper;
        this.servicioImagenIA = servicioImagenIA;
        this.luceneService = luceneService;

    }
    @Async("geminiTaskExecutor")
    @Override
    public void publicarContenidoParaUsuario(long targetUserId) {
// 1. Obtener la lista de Bots disponibles
        List<Usuario> bots = repositorioUsuario.buscarBots(); // Usando 0L si no se usa el ID
        if (bots.isEmpty()) {
            System.err.println("No hay bots disponibles para publicar.");
            return;
        }

        // 2. Seleccionar un bot aleatorio (o el primero)
        Usuario botUsuario = bots.get(new Random().nextInt(bots.size()));
        Usuario usuarioReceptor = repositorioUsuario.buscarPorId(targetUserId);

        if (usuarioReceptor == null || botUsuario == null || !botUsuario.isEsBot()) {
            System.err.println("Error: Receptor o Bot no encontrado/configurado.");
            return;
        }
        GustosPersonal gustos = repositorioGustoPersonal.buscarPorUsuario(usuarioReceptor);

        if (gustos == null || gustos.getTagsIntereses().isEmpty()) {
            System.out.println("No hay perfil de gustos IA para publicar contenido dirigido.");
            return;
        }
        String temaPrincipal = gustos.getTemaPrincipal();
        String tags = gustos.getTagsIntereses();
        String resumen = gustos.getResumenPerfil() != null ? gustos.getResumenPerfil() : "Estudiante con curiosidad académica.";

        // 2. Crear el Prompt Avanzado para Generación de Contenido
        String promptGeneracion = String.format(
                "Eres un bot publicitario. Genera un ANUNCIO de una sola frase (máximo 200 caracteres, sin hashtags) " +
                        "sobre un evento o especialización relacionada con '%s' y '%s'. El tono debe ser dirigido a un perfil: '%s'. " +
                        "Devuelve SÓLO el texto del anuncio.",
                temaPrincipal, tags, resumen
        );

        try {
            // 3. GENERAR TEXTO CON GEMINI
            String jsonResponse = geminiApiClient.enviarPromptAGemini(promptGeneracion);
            String contenidoGenerado = extractTextFromGeminiResponse(jsonResponse);

            // 4. 🖼️ GENERAR Y ADJUNTAR IMAGEN
            String urlImagenGenerada = servicioImagenIA.generarImagenRelacionada(contenidoGenerado); // Usa el texto generado

            // 5. Crear la Publicación Final (Solo se crea el objeto, la asignación se hace en el servicio)
            Publicacion nuevaPublicacion = new Publicacion();
            nuevaPublicacion.setDescripcion(contenidoGenerado); // ⬅️ Seteamos la descripción ANTES de pasarla
            nuevaPublicacion.setUsuarioDestinatarioId(usuarioReceptor.getId());
            // 6. 🔑 LLAMADA FINAL: Pasando el Bot como autor
            servicioPublicacion.guardarPubliBot(nuevaPublicacion, botUsuario, urlImagenGenerada); // ✅ CORREGIDO
            //luceneService.setIndexado(false);
            // 7. Mensaje de éxito
            System.out.println("🤖 Bot publicó anuncio dirigido sobre " + temaPrincipal);

        } catch (Exception e) {
            System.err.println("Error en el proceso de publicación del Bot: " + e.getMessage());
        }
        }

    // Dentro de la clase BotPublisherServiceImpl.java

    private String extractTextFromGeminiResponse(String jsonResponse) throws Exception {
        // Asumimos que el campo 'this.objectMapper' es accesible
        GeminiResponseDTO geminiResponse = this.objectMapper.readValue(jsonResponse, GeminiResponseDTO.class);

        String generatedText = geminiResponse.getCandidates().get(0)
                .getContent().getParts().get(0)
                .getText();

        // Limpieza de bloques Markdown (CRÍTICO: remueve ```json\n y ```)
        return generatedText.replace("```json\n", "").replace("```", "").trim();
    }




    @Override
    public void publicarContenidoMasivo() {
        System.out.println("🤖 INICIANDO TAREA DE PUBLICACIÓN MASIVA DE BOTS...");

        // 🔑 Paso 1: Obtener la lista de IDs de todos los usuarios que tienen un perfil de gustos
        // (Esto requiere un método en RepositorioGustoPersonal para listar IDs o perfiles)
        List<Long> usuariosConGusto = repositorioGustoPersonal.listarIdsDeUsuariosConPerfil();

        if (usuariosConGusto.isEmpty()) {
            System.out.println("No se encontraron perfiles de IA para generar publicaciones.");
            return;
        }

        // 🔑 Paso 2: Iterar y disparar el proceso asíncrono para cada usuario
        for (Long userId : usuariosConGusto) {
            // La llamada asíncrona no bloquea el bucle, permitiendo que la tarea se ejecute rápido.
            publicarContenidoParaUsuario(userId);
        }

        System.out.println("🤖 Tarea de publicación en cola para " + usuariosConGusto.size() + " usuarios.");
    }




}
