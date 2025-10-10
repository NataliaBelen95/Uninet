package com.tallerwebi.presentacion;


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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Controller
public class ControladorSubidaArchivo {

    public ControladorSubidaArchivo() {
        //constructor vacio
    }
//con este mostramos los datos del usuario en la pagina
    @GetMapping("/subir-archivo")
    public ModelAndView mostrarSubirArchivo(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session=request.getSession();
        DatosUsuario usuario=(DatosUsuario) session.getAttribute("usuarioLogueado");

        if(usuario==null){
            return new ModelAndView("redirect:/login");
        }
        model.addAttribute("usuario", usuario);

        return new ModelAndView("subir-archivo", model);
    }
//y con este manejamos la subida de archivos
@PostMapping("/subir-archivo")
public ModelAndView guardarSubirArchivo(
        @SessionAttribute("usuarioLogueado") DatosUsuario usuario,
        @RequestParam("archivo") MultipartFile archivo) {

    ModelMap model = new ModelMap();
    model.addAttribute("usuario", usuario);

    if (archivo == null || archivo.isEmpty()) {
        model.addAttribute("mensaje", "Archivo no encontrado.");
        return new ModelAndView("subir-archivo", model);
    }

    try {
        // Ruta absoluta del proyecto
        String basePath = System.getProperty("user.dir");
        Path rutaDestino = Paths.get(basePath, "archivos_pdf");
        Files.createDirectories(rutaDestino);

        // Nombre limpio del archivo
        String nombreArchivo = Paths.get(Objects.requireNonNull(archivo.getOriginalFilename())).getFileName().toString();
        Path destinoFinal = rutaDestino.resolve(nombreArchivo);

        // Solución: copiar manualmente el stream del archivo
        try (InputStream inputStream = archivo.getInputStream()) {
            Files.copy(inputStream, destinoFinal, StandardCopyOption.REPLACE_EXISTING);
        }

        model.addAttribute("mensaje", "Archivo guardado exitosamente: " + nombreArchivo);

    } catch (IOException e) {
        e.printStackTrace();
        model.addAttribute("mensaje", "Error al subir el archivo: " + e.getMessage());
    }

    return new ModelAndView("subir-archivo", model);
}


}
