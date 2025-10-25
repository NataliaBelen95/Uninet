package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorMiPerfilTest {

    private ControladorMiPerfil controladorMiPerfil;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioUsuario servicioUsuarioMock;
    private MultipartFile fotoMock;
    private ServicioGenero servicioGeneroMock;
    private ServicioLogin servicioLoginMock;
    private UsuarioMapper usuarioMapperMock;
    private ServicioPublicacion servicioPublicacionMock;
    private PublicacionMapper publicacionMapperMock;
    private ServicioNotificacion servicioNotificacionMock;
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {


        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);

        servicioUsuarioMock = mock(ServicioUsuario.class);
        servicioGeneroMock = mock(ServicioGenero.class);
        servicioLoginMock = mock(ServicioLogin.class);
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        publicacionMapperMock = mock(PublicacionMapper.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        usuarioMapperMock = mock(UsuarioMapper.class);

        //Para cargr fotos
        fotoMock = mock(MultipartFile.class);


        controladorMiPerfil = new ControladorMiPerfil(servicioUsuarioMock, servicioGeneroMock,
                                                      servicioLoginMock, servicioNotificacionMock,
                                                      usuarioMapperMock, servicioPublicacionMock,
                                                      publicacionMapperMock

        );
        // Usar una vista dummy para que no intente renderizar
        mockMvc = MockMvcBuilders.standaloneSetup(controladorMiPerfil)
                .setSingleView(new org.springframework.web.servlet.view.InternalResourceView("/dummy"))
                .build();
    }
    @Test
    public void queAlIrAMiPerfilConUsuarioLogueadoMuestrePerfil() throws Exception {
        DatosUsuario usuarioLogueado = new DatosUsuario();
        usuarioLogueado.setId(1L);
        usuarioLogueado.setSlug("juan-perez");

        Usuario usuarioPerfil = new Usuario();
        usuarioPerfil.setId(1L);

        when(servicioUsuarioMock.buscarPorSlug("juan-perez")).thenReturn(usuarioPerfil);
        when(usuarioMapperMock.toDtoPropio(usuarioPerfil)).thenReturn(usuarioLogueado);
        when(servicioGeneroMock.listarGeneros()).thenReturn(Collections.emptyList());
        when(servicioPublicacionMock.obtenerPublicacionesDeUsuario(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/miPerfil")
                        .sessionAttr("usuarioLogueado", usuarioLogueado))
                .andExpect(status().isOk())
                .andExpect(view().name("miPerfil"))
                .andExpect(model().attribute("usuario", usuarioLogueado))
                .andExpect(model().attribute("esPropio", true));
    }




    @Test
    public void queAlIrAPerfilAjenoConMockMvcObtengaDatosPublicosYNoActualiceSesion() throws Exception {
        // Datos de sesión
        DatosUsuario usuarioLogueado = new DatosUsuario();
        usuarioLogueado.setId(1L);
        usuarioLogueado.setSlug("juan-perez");

        // Usuario del perfil ajeno
        Usuario usuarioPerfil = new Usuario();
        usuarioPerfil.setId(2L);

        when(servicioUsuarioMock.buscarPorSlug("maria-gomez")).thenReturn(usuarioPerfil);
        when(servicioGeneroMock.listarGeneros()).thenReturn(Collections.emptyList());
        when(servicioPublicacionMock.obtenerPublicacionesDeUsuario(2L)).thenReturn(Collections.emptyList());
        when(usuarioMapperMock.toDtoPublico(usuarioPerfil)).thenReturn(new DatosUsuario());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuarioLogueado", usuarioLogueado);

        mockMvc.perform(get("/perfil/{slug}", "maria-gomez").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("miPerfil"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("esPropio", false));

        verify(servicioUsuarioMock).buscarPorSlug("maria-gomez");
        verify(usuarioMapperMock).toDtoPublico(usuarioPerfil);
        verifyNoInteractions(servicioNotificacionMock);
    }


    @Test
    public void queAlIrAPerfilSinUsuarioEnSesionConMockMvcMuestrePerfilPublico() throws Exception {
        // Usuario del perfil
        Usuario usuarioPerfil = new Usuario();
        usuarioPerfil.setId(2L);

        when(servicioUsuarioMock.buscarPorSlug("maria-gomez")).thenReturn(usuarioPerfil);
        when(servicioGeneroMock.listarGeneros()).thenReturn(Collections.emptyList());
        when(servicioPublicacionMock.obtenerPublicacionesDeUsuario(2L)).thenReturn(Collections.emptyList());
        when(usuarioMapperMock.toDtoPublico(usuarioPerfil)).thenReturn(new DatosUsuario());

        MockHttpSession session = new MockHttpSession(); // sesión vacía

        mockMvc.perform(get("/perfil/{slug}", "maria-gomez").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("miPerfil"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("esPropio", false));

        verify(servicioUsuarioMock).buscarPorSlug("maria-gomez");
        verify(usuarioMapperMock).toDtoPublico(usuarioPerfil);
        verifyNoInteractions(servicioNotificacionMock);
    }

}

