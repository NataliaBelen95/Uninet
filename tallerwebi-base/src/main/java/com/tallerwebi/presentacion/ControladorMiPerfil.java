package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorMiPerfil {

    private final ServicioUsuario servicioUsuario;
    private final ServicioGenero servicioGenero;
    private final ServicioLogin servicioLogin;
    private final ServicioNotificacion servicioNotificacion;
    private final UsuarioMapper usuarioMapper;
    private final ServicioPublicacion servicioPublicacion;
    private final PublicacionMapper publicacionMapper;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioUsuario servicioUsuario, ServicioGenero servicioGenero, ServicioLogin servicioLogin,
                               ServicioNotificacion servicioNotificacion, UsuarioMapper usuarioMapper,
                               ServicioPublicacion servicioPublicacion,  PublicacionMapper publicacionMapper) {
        this.servicioUsuario = servicioUsuario;
        this.servicioGenero = servicioGenero;
        this.servicioLogin = servicioLogin;
        this.servicioNotificacion = servicioNotificacion;
        this.usuarioMapper = usuarioMapper;
        this.servicioPublicacion = servicioPublicacion;
        this.publicacionMapper = publicacionMapper;
    }


    @GetMapping("/editar-informacion")
    @Transactional(readOnly = true)
    public ModelAndView editarInformacion(HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");

        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());
        DatosUsuario dto = usuarioMapper.toDtoPropio(usuario);
        dto.setCantidadNotificaciones(servicioNotificacion.contarNoLeidas(usuario.getId()));
        ModelMap model = new ModelMap();
        model.addAttribute("usuario", dto);
        model.addAttribute("generos", servicioGenero.listarGeneros());

        return new ModelAndView("editar-informacion", model);
    }

    @PostMapping("/miPerfil")
    @Transactional
    public ModelAndView actualizarPerfil(@ModelAttribute("usuario") DatosUsuario dto, HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Usuario usuarioEnBD = servicioUsuario.buscarPorId(datosUsuario.getId());

        // Actualizamos usuarioEnBD con datos del DTO
        usuarioEnBD.setEmail(dto.getEmail());
        usuarioEnBD.setEmailPersonal(dto.getEmailPersonal());
        usuarioEnBD.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioEnBD.setTelefono(dto.getTelefono());
        usuarioEnBD.setDireccion(dto.getDireccion());
        usuarioEnBD.setLocalidad(dto.getLocalidad());
        usuarioEnBD.setCodigoPostal(dto.getCodigoPostal());
        usuarioEnBD.setProvincia(dto.getProvincia());
        usuarioEnBD.setPassword(dto.getPassword());

        if(dto.getGenero() != null && dto.getGenero().getId() != null) {
            Genero generoEnBD = servicioGenero.buscarPorId(dto.getGenero().getId());
            usuarioEnBD.setGenero(generoEnBD);
        } else {
            usuarioEnBD.setGenero(null);
        }

        // Guardar entidad actualizada
        servicioUsuario.actualizar(usuarioEnBD);

        // Actualizar DTO para la sesión y vista
        DatosUsuario dtoActualizado = usuarioMapper.toDtoPropio(usuarioEnBD);
        dtoActualizado.setCantidadNotificaciones(servicioNotificacion.contarNoLeidas(usuarioEnBD.getId()));
        request.getSession().setAttribute("usuarioLogueado", dtoActualizado);

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", dtoActualizado);
        model.addAttribute("generos", servicioGenero.listarGeneros());
        model.addAttribute("mensaje", "Cambios guardados con éxito");

        return new ModelAndView("miPerfil", model);
    }
    @PostMapping("/miPerfil/foto")
    @Transactional
    public ModelAndView subirFoto(@RequestParam("fotoPerfil") MultipartFile foto, HttpServletRequest request) {
        DatosUsuario datosUsuario = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        ModelMap model = new ModelMap();

        if (datosUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        Usuario usuario = servicioUsuario.buscarPorId(datosUsuario.getId());

        if (foto.isEmpty()) {
            model.addAttribute("mensaje", "No seleccionaste ninguna imagen");
            model.addAttribute("usuario", datosUsuario);
            return new ModelAndView("miPerfil", model);
        }

        String nombreOriginal = foto.getOriginalFilename(); // ej: pepito.jpg
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.')).toLowerCase();
        long pesoPermitido = 2 * 1024 * 1024; // 2MB
        List<String> extensionesPermitidas = Arrays.asList(".png", ".jpg", ".jpeg", ".webp");

        if (!extensionesPermitidas.contains(extension)) {
            model.addAttribute("mensaje", "Formato no válido. Solo se aceptan: JPG, PNG, WEBP");
            model.addAttribute("usuario", datosUsuario);
            return new ModelAndView("miPerfil", model);
        }

        if (foto.getSize() > pesoPermitido) {
            model.addAttribute("mensaje", "El archivo es demasiado grande (máx. 2MB)");
            model.addAttribute("usuario", datosUsuario);
            return new ModelAndView("miPerfil", model);
        }

        try {
            String nombreFinal = java.util.UUID.randomUUID().toString() + extension;
            String rutaBase = System.getProperty("user.dir") + java.io.File.separator + "perfiles";
            java.io.File directorio = new java.io.File(rutaBase);

            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            java.io.File destino = new java.io.File(directorio, nombreFinal);

            foto.transferTo(destino);

            // Actualizamos entidad y guardamos
            usuario.setFotoPerfil("perfiles/" + nombreFinal);
            servicioUsuario.actualizar(usuario);

            // Actualizamos DTO
            DatosUsuario dtoActualizado = usuarioMapper.toDtoPropio(usuario);
            dtoActualizado.setCantidadNotificaciones(servicioNotificacion.contarNoLeidas(usuario.getId()));

            // Guardamos DTO actualizado en sesión y pasamos a la vista
            request.getSession().setAttribute("usuarioLogueado", dtoActualizado);
            model.addAttribute("usuario", dtoActualizado);

            model.addAttribute("mensaje", "Foto subida correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "Error al cargar imagen: " + e.getMessage());
            model.addAttribute("usuario", datosUsuario);
        }

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
                String rutaBase = System.getProperty("user.dir") + java.io.File.separator + "perfiles";
                String rutaCompleta = rutaBase + java.io.File.separator + usuario.getFotoPerfil().substring("perfiles/".length());
                java.io.File archivo = new java.io.File(rutaCompleta);

                if (archivo.exists()) {
                    archivo.delete();
                }

                // Limpiamos foto en entidad y guardamos
                usuario.setFotoPerfil(null);
                servicioUsuario.actualizar(usuario);

                // Actualizamos DTO y sesión
                DatosUsuario dtoActualizado = usuarioMapper.toDtoPropio(usuario);
                dtoActualizado.setCantidadNotificaciones(servicioNotificacion.contarNoLeidas(usuario.getId()));
                request.getSession().setAttribute("usuarioLogueado", dtoActualizado);

                model.addAttribute("usuario", dtoActualizado);
                model.addAttribute("mensaje", "Foto de perfil eliminada correctamente");
            } else {
                model.addAttribute("usuario", datosUsuario);
                model.addAttribute("mensaje", "No tenés foto para eliminar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("usuario", datosUsuario);
            model.addAttribute("error", "Error al eliminar la foto");
        }

        return new ModelAndView("miPerfil", model);
    }



    @GetMapping("/miPerfil")
    @Transactional(readOnly = true)
    public ModelAndView miPerfil(HttpServletRequest request) {
        DatosUsuario datosSesion = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datosSesion == null) {
            return new ModelAndView("redirect:/login");
        }
        return mostrarPerfil(datosSesion.getSlug(), request, true);
    }

    @GetMapping("/perfil/{slug}")
    public ModelAndView perfil(@PathVariable String slug, HttpServletRequest request) {
        DatosUsuario datosSesion = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        boolean esPropio = datosSesion != null && slug.equals(datosSesion.getSlug());
        return mostrarPerfil(slug, request, esPropio);
    }

    @Transactional
    private ModelAndView mostrarPerfil(String slug, HttpServletRequest request, boolean esPropio) {
        Usuario usuarioPerfil; // usuario cuyo perfil estamos viendo
        DatosUsuario usuarioLogueado = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        Long idLogueado = usuarioLogueado != null ? usuarioLogueado.getId() : null;

        // Obtener el usuario del perfil
        if (slug != null) {
            usuarioPerfil = servicioUsuario.buscarPorSlugConPublis(slug);
        } else {
            usuarioPerfil = servicioUsuario.buscarUsuarioPorIdConPublicaciones(usuarioLogueado.getId());
        }
        int cantidadNoLeidas = 0;
        if (esPropio) {
            cantidadNoLeidas = servicioNotificacion.contarNoLeidas(usuarioPerfil.getId());
        }

        DatosUsuario dto = esPropio
                ? usuarioMapper.toDtoPropio(usuarioPerfil)
                : usuarioMapper.toDtoPublico(usuarioPerfil);
        dto.setCantidadNotificaciones(cantidadNoLeidas);

        // Traemos y mapeamos publicaciones
        List<Publicacion> publicaciones = servicioPublicacion.obtenerPublicacionesDeUsuario(usuarioPerfil.getId());
        List<DatosPublicacion> dtosPublicaciones = publicaciones.stream()
                .map(p -> esPropio
                        ? publicacionMapper.toDtoPublica(p, idLogueado)  // perfil propio → trae comentarios
                        : publicacionMapper.toDtoPropia(p, idLogueado)   // perfil ajeno → NO trae comentarios
                )
                .collect(Collectors.toList());

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", dto);
        model.addAttribute("generos", servicioGenero.listarGeneros());
        model.addAttribute("publicaciones", dtosPublicaciones);
        model.addAttribute("esPropio", esPropio);
        // Actualizamos sesión solo si es propio
        if (esPropio) {
            request.getSession().setAttribute("usuarioLogueado", dto);
        }

        return new ModelAndView("miPerfil", model);
    }
}


