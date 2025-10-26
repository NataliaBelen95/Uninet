package com.tallerwebi.infraestructura.config;

import com.tallerwebi.presentacion.WebSocketController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;

@Configuration
public class WebSocketTestConfig {


    @Bean
    @Qualifier("brokerMessagingTemplate")
    public SimpMessagingTemplate brokerMessagingTemplate() {
        return mock(SimpMessagingTemplate.class);
    }


    @Bean
    public WebSocketController webSocketController(SimpMessagingTemplate simpMessagingTemplate) {
        return new WebSocketController(simpMessagingTemplate);
    }


}