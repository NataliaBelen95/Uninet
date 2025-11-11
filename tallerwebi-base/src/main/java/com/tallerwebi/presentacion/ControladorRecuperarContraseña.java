package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.MailService;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosRecuperarContrasenia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorRecuperarContraseña {

    private final ServicioUsuario servicioUsuario;
    private final MailService mailService;

    @Autowired
    public ControladorRecuperarContraseña(ServicioUsuario servicioUsuario, MailService mailService) {
        this.servicioUsuario = servicioUsuario;
        this.mailService = mailService;
    }

    // Mostrar formulario inicial (si querés)
    @GetMapping("/recuperar")
    public String mostrarFormularioRecupero(Model model) {
        model.addAttribute("datosRecuperarContrasenia", new DatosRecuperarContrasenia());
        return "recuperarContrasenia";
    }

    // Paso 1: recibir email, verificar existencia y enviar código
    @PostMapping("/recuperar")
    public String procesarSolicitudRecupero(@ModelAttribute DatosRecuperarContrasenia datos, Model model, HttpSession session) {
        String email = datos.getEmail();
        if (email == null || email.isBlank()) {
            model.addAttribute("error", "Ingrese un email");
            return "recuperarContrasenia";
        }

        Usuario usuario = servicioUsuario.buscarPorEmail(email);
        if (usuario == null) {
            model.addAttribute("error", "El email no está registrado");
            return "recuperarContrasenia";
        }

        // Generar código de 6 dígitos
        String codigo = mailService.generarCodigoConfirmacion();

        // Guardar en sesión (podés guardarlo en DB si preferís persistencia)
        session.setAttribute("emailRecuperacion", email);
        session.setAttribute("codigoRecuperacion", codigo);
        // opcional: guardar timestamp para expiración
        session.setAttribute("codigoRecuperacionCreadoEn", System.currentTimeMillis());

        String asunto = "Codigo confirmacion del mail";
        // Enviar mail
        try {
            mailService.enviarMail(email, asunto,codigo);
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo enviar el mail. Intente más tarde.");
            return "recuperarContrasenia";
        }

        // redirigir a la vista para confirmar código (tu archivo confirmarMailContraseniaNueva.html)
        model.addAttribute("email", email);
        model.addAttribute("datosRecuperarContrasenia", new DatosRecuperarContrasenia());
        return "confirmarMailContraseniaNueva";
    }

    // Paso 2: verificar código ingresado por el usuario
    @PostMapping("/recuperar/confirmar")
    public String procesarConfirmacionCodigo(@RequestParam("codigo") String codigoIngresado,
                                             @RequestParam(value = "email", required = false) String emailOculto,
                                             Model model,
                                             HttpSession session) {

        String codigoEsperado = (String) session.getAttribute("codigoRecuperacion");
        String emailSesion = (String) session.getAttribute("emailRecuperacion");

        if (codigoEsperado == null || emailSesion == null) {
            model.addAttribute("error", "No hay una solicitud de recuperación activa. Vuelva a intentarlo.");
            return "recuperarContrasenia";
        }

        // Comprobar que el email oculto coincida con el de sesión (defensa extra)
        if (emailOculto != null && !emailOculto.equals(emailSesion)) {
            model.addAttribute("error", "Datos inválidos.");
            return "confirmarMailContraseniaNueva";
        }

        // Comparar código
        if (!codigoEsperado.equals(codigoIngresado.trim())) {
            model.addAttribute("error", "Código incorrecto.");
            model.addAttribute("email", emailSesion);
            return "confirmarMailContraseniaNueva";
        }

        // Código correcto -> permitir cambiar la contraseña
        DatosRecuperarContrasenia datos = new DatosRecuperarContrasenia();
        datos.setEmail(emailSesion);
        model.addAttribute("datosRecuperarContrasenia", datos);

        // Opcional: invalidar código en sesión para que no se use otra vez
        session.removeAttribute("codigoRecuperacion");
        session.removeAttribute("codigoRecuperacionCreadoEn");

        return "recuperarContrasenia"; // vista para poner nueva contraseña (usa campos nueva/repetir)
    }

    // Paso 3: actualizar la contraseña
    @PostMapping("/recuperar/nueva")
    public String procesarNuevaContrasenia(@ModelAttribute DatosRecuperarContrasenia datos, Model model) {
        String email = datos.getEmail();
        String nueva = datos.getNuevaContrasena();
        String repetir = datos.getRepetirContrasena();

        if (email == null || nueva == null || repetir == null || nueva.isBlank() || repetir.isBlank()) {
            model.addAttribute("error", "Complete todos los campos");
            model.addAttribute("datosRecuperarContrasenia", datos);
            return "recuperarContrasenia";
        }

        if (!nueva.equals(repetir)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("datosRecuperarContrasenia", datos);
            return "recuperarContrasenia";
        }

        Usuario usuario = servicioUsuario.buscarPorEmail(email);
        if (usuario == null) {
            model.addAttribute("error", "Usuario no encontrado");
            return "recuperarContrasenia";
        }

        // Actualizar contraseña (usa el método que ya tenés en ServicioUsuario)
        servicioUsuario.actualizarContrasena(usuario, nueva);

        // Redirigir al login con mensaje de éxito (podés mostrar mensaje en la vista de login)
        model.addAttribute("mensaje", "Contraseña actualizada correctamente. Inicie sesión con su nueva contraseña.");
        return "redirect:/login";
    }

}

