package com.tallerwebi.dominio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

//  CORRECCIÓN: Ignorar campos como "role" que no necesitamos
@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {
    private List<Part> parts;

    // Getters y Setters
    public List<Part> getParts() { return parts; }
    public void setParts(List<Part> parts) { this.parts = parts; }
}