package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.departamento.Departamento;
import com.tallerwebi.dominio.departamento.ServicioDepartamento;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    @Autowired
    private MailService mailService;
    private RepositorioUsuario repositorioUsuario;
    private ServicioCarrera servicioCarrera;
    private ServicioDepartamento servicioDepartamento;
    /* hacer un controlador Home y ordenar Luego ***/


    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, RepositorioUsuario repositorioUsuario, ServicioCarrera servicioCarrera,ServicioDepartamento servicioDepartamento) {
        this.servicioLogin = servicioLogin;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioCarrera = servicioCarrera;
        this.servicioDepartamento = servicioDepartamento;
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
            datosUsuario.setDepartamento(usuarioBuscado.getDepartamento());
            datosUsuario.setId(usuarioBuscado.getId());
            datosUsuario.setFotoPerfil(usuarioBuscado.getFotoPerfil());

            request.getSession().setAttribute("usuarioLogueado", datosUsuario);
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());

            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
            return new ModelAndView("login", model);
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public ModelAndView cerrarSesion(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ModelAndView("redirect:/login");
    }


    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();
        model.put("todasLasCarreras", servicioCarrera.buscarTodas());
        model.put("todosLosDepartamentos",servicioDepartamento.obtenerDepartamentos());

        try {
            usuario.setRol("USER");
            // Generar código de confirmación
            String codigo = mailService.generarCodigoConfirmacion();
            usuario.setCodigoConfirmacion(codigo);
            usuario.setConfirmado(false);

            // Guardar usuario en DB
            servicioLogin.registrar(usuario);

            // Enviar mail con código
            String asunto = "Confirma tu registro";
            String texto = "Tu código de confirmación es: " + codigo;
            mailService.enviarMail(usuario.getEmail(), asunto, texto);

            // Redirigir a la pantalla para validar código
            model.put("email", usuario.getEmail());
            return new ModelAndView("validar-codigo", model);

        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
    }

    @RequestMapping(path = "/validar-codigo", method = RequestMethod.POST)
    @Transactional
    public ModelAndView validarCodigo(@RequestParam("email") String email,
                                      @RequestParam("codigo") String codigo) {
        ModelMap model = new ModelMap();
        Usuario usuario = repositorioUsuario.buscar(email); // tu mtodo para buscar usuario

        if (usuario.getCodigoConfirmacion().equals(codigo)) {
            usuario.setConfirmado(true);
            repositorioUsuario.guardar(usuario);
            return new ModelAndView("redirect:/login");
        } else {
            model.put("error", "Código incorrecto");
            model.put("email", email);
            return new ModelAndView("validar-codigo", model);
        }
    }


    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();

        Usuario nuevo = new Usuario();
        nuevo.setDepartamento(null);
        nuevo.setCarrera(null);

        model.put("usuario", nuevo);

        List<Departamento>departamentos= servicioDepartamento.obtenerDepartamentos();

        model.put("todosLosDepartamentos", departamentos);

        // Traer todas las carreras desde la base
//        List<Carrera> carreras = servicioCarrera.buscarTodas();
//        System.out.println("📚 Carreras disponibles:");
//        for (Carrera c : carreras) {
//            System.out.println("ID: " + c.getId() + " - Nombre: " + c.getNombre());
//        }
        //model.put("todasLasCarreras", carreras);
//lo mando vacio ya que despues se va a cargar al seleccionar un departamento
        model.put("todasLasCarreras", List.of());




        return new ModelAndView("nuevo-usuario", model);
    }

}