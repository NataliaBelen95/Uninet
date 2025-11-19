package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.PublicacionMapper;
import com.tallerwebi.presentacion.DTO.UsuarioMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
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
    private final ServicioAmistad servicioAmistad;

    // Inyección del repositorio a través del constructor
    @Autowired
    public ControladorMiPerfil(ServicioUsuario servicioUsuario, ServicioGenero servicioGenero, ServicioLogin servicioLogin,
                               ServicioNotificacion servicioNotificacion, UsuarioMapper usuarioMapper,
                               ServicioPublicacion servicioPublicacion,  PublicacionMapper publicacionMapper, ServicioAmistad servicioAmistad) {
        this.servicioUsuario = servicioUsuario;
        this.servicioGenero = servicioGenero;
        this.servicioLogin = servicioLogin;
        this.servicioNotificacion = servicioNotificacion;
        this.usuarioMapper = usuarioMapper;
        this.servicioPublicacion = servicioPublicacion;
        this.publicacionMapper = publicacionMapper;
        this.servicioAmistad = servicioAmistad;
    }

    /** --- Método privado para validar sesión --- */
    private DatosUsuario validarSesion(HttpServletRequest request) {
        return (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
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

    /** --- Método privado para mostrar perfil --- */
    @Transactional(readOnly = true)
    private ModelAndView mostrarPerfil(String slug, HttpServletRequest request, boolean esPropio) {
        Usuario usuarioPerfil = (slug != null)
                ? servicioUsuario.buscarPorSlugConPublis(slug)
                : servicioUsuario.buscarUsuarioPorIdConPublicaciones(validarSesion(request).getId());

        DatosUsuario dto = esPropio
                ? usuarioMapper.toDtoPropio(usuarioPerfil)
                : usuarioMapper.toDtoPublico(usuarioPerfil);

        if (esPropio) {
            dto.setCantidadNotificaciones(servicioNotificacion.contarNoLeidas(usuarioPerfil.getId()));
            request.getSession().setAttribute("usuarioLogueado", dto);
        }

        // Mapeo de publicaciones
        Long idLogueado = validarSesion(request) != null ? validarSesion(request).getId() : null;
        List<DatosPublicacion> dtos = servicioPublicacion.obtenerPublicacionesDeUsuario(usuarioPerfil.getId())
                .stream()
                .map(p -> esPropio
                        ? publicacionMapper.toDtoPublica(p, idLogueado)
                        : publicacionMapper.toDtoPropia(p, idLogueado))
                .collect(Collectors.toList());

        ModelMap model = new ModelMap();
        model.addAttribute("usuario", dto);
        model.addAttribute("generos", servicioGenero.listarGeneros());
        model.addAttribute("publicaciones", dtos);
        model.addAttribute("esPropio", esPropio);

        return new ModelAndView("miPerfil", model);
    }

    @GetMapping("/perfil/id/{id}")
    public ModelAndView perfilPorId(@PathVariable Long id, HttpServletRequest request) {

        Usuario usuarioPerfil = servicioUsuario.buscarPorId(id);

        // 1. Validación de nulidad (Si no existe)
        if (usuarioPerfil == null) {
            return new ModelAndView("redirect:/usuarios?error=Usuario no encontrado");
        }

        // 2. Obtener el SLUG de la entidad (asegurando que el mapper lo genere)
        DatosUsuario dtoPerfil = usuarioMapper.toDtoPublico(usuarioPerfil);
        String slug = dtoPerfil.getSlug();


        return new ModelAndView("redirect:/perfil/" + slug);
    }
    @PostMapping("/enviar/{idReceptor}")
    public String enviarSolicitud(@PathVariable Long idReceptor, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }

        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(idReceptor);
        if (solicitante == null || receptor == null) {
            return "redirect:/usuarios";
        }
        SolicitudAmistad solicitudCreada = servicioAmistad.enviarSolicitud(solicitante, receptor);
        Long solicitudId = solicitudCreada.getId();

        try {
            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);
        } catch (Exception e) {
            System.err.println("Error al notificar solicitud de amistad: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }


    @PostMapping("/enviar")
    public String enviarSolicitudForm(@RequestParam("receptorId") Long receptorId, HttpServletRequest request) {
        DatosUsuario datos = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
        if (datos == null) {
            return "redirect:/login";
        }
        Usuario solicitante = servicioUsuario.buscarPorId(datos.getId());
        Usuario receptor = servicioUsuario.buscarPorId(receptorId);

        // Asumiendo que el resto de las verificaciones de nullidad ya pasaron

        try {

            SolicitudAmistad solicitudCreada = servicioAmistad.enviarSolicitud(solicitante, receptor);
            Long solicitudId = solicitudCreada.getId();

            servicioNotificacion.crearAmistad(receptor, solicitante, TipoNotificacion.SOLICITUD_AMISTAD, solicitudId);

        } catch (IllegalStateException e) {
            //  CAPTURAR LA VALIDACIÓN DE NEGOCIO Y REDIRIGIR AMIGABLEMENTE
            String mensajeCodificado = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            System.err.println("Error de validación de amistad: " + e.getMessage());
            return "redirect:/home?error=" + mensajeCodificado;

        } catch (Exception e) {
            // Capturar otros errores como fallo de notificación o DB.
            System.err.println("Error general al enviar solicitud: " + e.getMessage());
            return "redirect:/usuarios?error=Error desconocido al enviar solicitud.";
        }

        // Redirección de éxito
        return "redirect:/home";
    }
}
