package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BotPublisherService;
import com.tallerwebi.dominio.GeminiAnalysisService;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BotCalendario {
    private final BotPublisherService botPublisherService;
    private final GeminiAnalysisService geminiAnalysisService;
    private final ServicioUsuario servicioUsuario;

    @Autowired
    public BotCalendario(BotPublisherService botPublisherService, GeminiAnalysisService geminiAnalysisService, ServicioUsuario servicioUsuario) {
        this.botPublisherService = botPublisherService;
        this.geminiAnalysisService = geminiAnalysisService;
        this.servicioUsuario = servicioUsuario;
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
        botPublisherService.publicarContenidoMasivo();
        System.out.println("---  SCHEDULER: Tarea de publicación delegada a hilos asíncronos ---");
    }
//@Scheduled(fixedDelay = 60000) 1 min prueba
    @Scheduled(fixedDelay =21600000) //
    public void analizarGustos() {
        System.out.println("--- SCHEDULER: Analizando gustos de usuarios ---");

        List<Usuario> usuarios = servicioUsuario.mostrarTodos();

        for (Usuario usuario : usuarios) {
            try {
                geminiAnalysisService.analizarYGuardarGustos(usuario);
            } catch (Exception e) {
                System.err.println("Error analizando gustos de usuario " + usuario.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("--- SCHEDULER: Análisis completado ---");
    }
}
