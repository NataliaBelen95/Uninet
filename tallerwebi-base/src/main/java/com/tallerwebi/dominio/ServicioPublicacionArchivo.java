package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ServicioPublicacionArchivo {
    private final RepositorioPublicacionArchivo repositorioPublicacionArchivo;

    @Autowired
    public ServicioPublicacionArchivo(RepositorioPublicacionArchivo repositorioPublicacionArchivo) {
        this.repositorioPublicacionArchivo = repositorioPublicacionArchivo;
    }
    private final String carpetaBase = System.getProperty("user.dir") + "/archivosPublicacion";




    public ArchivoPublicacion guardarArchivo(MultipartFile archivo, Publicacion publicacion) throws IOException {
        Files.createDirectories(Paths.get(carpetaBase));

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path rutaArchivo = Paths.get(carpetaBase, nombreArchivo);

        try (InputStream is = archivo.getInputStream()) {
            Files.copy(is, rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        }

        ArchivoPublicacion archivoPub = new ArchivoPublicacion();
        archivoPub.setNombreArchivo(nombreArchivo);
        archivoPub.setRutaArchivo(rutaArchivo.toString());
        archivoPub.setTipoContenido(archivo.getContentType());
        archivoPub.setPublicacion(publicacion);

        return repositorioPublicacionArchivo.guardarArchivoPublicacion(archivoPub);
    }


}