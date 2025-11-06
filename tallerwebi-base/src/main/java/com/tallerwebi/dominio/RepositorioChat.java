package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioChat {
    ChatMessage guardar(ChatMessage mensaje);
    List<com.tallerwebi.dominio.ChatMessage> obtenerConversacion(Long userA, Long userB);
}