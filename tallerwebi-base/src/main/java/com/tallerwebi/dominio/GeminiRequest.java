package com.tallerwebi.dominio;

import java.util.List;

/**
 * Data Transfer Object (DTO) que mapea el cuerpo JSON para enviar una petición
 * de generación de texto a la API de Gemini (generateContent).
 */
public class GeminiRequest {

    private final List<Content> contents;

    // Constructor para simplificar la creación con un solo prompt
    public GeminiRequest(String prompt) {
        this.contents = List.of(new Content(prompt));
    }

    public List<Content> getContents() {
        return contents;
    }

    // --- Clases Internas para la Estructura JSON ---

    public static class Content {
        // Rol siempre será 'user' para la petición
        private final String role = "user";
        private  List<Part> parts;

        public Content(String prompt) {
            this.parts = List.of(new Part(prompt));
        }

        public String getRole() { return role; }
        public List<Part> getParts() { return parts; }

        // Constructor sin argumentos, necesario para la deserialización de Jackson
        public Content() {}
    }

    public static class Part {
        private  String text;

        public Part(String text) { this.text = text; }

        public String getText() { return text; }

        // Constructor sin argumentos, necesario para la deserialización de Jackson
        public Part() {}
    }

    // Constructor sin argumentos
    public GeminiRequest() {
        this.contents = null;
    }
}