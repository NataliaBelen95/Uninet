package com.tallerwebi.config;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.registerModule(new JavaTimeModule());  // Registra el módulo JSR310
        objectMapper.findAndRegisterModules();  // Encuentra y registra todos los módulos disponibles
        return objectMapper;
    }
}