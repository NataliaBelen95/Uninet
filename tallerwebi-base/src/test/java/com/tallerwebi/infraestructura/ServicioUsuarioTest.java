package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioUsuarioTest {

    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioUsuario servicioUsuario;
    private RepositorioUsuario RepositorioUsuarioMock;

    @BeforeEach
    public void init() {
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        RepositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioUsuario = new ServicioUsuario(repositorioUsuarioMock);
    }

    @Test
    void buscarPorEmailDelegaAlRepositorioYDevuelveUsuario() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setEmail("test@unlam.edu.ar");

        when(repositorioUsuarioMock.buscar("test@unlam.edu.ar")).thenReturn(u);

        Usuario resultado = servicioUsuario.buscarPorEmail("test@unlam.edu.ar");

        assertNotNull(resultado, "Se esperaba un usuario no nulo devuelto por el servicio");
        assertEquals(5L, resultado.getId());
        assertEquals("test@unlam.edu.ar", resultado.getEmail());

        verify(repositorioUsuarioMock, times(1)).buscar("test@unlam.edu.ar");
    }

    @Test
    void buscarPorIdDelegaAlRepositorioYDevuelveUsuario() {
        Usuario u = new Usuario();
        u.setId(7L);

        when(repositorioUsuarioMock.buscarPorId(7L)).thenReturn(u);

        Usuario resultado = servicioUsuario.buscarPorId(7L);

        assertNotNull(resultado);
        assertEquals(7L, resultado.getId());

        verify(repositorioUsuarioMock, times(1)).buscarPorId(7L);
    }

    @Test
    void mostrarTodosDevuelveListaDeUsuarios() {
        Usuario u1 = new Usuario();
        Usuario u2 = new Usuario();

        when(repositorioUsuarioMock.buscarTodos()).thenReturn(List.of(u1, u2));

        List<Usuario> resultado = servicioUsuario.mostrarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repositorioUsuarioMock, times(1)).buscarTodos();
    }

    @Test
    void actualizarLlamaActualizarEnRepositorio() {
        Usuario u = new Usuario();
        servicioUsuario.actualizar(u);
        verify(repositorioUsuarioMock, times(1)).actualizar(u);
    }

}