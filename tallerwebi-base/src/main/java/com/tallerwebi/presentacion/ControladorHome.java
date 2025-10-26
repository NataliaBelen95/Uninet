package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ControladorHome {

    private final PublicacionMapper publicacionMapper;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioLike servicioLike;
    private final ServicioUsuario servicioUsuario;




    public ControladorHome(ServicioUsuario servicioUsuario, ServicioPublicacion servicioPublicacion, ServicioLike servicioLike, PublicacionMapper publicacionMapper) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioLike = servicioLike;
        this.publicacionMapper = publicacionMapper;
        this.servicioUsuario = servicioUsuario;

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
    public ModelAndView home(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();

        // Obtener el usuario logueado
        DatosUsuario usuario = (DatosUsuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        //dtoUsuario
        model.addAttribute("usuario", usuario);



        // Ahora la lista de publicaciones ya trae los comentarios por estar con `EAGER` en la entidad
        List<Publicacion> publicaciones = servicioPublicacion.findAll();

        // Mapear las publicaciones a DTOs
        List<DatosPublicacion> datosPublicaciones = new ArrayList<>();
        for (Publicacion publicacion : publicaciones) {
            DatosPublicacion dto = publicacionMapper.toDto(publicacion, usuario.getId());
            //System.out.println("Publicacion usuarioId: " + dto.getUsuarioId());
            datosPublicaciones.add(dto);
        }
        List<DatosUsuariosNuevos> usuariosDTO = servicioUsuario.mostrarTodos()
                .stream()
                .map(u -> new DatosUsuariosNuevos(
                        u.getNombre(),
                        u.getApellido(),
                        u.getId()
                ))
                .collect(Collectors.toList());

        // 4. Pasar datos al modelo
        model.addAttribute("usuariosNuevos", usuariosDTO);
        model.addAttribute("esPropio", true);
        model.addAttribute("datosPublicaciones", datosPublicaciones);
        return new ModelAndView("home", model);
    }


}

