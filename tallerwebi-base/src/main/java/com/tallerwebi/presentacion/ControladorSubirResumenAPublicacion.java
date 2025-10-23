package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;


import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.UUID;

@Controller
public class ControladorSubirResumenAPublicacion {
    private ServicioUsuario servicioUsuario;
    private ServicioPdfGenerator servicioPdfGenerator;

    @Autowired
    public ControladorSubirResumenAPublicacion(ServicioUsuario servicioUsuario,
                                               ServicioPdfGenerator servicioPdfGenerator) {
        this.servicioUsuario = servicioUsuario;
        this.servicioPdfGenerator = servicioPdfGenerator;
    }


    //para que suba el archivo cargado a subir-resumen y despues le pueda agregar descripcion
    @PostMapping("/subir-resumen")
    public String mostrarFormularioSubirResumen(@RequestParam String resumen,

                                                HttpServletRequest request,
                                                Model model) throws IOException {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datosUsuario == null) {
            return "redirect:/login"; // o lo que tenga sentido
        }
        try {
            // Generar nombre único para el PDF
            String nombrePdfGenerado = "resumen_" + UUID.randomUUID() + ".pdf";

            // Generar PDF a partir del resumen HTML
            File pdfGenerado = servicioPdfGenerator.generarPdfYGuardar(resumen, nombrePdfGenerado);

            // Guardar el nombre del PDF en el modelo para usarlo en la vista
            model.addAttribute("archivoPdfPath", nombrePdfGenerado);

            model.addAttribute("usuario", datosUsuario);

        } catch (IOException e) {
            // Si falla la generación del PDF, mostrar mensaje de error y devolver la vista con mensaje
            model.addAttribute("error", "Error al generar el PDF: " + e.getMessage());
            // Podés decidir si devolver una vista de error o la misma para reintentar
        }

        return "subir-resumen"; // Vista con formulario para agregar descripción y mostrar PDF generado
    }
//
//    @PostMapping("/herramientas-IA/compartir-resumen")
//    public ModelAndView compartirResumenComoPublicacion(
//            @SessionAttribute("usuarioLogueado") DatosUsuario usuario,
//            @RequestParam("resumen") String resumen,  // Recibimos el resumen en formato texto (HTML)
//            @RequestParam("archivoNombre") String nombreArchivoOriginal,  // El nombre del archivo, si ya existe
//            @RequestParam(value = "archivo", required = false) MultipartFile archivo, // archivo HTML como MultipartFile
//            @RequestParam(value = "descripcion", required = false) String descripcion) {
//        {
//
//            ModelMap model = new ModelMap();
//            model.addAttribute("usuario", usuario);
//
//            try {
//                Usuario user = servicioUsuario.buscarPorId(usuario.getId());
//
//                MultipartFile archivoAdjunto = archivo;  // El archivo que se sube con la solicitud
//
//                if (archivo == null || archivo.isEmpty()) {
//                    // Si no se especifica un archivo, generamos el PDF del resumen HTML
//                    String nombrePdfGenerado = "resumen_" + UUID.randomUUID() + ".pdf";
//
//                    // Generamos el PDF a partir del resumen HTML
//                    File pdfGenerado = servicioSubirResumenAPublicacion.generarPdf(resumen, nombrePdfGenerado);
//
//                    // Convertimos el archivo PDF a MultipartFile para que sea subido
//                    archivoAdjunto = servicioSubirResumenAPublicacion.obtenerArchivoPdf(nombrePdfGenerado);
//
//                    model.addAttribute("mensaje", "Resumen generado y compartido como PDF.");
//                } else {
//                    // Si ya existe el archivo HTML, lo buscamos en la carpeta /archivos_html
//                    // Asegúrate de que el archivo HTML esté guardado en el directorio adecuado
//                    archivoAdjunto = servicioSubirResumenAPublicacion.obtenerArchivoPdf(nombreArchivoOriginal);  // Método para obtener el archivo ya existente
//                    model.addAttribute("mensaje", "Resumen original como archivo PDF.");
//                }
//
//                // Crear la publicación con el archivo adjunto
//                Publicacion publicacion = new Publicacion();
//                publicacion.setDescripcion(descripcion != null ? descripcion : " Resumen adjunto como archivo PDF.");
//                servicioPublicacion.realizar(publicacion, user, archivoAdjunto);
//
//            } catch (PublicacionFallida e) {
//                model.addAttribute("mensaje", "Error al realizar la publicación: " + e.getMessage());
//            } catch (IOException e) {
//                model.addAttribute("mensaje", "Error al procesar el archivo: " + e.getMessage());
//            }
//
//            // Listar los archivos PDF disponibles
//            List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
//            model.addAttribute("archivos", archivos);
//
//            return new ModelAndView("herramientas-IA", model);
//        }
//    }



    //PARA DESCARGAR

    @PostMapping("/descargar-resumen-pdf")
    public ResponseEntity<byte[]> descargarResumenPdf(@RequestParam("resumenHtml") String resumenHtml) throws IOException {
        byte[] pdfBytes = servicioPdfGenerator.generarPdfDesdeHtml(resumenHtml);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("resumen.pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}