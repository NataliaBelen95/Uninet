// Archivo: Candidate.java

package com.tallerwebi.dominio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ⬅️ ¡Nueva Importación!

// 🔑 CORRECCIÓN: Ignorar campos como "finishReason" e "index"
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candidate {
    private Content content;

    // Getters y Setters
    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }
}