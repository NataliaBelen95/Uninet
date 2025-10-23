package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorMiPerfilTest {

    private ControladorMiPerfil controladorMiPerfil;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioUsuario servicioUsuarioMock;
    private MultipartFile fotoMock;

    @BeforeEach
    public void init() {
        //Preparación
        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn("dbcalaz@gmail.com");

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);

        servicioUsuarioMock = mock(ServicioUsuario.class);

        //Para cargr fotos
        fotoMock = mock(MultipartFile.class);
    }
    @Test
    public void queSePuedaCargarUnaFotoDePerfilCorrectament(){

    }
}
