package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.RepositorioSolicitudAmistad;
import com.tallerwebi.dominio.LuceneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ServicioLuceneTest {
    private LuceneService servicioLuceneMock;

    @BeforeEach
    public void init() {
        servicioLuceneMock = mock(LuceneService.class);

    }

    @Test
    void listarAmigosDelegaAlRepositorioYDevuelveLista() {
        Publicacion p = new Publicacion();

        p.setId(2L);
        p.setDescripcion("programar con javascrip y spring");



    }
}
