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





    public ControladorHome(ServicioUsuario servicioUsuario, ServicioPublicacion servicioPublicacion,
                           ServicioLike servicioLike, PublicacionMapper publicacionMapper,
                           ServicioRecomendaciones servicioRecomendaciones, GeminiAnalysisService geminiAnalysisService) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.publicacionMapper = publicacionMapper;
        this.servicioUsuario = servicioUsuario;
        this.servicioRecomendaciones = servicioRecomendaciones;
        this.geminiAnalysisService = geminiAnalysisService;



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

    // 1. Inicializar listas de publicaciones
    List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
    List<DatosPublicacion> datosPublisParaTi = new ArrayList<>();

    // 2. Lógica de filtrado (Determina qué publicaciones cargar)
    if ("r".equals(filtro)) {
        //  FILTRO PARA TI (Recomendaciones IA/Lucene)

        // Lógica de IA asíncrona (Solo consume si han pasado 6 horas)
        geminiAnalysisService.analizarYGuardarGustos(usuarioReal);

        // Obtener las publicaciones recomendadas
        try {
            List<Publicacion> publisParaTi = servicioRecomendaciones.recomendarParaUsuario(usuarioReal, 5);
            datosPublisParaTi = publisParaTi.stream()
                    .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error de IA/Lucene: " + e.getMessage());
        }

    } else {
        //  FILTRO PRINCIPAL ('p' o por defecto)
        List<Publicacion> publicaciones = servicioPublicacion.findAll();
        datosPublicaciones = publicaciones.stream()
                .map(p -> publicacionMapper.toDto(p, usuarioLogueado.getId()))
                .collect(Collectors.toList());
    }

    // 3. Obtener y poblar DATOS COMUNES (Estos datos van siempre)

    // Obtener la lista de usuarios para la columna derecha
    List<DatosUsuariosNuevos> usuariosDTO = servicioUsuario.mostrarTodos()
            .stream()
            .map(u -> new DatosUsuariosNuevos(u.getNombre(), u.getApellido(), u.getId()))
            .collect(Collectors.toList());

    // 4. Poblar el modelo FINAL
    model.addAttribute("usuario", usuarioLogueado);
    model.addAttribute("usuariosNuevos", usuariosDTO); // ✅ Reintegrado
    model.addAttribute("esPropio", true); // ✅ Reintegrado

    // Publicaciones (una de estas listas estará llena)
    model.addAttribute("datosPublicaciones", datosPublicaciones);
    model.addAttribute("publisParaTi", datosPublisParaTi);
    model.addAttribute("filtro", filtro);

    return new ModelAndView("home", model);
}

}

