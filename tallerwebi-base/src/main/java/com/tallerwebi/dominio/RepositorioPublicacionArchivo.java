package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;


public interface RepositorioPublicacionArchivo {
    ArchivoPublicacion findByPublicacion(Publicacion publicacion);

    ArchivoPublicacion guardarArchivoPublicacion(ArchivoPublicacion archivoPublicacion);
}
