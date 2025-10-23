package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioNotificacion {
    void guardar(Notificacion notificacion);
    List<Notificacion> buscarPorReceptor(Long receptorId);
    void marcarComoLeida(Long id);
}