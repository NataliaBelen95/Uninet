package com.tallerwebi.dominio;

public interface BotPublisherService {
    void ejecutarCampaniaPublicitariaDirigida();
    /**
     * Dispara el proceso de generación de contenido y publica para un usuario específico.
     * @param targetUserId ID del usuario cuya línea de tiempo recibirá la publicación dirigida.
     */
    void publicarContenidoParaUsuario(long targetUserId);

}