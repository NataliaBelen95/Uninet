package com.tallerwebi.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.BotPublisherServiceImpl;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

public class BotPublisherServiceTest {

    private RepositorioGustoPersonal repositorioGustoPersonalMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioPublicacion servicioPublicacionMock;
    private ObjectMapper objectMapperMock;
    private ServicioImagenIA servicioImagenIAMock;
    private LuceneService luceneServiceMock;
    private ServicioIntegracionIA servicioIntegracionIAMock;
    private GeminiAnalysisService geminiAnalysisServiceMock;
    private BotPublisherService botPublisherService;

    @BeforeEach
    public void init() {
        repositorioGustoPersonalMock = mock(RepositorioGustoPersonal.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioPublicacionMock = mock(ServicioPublicacion.class);
        objectMapperMock = mock(ObjectMapper.class);
        servicioImagenIAMock = mock(ServicioImagenIA.class);
        luceneServiceMock = mock(LuceneService.class);
        servicioIntegracionIAMock = mock(ServicioIntegracionIA.class);
        botPublisherService = new BotPublisherServiceImpl(repositorioGustoPersonalMock, repositorioUsuarioMock,
                          servicioPublicacionMock, objectMapperMock, servicioImagenIAMock, servicioIntegracionIAMock) {
        };




    }
}
