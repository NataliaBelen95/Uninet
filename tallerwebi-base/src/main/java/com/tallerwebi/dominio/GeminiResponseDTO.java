package com.tallerwebi.dominio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO que mapea la respuesta JSON de la API de Google Gemini.
 * Adaptado al formato actual (que incluye el campo "role" dentro de "content").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDTO {

    private List<Candidate> candidates;

    /**
     * Devuelve el texto generado por el primer candidato, o un mensaje por defecto.
     */
    /*Acceder a content → parts → text, porque así es como Gemini organiza sus respuestas por diseño*/
    public String getGeneratedText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate candidate = candidates.get(0);
            if (candidate.getContent() != null &&
                    candidate.getContent().getParts() != null &&
                    !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return "Contenido no disponible.";
    }

    // Getters y Setters
    public List<Candidate> getCandidates() { return candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

    // ----------------------------------------------------------------
    // Subclases internas para mapear la estructura completa del JSON
    // ----------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;

        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String role; // Nuevo campo agregado por la API
        private List<Part> parts;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

}
