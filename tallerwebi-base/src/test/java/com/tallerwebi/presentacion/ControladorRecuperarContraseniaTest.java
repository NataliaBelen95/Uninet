package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.MailService;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorRecuperarContraseniaTest {

    private ControladorRecuperarContraseña controlador;
    private ServicioUsuario servicioUsuarioMock;
    private MailService mailServiceMock;

    @BeforeEach
    public void init() {
        servicioUsuarioMock = mock(ServicioUsuario.class);
        mailServiceMock = mock(MailService.class);

        // instanciamos el controlador con los mocks
        controlador = new ControladorRecuperarContraseña(servicioUsuarioMock, mailServiceMock);
    }

    @Test
    public void mostrarFormularioRecuperoAgregaObjetoAlModelo_yDevuelveVista() {
        Model modelMock = mock(Model.class);

        String view = controlador.mostrarFormularioRecupero(modelMock);

        assertEquals("recuperarContrasenia", view);
        verify(modelMock, times(1)).addAttribute(eq("datosRecuperarContrasenia"), any(DatosRecuperarContrasenia.class));
    }

    @Test
    public void procesarSolicitudRecuperoEmailNoRegistradoMuestraError() {
        // Preparación
        DatosRecuperarContrasenia datos = new DatosRecuperarContrasenia();
        datos.setEmail("no@mail.com");

        when(servicioUsuarioMock.buscarPorEmail("no@mail.com")).thenReturn(null);

        Model modelMock = mock(Model.class);
        HttpSession sessionMock = mock(HttpSession.class);

        String view = controlador.procesarSolicitudRecupero(datos, modelMock, sessionMock);

        assertEquals("recuperarContrasenia", view);
        verify(servicioUsuarioMock, times(1)).buscarPorEmail("no@mail.com");
        verify(modelMock, times(1)).addAttribute(eq("error"), anyString());
        verifyNoInteractions(mailServiceMock);
    }

    @Test
        public void procesarSolicitudRecuperoEmailRegistradoEnviaCodigoYMuestraConfirmacion() {
        // Preparación
        DatosRecuperarContrasenia datos = new DatosRecuperarContrasenia();
        datos.setEmail("si@mail.com");

        Usuario usuario = new Usuario();
        usuario.setEmail("si@mail.com");

        when(servicioUsuarioMock.buscarPorEmail("si@mail.com")).thenReturn(usuario);
        when(mailServiceMock.generarCodigoConfirmacion()).thenReturn("123456");

        Model modelMock = mock(Model.class);
        HttpSession sessionMock = mock(HttpSession.class);

        String view = controlador.procesarSolicitudRecupero(datos, modelMock, sessionMock);

        assertEquals("confirmarMailContraseniaNueva", view);
        // Verificar que se guardó en sesión (usamos verify sobre el mock de session)
        verify(sessionMock, times(1)).setAttribute("emailRecuperacion", "si@mail.com");
        verify(sessionMock, times(1)).setAttribute("codigoRecuperacion", "123456");
        verify(mailServiceMock, times(1)).generarCodigoConfirmacion();
        verify(mailServiceMock, times(1)).enviarMail(eq("si@mail.com"), anyString(), eq("123456"));
        // el modelo debe incluir email y datosRecuperarContrasenia
        verify(modelMock, times(1)).addAttribute(eq("email"), eq("si@mail.com"));
        verify(modelMock, times(1)).addAttribute(eq("datosRecuperarContrasenia"), any(DatosRecuperarContrasenia.class));
    }

    @Test
    public void procesarConfirmacionCodigoCodigoCorrectoMuestraFormularioNuevaContraseniaYInvalidaCodigoEnSesion() {
        // Preparación: sesión con email y código esperados
        HttpSession sessionMock = mock(HttpSession.class);
        when(sessionMock.getAttribute("codigoRecuperacion")).thenReturn("123456");
        when(sessionMock.getAttribute("emailRecuperacion")).thenReturn("si@mail.com");

        Model modelMock = mock(Model.class);

        String view = controlador.procesarConfirmacionCodigo("123456", "si@mail.com", modelMock, sessionMock);

        assertEquals("recuperarContrasenia", view);
        // Debe setear en el modelo el objeto para la nueva contraseña
        verify(modelMock, times(1)).addAttribute(eq("datosRecuperarContrasenia"), any(DatosRecuperarContrasenia.class));
        // Debe haber removido el código de la sesión (invalidación)
        verify(sessionMock, times(1)).removeAttribute("codigoRecuperacion");
        verify(sessionMock, times(1)).removeAttribute("codigoRecuperacionCreadoEn");
    }

    @Test
    public void procesarConfirmacionCodigoCodigoIncorrectoMuestraErrorYPermaneceEnConfirmacion() {
        HttpSession sessionMock = mock(HttpSession.class);
        when(sessionMock.getAttribute("codigoRecuperacion")).thenReturn("123456");
        when(sessionMock.getAttribute("emailRecuperacion")).thenReturn("si@mail.com");

        Model modelMock = mock(Model.class);

        String view = controlador.procesarConfirmacionCodigo("000000", "si@mail.com", modelMock, sessionMock);

        assertEquals("confirmarMailContraseniaNueva", view);
        verify(modelMock, times(1)).addAttribute(eq("error"), anyString());
        // No debe invalidar el código si fue incorrecto
        verify(sessionMock, never()).removeAttribute("codigoRecuperacion");
    }

    @Test
    public void procesarNuevaContraseniaConDatosValidosActualizaContrasenaYRedirigeALogin() {
        DatosRecuperarContrasenia datos = new DatosRecuperarContrasenia();
        datos.setEmail("si@mail.com");
        datos.setNuevaContrasena("Nueva123!");
        datos.setRepetirContrasena("Nueva123!");

        Usuario usuario = new Usuario();
        usuario.setEmail("si@mail.com");

        when(servicioUsuarioMock.buscarPorEmail("si@mail.com")).thenReturn(usuario);

        Model modelMock = mock(Model.class);

        String view = controlador.procesarNuevaContrasenia(datos, modelMock);

        // El controlador recomendado devuelve redirect:/login para seguir PRG
        assertEquals("redirect:/login", view);
        verify(servicioUsuarioMock, times(1)).buscarPorEmail("si@mail.com");
        verify(servicioUsuarioMock, times(1)).actualizarContrasena(usuario, "Nueva123!");
    }

    @Test
    public void procesarNuevaContraseniaConDatosInvalidosMuestraError() {
        DatosRecuperarContrasenia datos = new DatosRecuperarContrasenia();
        datos.setEmail("si@mail.com");
        datos.setNuevaContrasena("a");
        datos.setRepetirContrasena("b"); // mismatch

        Model modelMock = mock(Model.class);

        String view = controlador.procesarNuevaContrasenia(datos, modelMock);

        assertEquals("recuperarContrasenia", view);
        verify(modelMock, times(1)).addAttribute(eq("error"), anyString());
        // No debe llamar al servicio para actualizar
        verify(servicioUsuarioMock, never()).actualizarContrasena(any(), anyString());
    }
}