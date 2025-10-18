package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.ServicioConvertirTextoApdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Controller
public class ControladorSubirResumenAPublicacion {
    private ServicioUsuario servicioUsuario;
    private ServicioConvertirTextoApdf servicioConvertirTextoApdf  ;
    private ServicioPublicacion servicioPublicacion;
    private ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;

    @Autowired
    public ControladorSubirResumenAPublicacion(ServicioUsuario servicioUsuario, ServicioConvertirTextoApdf ServicioConvertirTextoApdf,
                                               ServicioPublicacion ServicioPublicacion, ServicioMostrarArchivosSubidos ServicioMostrarArchivosSubidos) {
        this.servicioUsuario = servicioUsuario;
        this.servicioConvertirTextoApdf= ServicioConvertirTextoApdf;
        this.servicioPublicacion = ServicioPublicacion;
        this.servicioMostrarArchivosSubidos = ServicioMostrarArchivosSubidos;
    }


    @PostMapping("/herramientas-IA/compartir-resumen")
    public ModelAndView compartirResumenComoPublicacion(
            @SessionAttribute("usuarioLogueado") DatosUsuario usuario,
            @RequestParam("resumen") String resumen,
            @RequestParam("archivoNombre") String nombreArchivoOriginal) {

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);

        try {
            Usuario user = servicioUsuario.buscarPorId(usuario.getId());

            Publicacion publicacion = new Publicacion();

            MultipartFile archivoAdjunto;

            // Si el resumen es mayor a 200 caracteres, lo convertimos en PDF
            if (resumen.length() > 200) {
                // Generamos PDF con el resumen
                String nombrePdfGenerado = "resumen_" + UUID.randomUUID() + ".pdf";
                File pdfGenerado = servicioConvertirTextoApdf.generarPdf(resumen, nombrePdfGenerado);


                // Convertimos el archivo PDF en MultipartFile
                Path path = pdfGenerado.toPath();
                String tipoContenido = Files.probeContentType(path);
                byte[] contenido = Files.readAllBytes(path);
                archivoAdjunto = new MockMultipartFile(nombrePdfGenerado, nombrePdfGenerado, tipoContenido, contenido);

                // Descripción corta indicando que hay un resumen en el archivo
                publicacion.setDescripcion("📄 Resumen adjunto en PDF.");
            } else {
                // Si es corto, se puede poner como texto en la publicación
                publicacion.setDescripcion("📄 Resumen generado por IA:\n\n" + resumen);

                // También podés adjuntar el archivo original si querés (opcional)
                // Acá lo leemos si `nombreArchivoOriginal` tiene valor
                archivoAdjunto = null;
                if (nombreArchivoOriginal != null && !nombreArchivoOriginal.isEmpty()) {
                    String rutaArchivo = System.getProperty("user.dir") + "/archivos_pdf/" + nombreArchivoOriginal;
                    File file = new File(rutaArchivo);

                    if (!file.exists()) {
                        model.addAttribute("mensaje", "El archivo original no existe para compartir.");
                        return new ModelAndView("herramientas-IA", model);
                    }

                    Path path = file.toPath();
                    String tipoContenido = Files.probeContentType(path);
                    byte[] contenido = Files.readAllBytes(path);
                    archivoAdjunto = new MockMultipartFile(
                            nombreArchivoOriginal,
                            nombreArchivoOriginal,
                            tipoContenido,
                            contenido
                    );
                }
            }

            // Publicamos
            servicioPublicacion.realizar(publicacion, user, archivoAdjunto);

            model.addAttribute("mensaje", "Resumen compartido como publicación.");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "Error al compartir el resumen: " + e.getMessage());
        }

        List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
        model.addAttribute("archivos", archivos);

        return new ModelAndView("herramientas-IA", model);
    }
}
