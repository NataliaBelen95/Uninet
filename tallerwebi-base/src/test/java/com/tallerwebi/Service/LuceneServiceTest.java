package com.tallerwebi.Service;

import com.tallerwebi.dominio.IndexWriterFactory;
import com.tallerwebi.dominio.LuceneDirectoryManager;
import com.tallerwebi.dominio.LuceneService;
import com.tallerwebi.dominio.Publicacion;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class LuceneServiceTest {

    private LuceneService luceneService;
    private LuceneDirectoryManager directoryManager;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // directorio temporal para el test
        tempDir = Files.createTempDirectory("lucene-test-");

        // Inicializa el manager y el servicio
        directoryManager = new LuceneDirectoryManager();
        directoryManager.switchToDisk(tempDir.toString());

        luceneService = new LuceneService(directoryManager);

        // Limpia el índice antes de cada test
        luceneService.limpiarIndice();
    }

    @AfterEach
    void tearDown() throws IOException {
        directoryManager.close();

        // Borra el directorio temporal
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void queLuceneIndexeeTodasLasPublicacionesMenosLaQueSonPublicidad_UsandoMock() throws IOException {
        // Preparación de datos
        Publicacion pOrganica = new Publicacion();
        pOrganica.setId(2L);
        pOrganica.setDescripcion("programar con javascript y spring");
        pOrganica.setEsPublicidad(false); // DEBE INDEXARSE

        Publicacion pPublicidad = new Publicacion();
        pPublicidad.setId(3L);
        pPublicidad.setDescripcion("anotate en curso js");
        pPublicidad.setEsPublicidad(true); // NO DEBE INDEXARSE

        List<Publicacion> publicaciones = List.of(pOrganica, pPublicidad);

        // 1. Configuración del Mock
        IndexWriter writerMock = mock(IndexWriter.class);
        //  mock de la Factoría
        luceneService.setIndexWriterFactory(() -> writerMock);

        // Ejecución
        luceneService.indexarPublicaciones(publicaciones);


        // Verifica que la publicación orgánica (ID 2) haya sido agregada
        verify(writerMock, times(1)).addDocument(argThat(new ArgumentMatcher<Document>() {
            @Override
            public boolean matches(Document luceneDoc) {
                // Aquí no hay error de inferencia porque el tipo Document está declarado
                return luceneDoc != null && "2".equals(luceneDoc.get("id"));
            }
        }));
        //problemas con doc.get , usar override funion mokito(Cannot resolve method 'get' in 'Iterable')
        //l argumento que pasa por el matcher debe ser un Document de Lucene
        // Verifica que la publicación publicitaria (ID 3) NUNCA haya sido agregada
        verify(writerMock, never()).addDocument(argThat(new ArgumentMatcher<Document>() {
            @Override
            public boolean matches(Document luceneDoc) {
                return luceneDoc != null && "3".equals(luceneDoc.get("id"));
            }
        }));


        verify(writerMock, times(1)).addDocument(any(Document.class));

        // close indexWritter
        verify(writerMock, times(1)).close();
    }



    /** 🧪 Test 2: Validación de la Indexación y Búsqueda Real (E2E del servicio) */
    @Test
    void queAlBuscarUnTerminoEncuentreSoloLasPublicacionesRelevantes() throws Exception {
        // Preparación de datos
        Publicacion p1 = new Publicacion();
        p1.setId(10L);
        p1.setDescripcion("Clase de React y Node JS en vivo");
        p1.setEsPublicidad(false);

        Publicacion p2 = new Publicacion();
        p2.setId(20L);
        p2.setDescripcion("Programacion orientada a objetos Java");
        p2.setEsPublicidad(false);

        Publicacion p3 = new Publicacion();
        p3.setId(30L);
        p3.setDescripcion("Curso de marketing digital");
        p3.setEsPublicidad(false);

        // Ejecución de la indexación
        List<Publicacion> publicaciones = List.of(p1, p2, p3);
        luceneService.indexarPublicaciones(publicaciones);

        // Búsqueda de un término que coincide con p1 y p2
        String terminoBusqueda = "programacion JS";
        List<String> resultados = luceneService.buscarSimilares(terminoBusqueda, 10);

        //  Verificación
        //  IDs de las publicaciones relevantes (p1 y p2)
        assertEquals(2, resultados.size(), "Debería encontrar 2 publicaciones.");
        assertTrue(resultados.contains("10"), "Debe encontrar el ID 10 (React y Node JS).");
        assertTrue(resultados.contains("20"), "Debe encontrar el ID 20 (Programacion Java).");
        assertTrue(!resultados.contains("30"), "No debe encontrar el ID 30 (Marketing).");
    }
}