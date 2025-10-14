package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.EmailNoInstitucional;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.RepositorioUsuarioImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;




public class ControladorLoginTest {

    private ControladorLogin controladorLogin;
    private Usuario usuarioMock;
    private DatosLogin datosLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioLogin servicioLoginMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private RepositorioUsuarioImpl servicioUsuarioMock;
    private ServicioCarrera servicioCarreraMock;



    @BeforeEach
    public void init(){

        datosLoginMock = new DatosLogin("dami@unlam.com", "123");

        usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
        when(usuarioMock.getPassword()).thenReturn("123"); // extra seguridad

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock); // <-- agregado global

        servicioLoginMock = mock(ServicioLogin.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);

        // Mocks para ControladorRegistro
        servicioUsuarioMock = mock(RepositorioUsuarioImpl.class);

        servicioCarreraMock = mock(ServicioCarrera.class);

        controladorLogin = new ControladorLogin(servicioLoginMock, repositorioUsuarioMock, servicioCarreraMock);

    }


    @Test
    public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente(){
        // preparacion
        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Usuario o clave incorrecta"));
        verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
        verify(sessionMock, never()).setAttribute(eq("usuarioLogueado"), any());
    }

    @Test
    public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHome(){
        // preparacion
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuarioEncontradoMock);

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(sessionMock).setAttribute(eq("ROL"), eq("ADMIN"));
        // <-- Verificar que el usuario completo se guardó en sesión
        verify(sessionMock).setAttribute(eq("usuarioLogueado"), any(DatosUsuario.class));
    }

    @Test
    public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin() throws UsuarioExistente, EmailNoInstitucional {

        // ejecucion
        ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioLoginMock, times(1)).registrar(usuarioMock);
    }

    @Test
    public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente, EmailNoInstitucional {
        // preparacion
        doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

        // ejecucion
        ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El usuario ya existe"));
    }

    @Test
    public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente, EmailNoInstitucional {
        // preparacion
        doThrow(RuntimeException.class).when(servicioLoginMock).registrar(usuarioMock);

        // ejecucion
        ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Error al registrar el nuevo usuario"));
    }


    @Test
    public void loginConUsuarioRolUserDeberiaLLevarAHome(){
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(usuarioEncontradoMock.getRol()).thenReturn("USER"); // ahora USER

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuarioEncontradoMock);

        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(sessionMock).setAttribute(eq("ROL"), eq("USER"));
        verify(sessionMock).setAttribute(eq("usuarioLogueado"), any(DatosUsuario.class));
    }
}