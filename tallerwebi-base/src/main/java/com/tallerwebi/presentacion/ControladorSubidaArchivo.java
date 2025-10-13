package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.ServicioMostrarArchivosSubidos;
import com.tallerwebi.dominio.ServicioSubirArchivo;
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
import java.util.List;


@Controller
public class ControladorSubidaArchivo {

    private final ServicioSubirArchivo servicioSubirArchivo;
    private final ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;

    public ControladorSubidaArchivo(ServicioSubirArchivo servicioSubirArchivo, ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos) {
        this.servicioSubirArchivo = servicioSubirArchivo;
        this.servicioMostrarArchivosSubidos = servicioMostrarArchivosSubidos;
    }

    //con este mostramos los datos del usuario en la página
    @GetMapping("/subir-archivo")
    public ModelAndView mostrarDatosEnSubirArchivo(HttpServletRequest request) {
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

        return new ModelAndView("subir-archivo", model);
    }

    //y con este manejamos la subida de archivos, responde al post del html
    @PostMapping("/subir-archivo")
    public ModelAndView guardarArchivoDeSubirArchivo(@SessionAttribute("usuarioLogueado") DatosUsuario usuario, @RequestParam("archivo") MultipartFile archivo){
        // este metodo devuelve un modelandview , retorna una vista de datos
        //el session attribute obtiene los datos de usuario de la sesión
        //el request param recibe el archivo que enviamos por post como un "MultipartFile"

        ModelMap model = new ModelMap();//Crea un model map… estructura clave-valor para pasar datos a la vista
        model.addAttribute("usuario", usuario);//agrego el usuario logueado al modelo para mostrar sus datos en la vista

        //PRIMER VALIDACIÓN---> SI NO HAY ARCHIVO
        if (archivo == null || archivo.isEmpty()) {
            model.addAttribute("mensaje", "Archivo no encontrado.");//agrego mensaje de error al modelo
            return new ModelAndView("subir-archivo", model);// retorno la vista con el mensaje
        }

        try{
            //delego la lógica de subida al servicio
            String nombreArchivo =servicioSubirArchivo.guardarArchivoPdf(archivo, usuario);
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
        return new ModelAndView("subir-archivo", model);

    }//fin del metodo


}//fin de la clase
