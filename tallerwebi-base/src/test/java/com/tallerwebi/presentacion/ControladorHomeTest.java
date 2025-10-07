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
    public void home_UsuarioLogueadoVeDatosPublicacionesYLikes() {
        // Mock del request y la sesión
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpSession sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);

        // Datos del usuario logueado (DTO)
        DatosUsuario datosUsuarioMock = new DatosUsuario();
        datosUsuarioMock.setNombre("Ana");
        datosUsuarioMock.setApellido("Perez");

        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(datosUsuarioMock);

        // Usuario y publicaciones mock
        Usuario autor = new Usuario();
        autor.setNombre("Pepe");

        Publicacion pub1 = new Publicacion();
        pub1.setId(1L);
        pub1.setDescripcion("Publicación 1");
        pub1.setUsuario(autor);
        pub1.setComentarios(new ArrayList<>());

        Publicacion pub2 = new Publicacion();
        pub2.setId(2L);
        pub2.setDescripcion("Publicación 2");
        pub2.setUsuario(autor);
        pub2.setComentarios(new ArrayList<>());

        List<Publicacion> publicaciones = List.of(pub1, pub2);

        when(servicioPublicadoMock.findAll()).thenReturn(publicaciones);
        when(servicioLikeMock.contarLikes(pub1)).thenReturn(5);
        when(servicioLikeMock.contarLikes(pub2)).thenReturn(3);

        // Instanciar el controlador con mocks
        ControladorHome controlador = new ControladorHome(servicioPublicadoMock, servicioLikeMock);

        // Ejecutar
        ModelAndView mav = controlador.home(requestMock);

        // Validar vista
        assertEquals("home", mav.getViewName());

        // Validar datos del modelo
        Map<String, Object> model = mav.getModel();
        DatosUsuario usuarioEnModelo = (DatosUsuario) model.get("usuario");
        assertNotNull(usuarioEnModelo);
        assertEquals("Ana", usuarioEnModelo.getNombre());

        // Validar lista de DatosPublicacion
        List<DatosPublicacion> datosPublicaciones = (List<DatosPublicacion>) model.get("datosPublicaciones");
        assertNotNull(datosPublicaciones);
        assertEquals(2, datosPublicaciones.size());

        DatosPublicacion dto1 = datosPublicaciones.get(0);
        assertEquals("Publicación 1", dto1.getDescripcion());
        assertEquals(5, dto1.getCantLikes());

        DatosPublicacion dto2 = datosPublicaciones.get(1);
        assertEquals("Publicación 2", dto2.getDescripcion());
        assertEquals(3, dto2.getCantLikes());
    }
}