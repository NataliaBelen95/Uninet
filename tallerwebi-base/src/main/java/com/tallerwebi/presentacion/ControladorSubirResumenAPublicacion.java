package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.infraestructura.ServicioSubirResumenAPublicacion;
import com.tallerwebi.infraestructura.ServicioSubirResumenAPublicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;  // CORRECTO PARA PRODUCCIÓN

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Controller
public class ControladorSubirResumenAPublicacion {
    private ServicioUsuario servicioUsuario;
    private ServicioSubirResumenAPublicacion servicioSubirResumenAPublicacion  ;
    private ServicioPublicacion servicioPublicacion;
    private ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;

    @Autowired
    public ControladorSubirResumenAPublicacion(ServicioUsuario servicioUsuario, ServicioSubirResumenAPublicacion servicioSubirResumenAPublicacion, ServicioMostrarArchivosSubidos ServicioMostrarArchivosSubidos, ServicioPublicacion  servicioPublicacion) {
        this.servicioUsuario = servicioUsuario;
        this.servicioSubirResumenAPublicacion= servicioSubirResumenAPublicacion;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioMostrarArchivosSubidos = ServicioMostrarArchivosSubidos;
    }
    @PostMapping("/herramientas-IA/compartir-resumen")
    public ModelAndView compartirResumenComoPublicacion(
            @SessionAttribute("usuarioLogueado") DatosUsuario usuario,
            @RequestParam("resumen") String resumen,  // Recibimos el resumen en formato texto (HTML)
            @RequestParam("archivoNombre") String nombreArchivoOriginal,  // El nombre del archivo, si ya existe
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {  // Recibimos el archivo HTML como MultipartFile

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);

        try {
            Usuario user = servicioUsuario.buscarPorId(usuario.getId());

            MultipartFile archivoAdjunto = archivo;  // El archivo que se sube con la solicitud

            if (archivo == null || archivo.isEmpty()) {
                // Si no se especifica un archivo, generamos el PDF del resumen HTML
                String nombrePdfGenerado = "resumen_" + UUID.randomUUID() + ".pdf";

                // Generamos el PDF a partir del resumen HTML
                File pdfGenerado = servicioSubirResumenAPublicacion.generarPdf(resumen, nombrePdfGenerado);

                // Convertimos el archivo PDF a MultipartFile para que sea subido
                archivoAdjunto = servicioSubirResumenAPublicacion.obtenerArchivoPdf(nombrePdfGenerado);

                model.addAttribute("mensaje", "Resumen generado y compartido como PDF.");
            } else {
                // Si ya existe el archivo HTML, lo buscamos en la carpeta /archivos_html
                // Asegúrate de que el archivo HTML esté guardado en el directorio adecuado
                archivoAdjunto = servicioSubirResumenAPublicacion.obtenerArchivoPdf(nombreArchivoOriginal);  // Método para obtener el archivo ya existente
                model.addAttribute("mensaje", "Resumen original como archivo PDF.");
            }

            // Crear la publicación con el archivo adjunto
            Publicacion publicacion = new Publicacion();
            publicacion.setDescripcion("📄 Resumen adjunto como archivo PDF.");
            servicioPublicacion.realizar(publicacion, user, archivoAdjunto);

        } catch (PublicacionFallida e) {
            model.addAttribute("mensaje", "Error al realizar la publicación: " + e.getMessage());
        } catch (IOException e) {
            model.addAttribute("mensaje", "Error al procesar el archivo: " + e.getMessage());
        }

        // Listar los archivos PDF disponibles
        List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
        model.addAttribute("archivos", archivos);

        return new ModelAndView("herramientas-IA", model);
    }


}
