package com.tallerwebi.dominio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// CORRECCIÓN: Ignorar campos como "usageMetadata"
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDTO {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() { return candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
}