package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BotCalendario {
    private final BotPublisherService botPublisherService;

    @Autowired
    public BotCalendario(BotPublisherService botPublisherService) {
        this.botPublisherService = botPublisherService;
    }

    /**
     * Tarea programada: Ejecuta la publicación masiva del bot.
     * Se configura para ejecutarse cada 6 horas (21,600,000 milisegundos).
     * Esto asegura que solo uses la cuota de Gemini cuatro veces al día.
     */
    @Scheduled(fixedDelay = 21600000)// ⬅️ Se ejecuta cada 6 horas (en milisegundos)
    public void ejecutarPublicacionMasiva() {
        System.out.println("--- 🤖 SCHEDULER: Iniciando ciclo de publicación dirigida ---");
        // Llama al método que se encarga de recorrer la lista de usuarios y publicar
        botPublisherService.publicarContenidoMasivo();
        System.out.println("--- 🤖 SCHEDULER: Tarea de publicación delegada a hilos asíncronos ---");
    }
}
