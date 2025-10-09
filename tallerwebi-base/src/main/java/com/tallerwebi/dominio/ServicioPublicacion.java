package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;

import java.util.List;

public interface ServicioPublicacion {


    void realizar(Publicacion publicacion) throws PublicacionFallida;
    List<Publicacion> findAll();
    Publicacion  obtenerPublicacionPorId(long id);

    int obtenerCantidadDeLikes(long id);

}