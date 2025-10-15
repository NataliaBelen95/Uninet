package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.ServicioHacerResumen;
import com.tallerwebi.dominio.ServicioMostrarArchivosSubidos;
import com.tallerwebi.dominio.ServicioSubirArchivoALaIA;
import com.tallerwebi.dominio.excepcion.NoSePudoExtraerElTextoDelPDFException;
import com.tallerwebi.dominio.excepcion.NoSePudoGenerarResumenDelPDFException;
import com.tallerwebi.dominio.excepcion.NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;
import com.tallerwebi.dominio.excepcion.NoSePuedeSubirArchivoPorFallaException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;


@Controller
public class ControladorHerramientasIA {

    private final ServicioSubirArchivoALaIA servicioSubirArchivoALaIA;
    private final ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;
    private final ServicioHacerResumen servicioHacerResumen;

    public ControladorHerramientasIA(ServicioSubirArchivoALaIA servicioSubirArchivoALaIA, ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos,ServicioHacerResumen servicioHacerResumen) {
        this.servicioSubirArchivoALaIA = servicioSubirArchivoALaIA;
        this.servicioMostrarArchivosSubidos = servicioMostrarArchivosSubidos;
        this.servicioHacerResumen = servicioHacerResumen;
    }

    //con este mostramos los datos del usuario en la página
    @GetMapping("/herramientas-IA")
    public ModelAndView mostrarDatosEnHerramientasIA(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        model.addAttribute("usuario", usuario);

        //agrego lista de archivos al model
        List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
        model.addAttribute("archivos", archivos);

        return new ModelAndView("herramientas-IA", model);
    }

    //y con este manejamos la subida de archivos, responde al post del html
    @PostMapping("/herramientas-IA")
    public ModelAndView guardarArchivoSubidoEnHerramientasIA(@SessionAttribute("usuarioLogueado") DatosUsuario usuario, @RequestParam("archivo") MultipartFile archivo){
        // este metodo devuelve un modelandview , retorna una vista de datos
        //el session attribute obtiene los datos de usuario de la sesión
        //el request param recibe el archivo que enviamos por post como un "MultipartFile"

        ModelMap model = new ModelMap();//Crea un model map… estructura clave-valor para pasar datos a la vista
        model.addAttribute("usuario", usuario);//agrego el usuario logueado al modelo para mostrar sus datos en la vista

        //PRIMER VALIDACIÓN---> SI NO HAY ARCHIVO
        if (archivo == null || archivo.isEmpty()) {
            model.addAttribute("mensaje", "Archivo no encontrado.");//agrego mensaje de error al modelo
            return new ModelAndView("herramientas-IA", model);// retorno la vista con el mensaje
        }
        try{
            //delego la lógica de subida al servicio
            String nombreArchivo = servicioSubirArchivoALaIA.guardarArchivoPdf(archivo, usuario);
            //si se pudo subir, envía mensaje de éxito
            model.addAttribute("mensaje", "Archivo guardado exitosamente: " + nombreArchivo);
        //si no se pudo subir envía la excepción
        }catch (NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException | NoSePuedeSubirArchivoPorFallaException e) {
            model.addAttribute("mensaje", e.getMessage());
        }
        //recargo la lista de archivos
        List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
        model.addAttribute("archivos", archivos);
        //retorno la vista con los datos del model map
        return new ModelAndView("herramientas-IA", model);
    }//fin del metodo

    @PostMapping("/herramientas-IA/resumen")
    public ModelAndView generarResumenPdf(@SessionAttribute("usuarioLogueado") DatosUsuario usuario, @RequestParam("archivoSeleccionado") String nombreArchivo) {

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);

        // Construimos la ruta absoluta del archivo
        String rutaArchivo = "C:/Users/rocam/OneDrive/Escritorio/TALLER WEB 1/Uninet/tallerwebi-base/archivos_pdf/" + nombreArchivo;

        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                model.addAttribute("mensaje", "El archivo no existe: " + rutaArchivo);
                return new ModelAndView("herramientas-IA", model);
            }
            // Extraemos el texto usando tu servicio
            String texto = servicioHacerResumen.extraerTexto(rutaArchivo);

            // Generamos el resumen con la IA
            String resumen = servicioHacerResumen.generarResumen(texto);
            // 🔍 IMPRIMÍS EL RESUMEN EN CONSOLA
            System.out.println("Resumen generado:");
            System.out.println(resumen); // Acá ves si está vacío, cortado, etc.
//agrego el resumen al model
            model.addAttribute("resumen", resumen);
            model.addAttribute("mensaje", "Resumen generado exitosamente.");

        } catch (NoSePudoExtraerElTextoDelPDFException | NoSePudoGenerarResumenDelPDFException e) {
            model.addAttribute("mensaje", e.getMessage());
        }

        // Recargamos la lista de archivos para mostrarla en la vista
        List<String> archivos = servicioMostrarArchivosSubidos.listarArchivosPdf();
        model.addAttribute("archivos", archivos);

        return new ModelAndView("herramientas-IA", model);
    }
}//fin de la clase
