package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioAmigoTest {

    private RepositorioAmistad repositorioAmigoMock;
    private ServicioAmistadImpl servicioAmigo;
    private RepositorioSolicitudAmistad repositorioSolicitudAmistadMock;

    @BeforeEach
    public void init() {
        repositorioAmigoMock = mock(RepositorioAmistad.class);
        repositorioSolicitudAmistadMock = mock(RepositorioSolicitudAmistad.class);
        servicioAmigo = new ServicioAmistadImpl(repositorioSolicitudAmistadMock, repositorioAmigoMock);
    }

    @Test
    void listarAmigosDelegaAlRepositorioYDevuelveLista() {
        Usuario amigo1 = new Usuario();
        amigo1.setId(2L);
        Usuario amigo2 = new Usuario();
        amigo2.setId(3L);

        when(repositorioAmigoMock.obtenerAmigosDeUsuario(1L)).thenReturn(List.of(amigo1, amigo2));

        List<Usuario> resultado = servicioAmigo.obtenerAmigosDeUsuario(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertSame(amigo1, resultado.get(0));
        assertSame(amigo2, resultado.get(1));

        verify(repositorioAmigoMock, times(1)).obtenerAmigosDeUsuario(1L);
    }

}