package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ServicioMostrarArchivosSubidosImpl  implements ServicioMostrarArchivosSubidos {

    private final String RUTA_ARCHIVOS = "archivos_pdf";

    @Override
    public List<String> listarArchivosPdf() {
        File carpeta = new File(RUTA_ARCHIVOS);
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

