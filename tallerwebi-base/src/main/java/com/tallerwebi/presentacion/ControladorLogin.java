package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;


    /* hacer un controlador Home y ordenar Luego ***/
    @Autowired

    private RepositorioUsuario repositorioUsuario;
    private ServicioCarrera servicioCarrera;
          /******     ***/

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, RepositorioUsuario repositorioUsuario, ServicioCarrera servicioCarrera) {
        this.servicioLogin = servicioLogin;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioCarrera = servicioCarrera;
    }

    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());

        return new ModelAndView("login", modelo);
    }

    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            // Crear DTO para guardar en sesión
            DatosUsuario datosUsuario = new DatosUsuario();
            datosUsuario.setNombre(usuarioBuscado.getNombre());
            datosUsuario.setApellido(usuarioBuscado.getApellido());
            datosUsuario.setEmail(usuarioBuscado.getEmail());
            datosUsuario.setCarrera(usuarioBuscado.getCarrera());
            datosUsuario.setId(usuarioBuscado.getId());


            request.getSession().setAttribute("usuarioLogueado", datosUsuario);
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());

            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
            return new ModelAndView("login", model);
        }
    }

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();

        try {
            usuario.setRol("USER");
            servicioLogin.registrar(usuario);


        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }

        // Redirigir al login si todo sale bien
        System.out.println("Registrando usuario: " + usuario.getEmail() + " - pass=" + usuario.getPassword());
        return new ModelAndView("redirect:/login");

    }
    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());

        // Traer todas las carreras desde la base

        List<Carrera> carreras = servicioCarrera.buscarTodas();
        System.out.println("📚 Carreras disponibles:");
        for (Carrera c : carreras) {
            System.out.println("ID: " + c.getId() + " - Nombre: " + c.getNombre());
        }
        model.put("todasLasCarreras", carreras);
        return new ModelAndView("nuevo-usuario", model);
    }
}

