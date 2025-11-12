package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import com.tallerwebi.presentacion.DTO.DatosPublicacion;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import com.tallerwebi.presentacion.DTO.DatosUsuariosNuevos;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ControladorHome {


    private final ServicioUsuario servicioUsuario;
    private final BotPublisherService botPublisherService;
    private final ServicioFeed servicioFeed;
    //para no esperar 6 hs
    private final GeminiAnalysisService geminiAnalysisService;
    private final ServicioAmistad servicioAmistad;





    public ControladorHome(ServicioUsuario servicioUsuario,
                           BotPublisherService botPublisherService, ServicioFeed servicioFeed, GeminiAnalysisService geminiAnalysisService, ServicioAmistad servicioAmistad) {

        this.servicioUsuario = servicioUsuario;
        this.botPublisherService = botPublisherService;
        this.servicioFeed = servicioFeed;
        this.geminiAnalysisService = geminiAnalysisService;
        this.servicioAmistad = servicioAmistad;


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
                         @RequestParam(value = "filtro", defaultValue = "p") String filtro) throws Exception {

    ModelMap model = new ModelMap();


    DatosUsuario usuarioLogueado = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) {
        return new ModelAndView("redirect:/login");
    }

    Usuario usuarioReal = servicioUsuario.buscarPorId(usuarioLogueado.getId());


    List<DatosPublicacion> datosPublicaciones = "r".equals(filtro)
            ? servicioFeed.obtenerFeedRecomendado(usuarioReal, usuarioLogueado.getId())
            : servicioFeed.obtenerFeedPrincipal(usuarioLogueado.getId());

    List<Usuario> todosLosUsuarios = servicioUsuario.mostrarTodos(); // devuelve DatosUsuario con isEsBot()
    long idLogueado = usuarioLogueado.getId();

    Set<Long> idsAmigos = servicioAmistad.obtenerIdsAmigosDe(idLogueado);

    List<DatosUsuariosNuevos> usuariosDTO = todosLosUsuarios.stream()
            .filter(u -> u.getId() != idLogueado)           // no mostrarme a mí mismo
            .filter(u -> !u.isEsBot())                      // excluir bots
            .filter(u -> !idsAmigos.contains(u.getId()))    // excluir ya amigos
            .map(u -> new DatosUsuariosNuevos(
                    u.getNombre(),
                    u.getApellido(),
                    u.getId()
            ))
            .collect(Collectors.toList());

    model.addAttribute("usuario", usuarioLogueado);
    model.addAttribute("usuariosNuevos", usuariosDTO);
    model.addAttribute("esPropio", true);
    model.addAttribute("datosPublicaciones", datosPublicaciones);
    model.addAttribute("filtro", filtro);

    return new ModelAndView("home", model);
}


    @GetMapping("/admin/publicar-anuncios")
    public ModelAndView dispararBot() {
        botPublisherService.ejecutarCampañaPublicitariaDirigida();
        // Simplemente devuelve una página de confirmación.
        return new ModelAndView("redirect:/home");
    }
//    @GetMapping("/admin/analizar-gustos")
//    public ModelAndView analizarGustos(HttpServletRequest request) {
//
//        DatosUsuario usuarioLogueado = (DatosUsuario) request.getSession().getAttribute("usuarioLogueado");
//        Usuario usuarioReal = servicioUsuario.buscarPorId(usuarioLogueado.getId());
//        geminiAnalysisService.analizarYGuardarGustos(usuarioReal);
//        // Simplemente devuelve una página de confirmación.
//        return new ModelAndView("redirect:/home");
//    }
//



}

