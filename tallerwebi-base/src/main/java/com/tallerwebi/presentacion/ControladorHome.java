package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ControladorHome {

    private final PublicacionMapper publicacionMapper;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;
    private final ServicioRecomendaciones servicioRecomendaciones;
    private final GeminiAnalysisService geminiAnalysisService;
    private final BotPublisherService botPublisherService;





    public ControladorHome(ServicioUsuario servicioUsuario, ServicioPublicacion servicioPublicacion,
                           ServicioLike servicioLike, PublicacionMapper publicacionMapper,
                           ServicioRecomendaciones servicioRecomendaciones, GeminiAnalysisService geminiAnalysisService,
                           BotPublisherService botPublisherService) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.publicacionMapper = publicacionMapper;
        this.servicioUsuario = servicioUsuario;
        this.servicioRecomendaciones = servicioRecomendaciones;
        this.geminiAnalysisService = geminiAnalysisService;
        this.botPublisherService = botPublisherService;



        //this.servicioUsuario = servicioUsuario;

    }
/*
    @GetMapping("/usuarios")
    public ModelAndView mostrarUsuarios(HttpServletRequest request, ModelMap model) {
        // 1. Verificar que haya un usuario logueado
        DatosUsuario usuarioLogueado = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");


        // 2. Es su propio perfil (ya que ve todos los usuarios desde su sesión)
        boolean esPropio = true;

        // 3. Obtener todos los usuarios como DTOs
        List<DatosUsuariosNuevos> usuariosDTO = servicioUsuario.mostrarTodos()
                .stream()
                .map(u -> new DatosUsuariosNuevos(
                        u.getNombre(),
                        u.getApellido(),
                        u.getId()
                ))
                .collect(Collectors.toList());

        // 4. Pasar datos al modelo
        model.addAttribute("usuario", usuarioLogueado);
        model.addAttribute("usuariosNuevos", usuariosDTO);
        model.addAttribute("esPropio", esPropio);

        // 5. Retornar la vista
        return new ModelAndView("usuarios", model);
    }
*/
@GetMapping("/home")
public ModelAndView home(HttpServletRequest request,
                         @RequestParam(value = "filtro", defaultValue = "p") String filtro) {

    ModelMap model = new ModelMap();
    HttpSession session = request.getSession();

    DatosUsuario usuarioLogueado = (DatosUsuario) session.getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) {
        return new ModelAndView("redirect:/login");
    }

    Usuario usuarioReal = servicioUsuario.buscarPorId(usuarioLogueado.getId());

    // Inicializo listas
    List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
    List<DatosPublicacion> datosPublisParaTi = new ArrayList<>();

    System.out.println("========== ENTRANDO A /home ==========");
    System.out.println("Usuario logueado: " + usuarioReal.getNombre());
    System.out.println("Filtro actual: " + filtro);

    try {
        if ("r".equals(filtro)) {
            System.out.println(">> Cargando publicaciones 'Para ti'...");

            // 🔹 Análisis IA / gustos
            geminiAnalysisService.analizarYGuardarGustos(usuarioReal);

            // 🔹 Publicaciones recomendadas (Lucene)
            List<Publicacion> publisLucene = servicioRecomendaciones.recomendarParaUsuario(usuarioReal, 5);
            System.out.println("Lucene devolvió: " + publisLucene.size() + " publicaciones.");

            // 🔹 Generar contenido de bots
            //botPublisherService.publicarContenidoParaUsuario(usuarioReal.getId());



            // 🔹 Consultar publicaciones de bots
            List<Publicacion> publisBots = servicioPublicacion.obtenerPublisBotsParaUsuario(usuarioReal);
            System.out.println("Bots devolvieron: " + publisBots.size() + " publicaciones.");

            // 🔹 Convertir a DTO
            List<DatosPublicacion> datosLucene = publisLucene.stream()
                    .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                    .collect(Collectors.toList());

            List<DatosPublicacion> datosBots = publisBots.stream()
                    .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                    .collect(Collectors.toList());

            // 🔹 Combinar resultados
            datosLucene.addAll(datosBots);
            datosPublisParaTi = datosLucene;

            // 🔹 Fallback si no hay nada
            if (datosPublisParaTi.isEmpty()) {
                System.out.println("⚠️ No se encontraron publicaciones recomendadas ni de bots. Cargando todas.");
                datosPublisParaTi = servicioPublicacion.findAll().stream()
                        .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                        .collect(Collectors.toList());
            }

        } else {
            System.out.println(">> Cargando feed principal...");
            List<Publicacion> publicaciones = servicioPublicacion.findAll();
            System.out.println("Feed general tiene: " + publicaciones.size() + " publicaciones.");

            datosPublicaciones = publicaciones.stream()
                    .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                    .collect(Collectors.toList());
        }

    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("❌ Error al cargar home: " + e.getMessage());
    }

    // 🔹 Usuarios nuevos (columna derecha)
    List<DatosUsuariosNuevos> usuariosDTO = servicioUsuario.mostrarTodos()
            .stream()
            .map(u -> new DatosUsuariosNuevos(u.getNombre(), u.getApellido(), u.getId()))
            .collect(Collectors.toList());

    // 🔹 Cargar modelo final
    model.addAttribute("usuario", usuarioLogueado);
    model.addAttribute("usuariosNuevos", usuariosDTO);
    model.addAttribute("esPropio", true);
    model.addAttribute("datosPublicaciones", datosPublicaciones);
    model.addAttribute("publisParaTi", datosPublisParaTi);
    model.addAttribute("filtro", filtro);

    System.out.println("========== FIN /home ==========\n");

    return new ModelAndView("home", model);
}


    @GetMapping("/admin/publicar-anuncios")
    public ModelAndView dispararBot() {
        botPublisherService.publicarContenidoMasivo();
        // Simplemente devuelve una página de confirmación.
        return new ModelAndView("redirect:/home");
    }

}

