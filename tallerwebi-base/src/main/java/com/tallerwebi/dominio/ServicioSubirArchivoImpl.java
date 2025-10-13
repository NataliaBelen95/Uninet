package com.tallerwebi.dominio;


import com.tallerwebi.dominio.excepcion.NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;
import com.tallerwebi.dominio.excepcion.NoSePuedeSubirArchivoPorFallaException;

import com.tallerwebi.presentacion.DatosUsuario;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service("ServicioSubirArchivo")
public class ServicioSubirArchivoImpl implements ServicioSubirArchivo {


    @Override
    public String guardarArchivoPdf(MultipartFile archivo, DatosUsuario usuario) {
        //TRY-->MANEJO DE SUBIDA DE ARCHIVOS
        try{
            String basePath = System.getProperty("user.dir");  //  obtengo la Ruta absoluta del proyecto, en donde corre la app
            Path rutaDestino = Paths.get(basePath, "archivos_pdf"); //creo la ruta destino, en donde se deberían guardar los archivos
            Files.createDirectories(rutaDestino);// si la carpeta no existe la crea

            String nombreArchivo = Paths.get(Objects.requireNonNull(archivo.getOriginalFilename())).getFileName().toString(); // obtengo el nombre limpio del archivo sin todas las carpetas previas y eso
            //requiere que no sea null, si no, lanza excepción
            Path destinoFinal = rutaDestino.resolve(nombreArchivo);//crea la ruta completa al archivo destino

            // SEGUNDO TRY --> LECTURA Y GUARDADO DEL ARCHIVO
            try (InputStream inputStream = archivo.getInputStream()) {//abro un flujo de entrada desde el archivo subido
                Files.copy(inputStream, destinoFinal, StandardCopyOption.REPLACE_EXISTING);// copio el archivo en la carpeta destino, si ya existe uno con ese nombre lo sobreescribe
            } catch (Exception e) {//lanza la excepción si no se pudo copiar
                throw new NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException();
            }
            return nombreArchivo;

        }catch (Exception e){
            throw new NoSePuedeSubirArchivoPorFallaException();
        }

    }
}
