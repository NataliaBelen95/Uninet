package com.tallerwebi.presentacion;



import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorHomeTest {
    private ControladorHome controlador;
    private ServicioPublicado servicioPublicadoMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private Usuario usuarioLogueado;
    private ServicioLike servicioLikeMock;
    private RepositorioUsuario repositorioUsuarioMock;

    @BeforeEach
    public void init() {
        // Mocks
        servicioPublicadoMock = mock(ServicioPublicado.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);


        // Usuario de prueba
        usuarioLogueado = new Usuario();
        usuarioLogueado.setNombre("Ana");
        usuarioLogueado.setApellido("Perez");
        Carrera c1 = new Carrera();
        c1.setNombre("Carrera prueba");
        usuarioLogueado.setCarrera(c1);

        // Simular sesión
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(usuarioLogueado);
        when(servicioPublicadoMock.findAll()).thenReturn(List.of(new Publicacion(), new Publicacion()));

        // Mock publicaciones
        when(servicioPublicadoMock.findAll()).thenReturn(List.of(new Publicacion(), new Publicacion()));

        // Controlador
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioLikeMock = mock(ServicioLike.class);
        controlador = new ControladorHome(servicioPublicadoMock, servicioLikeMock);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void iniciarSesion_seCarganDatosDeUsuarioLogueadoYPublicaciones() {
        // Mock de la sesión y del usuario logueado como DatosUsuario
        HttpSession sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);

        DatosUsuario datosUsuarioMock = new DatosUsuario();
        datosUsuarioMock.setNombre("Ana");
        datosUsuarioMock.setApellido("Perez");

        Carrera c1 = new Carrera();
        c1.setNombre("Carrera prueba");
        Materia m1 = new Materia();
        m1.setNombre("Materia prueba1");
        Materia m2 = new Materia();
        m2.setNombre("Materia prueba2");
        List<Materia> materias = new ArrayList<>();
        c1.setMaterias(materias);
        datosUsuarioMock.setCarrera(c1);

        // Cuando se pida el atributo "usuarioLogueado" en la sesión, devuelve datosUsuarioMock
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);

        // Ejecutar el método home
        ModelAndView mav = controlador.home(requestMock);

        // Verificar vista
        assertEquals("home", mav.getViewName());

        Map<String, Object> model = mav.getModel();

        // Verificar DTO de usuario
        DatosUsuario datosUsuario = (DatosUsuario) model.get("usuario");

        assertNotNull(datosUsuario);
        assertEquals("Ana", datosUsuario.getNombre());
        assertEquals("Perez", datosUsuario.getApellido());
        assertEquals("Carrera prueba", datosUsuario.getCarrera().getNombre());

        // Verificar publicaciones
        List<Publicacion> publicaciones = (List<Publicacion>) model.get("publicaciones");
        assertEquals(2, publicaciones.size());

        // puede empezar vacia
        assertTrue(model.get("publicacion") == null || model.get("publicacion") instanceof Publicacion);


    }
}