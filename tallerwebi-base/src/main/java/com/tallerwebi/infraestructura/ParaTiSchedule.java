package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParaTiSchedule {
    private final BotPublisherService botPublisherService;
    private final GeminiAnalysisService geminiAnalysisService;
    private final ServicioUsuario servicioUsuario;
private final LuceneService luceneService;
        private final RepositorioPublicacion repositorioPublicacion;
    @Autowired
    public ParaTiSchedule(BotPublisherService botPublisherService, GeminiAnalysisService geminiAnalysisService,
                          ServicioUsuario servicioUsuario, LuceneService luceneService, RepositorioPublicacion repositorioPublicacion) {
        this.botPublisherService = botPublisherService;
        this.geminiAnalysisService = geminiAnalysisService;
        this.servicioUsuario = servicioUsuario;
        this.luceneService = luceneService;
        this.repositorioPublicacion = repositorioPublicacion;
    }

    /**
     * Tarea programada: Ejecuta la publicación masiva del bot.
     * Se configura para ejecutarse cada 6 horas (21,600,000 milisegundos).
     * Esto asegura que solo uses la cuota de Gemini cuatro veces al día.
     */
    @Scheduled(fixedDelay = 21600000)// ⬅️ Se ejecuta cada 6 horas (en milisegundos)
    public void ejecutarPublicacionMasiva() {
        System.out.println("---  SCHEDULER: Iniciando ciclo de publicación dirigida ---");
        // Llama al método que se encarga de recorrer la lista de usuarios y publicar
        botPublisherService.ejecutarCampaniaPublicitariaDirigida();
        System.out.println("---  SCHEDULER: Tarea de publicación delegada a hilos asíncronos ---");
    }
//@Scheduled(fixedDelay = 60000) 1 min prueba
@Scheduled(fixedDelay = 300000) // 5 minutos
    public void analizarGustos() {
        System.out.println("--- SCHEDULER: Analizando gustos de usuarios ---");

        List<Usuario> usuarios = servicioUsuario.mostrarTodos();

        for (Usuario usuario : usuarios) {
            try {
                geminiAnalysisService.analizarInteraccionesYActualizarGustos(usuario);
            } catch (Exception e) {
                System.err.println("Error analizando gustos de usuario " + usuario.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("--- SCHEDULER: Análisis completado ---");
    }

    //@Scheduled(fixedDelay = 14400000) //4 hs
    @Scheduled(fixedDelay = 60000) //1 min prueba
    public void ejecutarReindexacionLucene() {
        System.out.println("--- SCHEDULER: Iniciando Re-Indexación Masiva de Lucene (Mantenimiento) ---");
        try {
            // 1. Obtiene todas las publicaciones
            List<Publicacion> todas = repositorioPublicacion.listarTodas();

            // 2. Ejecuta la indexación masiva (que corrige inconsistencias)
            luceneService.indexarPublicaciones(todas);

            System.out.println("--- SCHEDULER: Re-Indexación Masiva Completa. Indexados: " + todas.size() + " ---");
        } catch (Exception e) {
            System.err.println("Error durante la re-indexación programada de Lucene: " + e.getMessage());
        }
    }
}
