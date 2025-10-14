package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Controller
public class ControladorMiPerfil {

    private final ServicioUsuario servicioUsuario;
    private final ServicioGenero servicioGenero;
    private final ServicioLogin servicioLogin;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioUsuario servicioUsuario, ServicioGenero servicioGenero, ServicioLogin servicioLogin) {
        this.servicioUsuario = servicioUsuario;
        this.servicioGenero = servicioGenero;
        this.servicioLogin = servicioLogin;
    }

    @GetMapping("/miPerfil")
    @Transactional(readOnly = true)
    public ModelAndView miPerfil(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        Usuario usuario = servicioUsuario.buscarPorEmail(datosUsuario.getEmail());
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);
        model.addAttribute("generos", servicioGenero.listarGeneros());

        return new ModelAndView("miPerfil", model);
    }

    @GetMapping("/editar-informacion")
    @Transactional(readOnly = true)
    public ModelAndView editarInformacion(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuario);
        model.addAttribute("generos", servicioGenero.listarGeneros());

        return new ModelAndView("editar-informacion", model);
    }


    @PostMapping("/miPerfil")
    @Transactional
    public ModelAndView actualizarPerfil(@ModelAttribute("usuario")  Usuario usuario, HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if(datosUsuario == null){
            return new ModelAndView("miPerfil");
        }
        //Usuario usuarioEnBD = servicioUsuario.buscarPorEmail(datosUsuario.getEmail()); forma vieja
        Usuario usuarioEnBD = servicioUsuario.buscarPorId(datosUsuario.getId());

        //Acá actualizamos los campos editables
        usuarioEnBD.setEmail(usuario.getEmail());
        usuarioEnBD.setEmailPersonal(usuario.getEmailPersonal());
        usuarioEnBD.setFechaNacimiento(usuario.getFechaNacimiento());
        usuarioEnBD.setTelefono(usuario.getTelefono());
        usuarioEnBD.setDireccion(usuario.getDireccion());
        usuarioEnBD.setLocalidad(usuario.getLocalidad());
        usuarioEnBD.setCodigoPostal(usuario.getCodigoPostal());
        usuarioEnBD.setProvincia(usuario.getProvincia());
        usuarioEnBD.setPassword(usuario.getPassword());
        datosUsuario.setFotoPerfil(usuario.getFotoPerfil());

        if(usuario.getGenero() != null && usuario.getGenero().getId() != null) {
            Genero generoEnBD = servicioGenero.buscarPorId(usuario.getGenero().getId());
            usuarioEnBD.setGenero(generoEnBD);
        }else{
            usuarioEnBD.setGenero(null);
        }

        //Guardamos en la base de datos
        servicioUsuario.actualizar(usuarioEnBD);

        //Actualizamos el modelo y mostramos mensaje
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", usuarioEnBD);
        model.addAttribute("generos", servicioGenero.listarGeneros());
        model.addAttribute("mensaje", "Cambios guardados con éxito");

        return new ModelAndView("miPerfil", model);
    }

    @PostMapping("/miPerfil/foto")
    @Transactional
    public ModelAndView subirFoto(@RequestParam("fotoPerfil") MultipartFile foto, HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
        ModelMap model = new ModelMap();

        if (foto.isEmpty()) {
            model.addAttribute("mensaje", "No seleccionaste ninguna imagen");
            return new ModelAndView("miPerfil", model);
        }

        String nombreOriginal = foto.getOriginalFilename(); // ej: pepito.jpg
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.')).toLowerCase();
        long pesoPermitido = 2 * 1024 * 1024; // 2MB
        List<String> extensionesPermitidas = Arrays.asList(".png", ".jpg", ".jpeg", ".webp");

        if (!extensionesPermitidas.contains(extension)) {
            model.addAttribute("mensaje", "Formato no válido. Solo se aceptan: JPG, PNG, WEBP");
            return new ModelAndView("miPerfil", model);
        }

        if (foto.getSize() > pesoPermitido) {
            model.addAttribute("mensaje", "El archivo es demasiado grande (máx. 2MB)");
            return new ModelAndView("miPerfil", model);
        }

        try {
            // Nombre único para evitar conflictos
            String nombreFinal = java.util.UUID.randomUUID().toString() + extension;

            // Usamos una única fuente: la carpeta "perfiles" en la raíz del proyecto
            String rutaBase = System.getProperty("user.dir") + java.io.File.separator + "perfiles";
            java.io.File directorio = new java.io.File(rutaBase);

            // Crea todos los directorios necesarios (mkdirs en vez de mkdir)
            if (!directorio.exists()) {
                boolean ok = directorio.mkdirs();
                System.out.println("Directorio creado: " + directorio.getAbsolutePath() + " -> " + ok);
            }

            java.io.File destino = new java.io.File(directorio, nombreFinal);

            // DEBUG: imprimir rutas para chequear
            System.out.println("rutaBase = " + rutaBase);
            System.out.println("destino absoluto = " + destino.getAbsolutePath());

            // Guardar el archivo (usa la ruta absoluta)
            foto.transferTo(destino);

            usuario.setFotoPerfil("perfiles/" + nombreFinal);
            datosUsuario.setFotoPerfil(usuario.getFotoPerfil());
            servicioUsuario.actualizar(usuario);

            System.out.println("Archivo subido: " + nombreOriginal);
            System.out.println("Ruta guardada en BD: " + usuario.getFotoPerfil());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "Error al cargar imagen: " + e.getMessage());
        }
        model.addAttribute("usuario", usuario);
        return new ModelAndView("miPerfil", model);
    }

    @PostMapping("/miPerfil/eliminar-foto")
    @Transactional
    public ModelAndView eliminarFoto(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
        ModelMap model = new ModelMap();

        try {
            if (usuario.getFotoPerfil() != null) {
                // Elimina el archivo físico
                String rutaBase = System.getProperty("user.dir") + java.io.File.separator + "perfiles";
                String rutaCompleta = rutaBase + java.io.File.separator + usuario.getFotoPerfil().substring("perfiles/".length());

                // Crear el archivo
                java.io.File archivo = new java.io.File(rutaCompleta);

                if (archivo.exists()) {
                    archivo.delete();
                }

                // Elimina la ruta en la base
                usuario.setFotoPerfil(null);
                servicioUsuario.actualizar(usuario);
            }

            model.addAttribute("mensaje", "Foto de perfil eliminada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al eliminar la foto");
        }

        model.addAttribute("usuario", usuario);
        return new ModelAndView("miPerfil", model);
    }


}