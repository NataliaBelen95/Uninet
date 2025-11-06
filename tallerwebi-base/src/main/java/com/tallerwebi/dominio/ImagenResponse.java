package com.tallerwebi.dominio;

import java.util.List;

/**
 * DTO que mapea la respuesta JSON de la API de Imagen 3.0.
 */
public class ImagenResponse {

    // Lista de resultados generados
    private List<GeneratedImageResult> generatedImages;

    public List<GeneratedImageResult> getGeneratedImages() {
        return generatedImages;
    }

    public void setGeneratedImages(List<GeneratedImageResult> generatedImages) {
        this.generatedImages = generatedImages;
    }

    // --- Clases Internas para la Estructura JSON ---

    public static class GeneratedImageResult {
        private Image image;

        public Image getImage() { return image; }
        public void setImage(Image image) { this.image = image; }
    }

    public static class Image {
        // El contenido de la imagen codificado en Base64
        private String imageBytes;

        public String getImageBytes() { return imageBytes; }
        public void setImageBytes(String imageBytes) { this.imageBytes = imageBytes; }
    }
}