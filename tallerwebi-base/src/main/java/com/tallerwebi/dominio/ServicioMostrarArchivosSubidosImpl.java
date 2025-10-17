package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ServicioMostrarArchivosSubidosImpl  implements ServicioMostrarArchivosSubidos {

    private final String RUTA_CARPETA_ABSOLUTA;

    public ServicioMostrarArchivosSubidosImpl() {
        String basePath= System.getProperty("user.dir");
        this.RUTA_CARPETA_ABSOLUTA= Paths.get(basePath, "archivos_pdf").toString();
    }

    @Override
    public List<String> listarArchivosPdf() {
        File carpeta = new File(RUTA_CARPETA_ABSOLUTA);
        List<String> nombresArchivos = new ArrayList<>();

        if(carpeta.exists() && carpeta.isDirectory()) {
            for(File file : Objects.requireNonNull(carpeta.listFiles())) {
                if(file.isFile() && file.getName().endsWith(".pdf")) {
                    nombresArchivos.add(file.getName());
                }
            }
        }

        return nombresArchivos;
    }
}

