package com.tallerwebi.dominio;

import java.util.List;

/**
 * DTO que mapea el cuerpo JSON para enviar una petición de
 * generación de imágenes a la API de Imagen 3.0.
 */
public class ImagenRequest {

    private final List<ImagenRequestContainer> requests;

    // Constructor que simplifica la creación con un solo prompt
    public ImagenRequest(String prompt) {
        this.requests = List.of(new ImagenRequestContainer(prompt));
    }

    public List<ImagenRequestContainer> getRequests() {
        return requests;
    }

    // --- Clases Internas para la Estructura JSON ---

    public static class ImagenRequestContainer {
        private final String prompt;
        private final ImagenConfig config;

        public ImagenRequestContainer(String prompt) {
            this.prompt = prompt;
            // Configuración por defecto: 1 imagen, PNG, aspecto 1:1.
            // Esto se puede modificar según los requisitos.
            this.config = new ImagenConfig(1, "image/png", "1:1");
        }

        public String getPrompt() { return prompt; }
        public ImagenConfig getConfig() { return config; }
    }

    public static class ImagenConfig {
        private final int numberOfImages;
        private final String outputMimeType;
        private final String aspectRatio;

        public ImagenConfig(int numberOfImages, String outputMimeType, String aspectRatio) {
            this.numberOfImages = numberOfImages;
            this.outputMimeType = outputMimeType;
            this.aspectRatio = aspectRatio;
        }

        public int getNumberOfImages() { return numberOfImages; }
        public String getOutputMimeType() { return outputMimeType; }
        public String getAspectRatio() { return aspectRatio; }
    }
}