package com.tallerwebi.dominio;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LuceneService {

    private final LuceneDirectoryManager directoryManager;
    private final Analyzer analyzer = new StandardAnalyzer();
    private boolean indexado = false;

    // 1. Ya no se inicializa aquí para evitar la recursión.
    private IndexWriterFactory indexWriterFactory;

    public LuceneService(LuceneDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;

        // 2. Se inyecta la implementación por defecto en el constructor.
        this.indexWriterFactory = this::createDefaultIndexWriter;
    }

    /**
     * 🔹 Punto de entrada: indexa publicaciones
     */
    public void indexarPublicaciones(List<Publicacion> publicaciones) throws IOException {
        if (indexado) return;

        // Filtramos solo las publicaciones orgánicas
        List<Publicacion> publicacionesValidas = publicaciones.stream()
                .filter(p -> !p.getEsPublicidad())
                .collect(Collectors.toList());

        System.out.println("Indexando " + publicacionesValidas.size() + " publicaciones no publicitarias...");
        // 3. Usa la factory para crear el IndexWriter.
        try (IndexWriter writer = indexWriterFactory.create()) {
            for (Publicacion p : publicacionesValidas) {
                writer.addDocument(crearDocumentoDesdePublicacion(p));
            }
            indexado = true;
        } catch (IOException e) {
            System.err.println("Error al indexar publicaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 🔍 Busca publicaciones similares según texto
     */
    public List<String> buscarSimilares(String textoUsuario, int limite) throws Exception {
        if (!indexado) return new ArrayList<>();

        Query query = construirQuery(textoUsuario);

        try (DirectoryReader reader = DirectoryReader.open(directoryManager.getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, limite);

            List<String> resultados = new ArrayList<>();
            for (ScoreDoc sd : topDocs.scoreDocs) {
                resultados.add(searcher.doc(sd.doc).get("id"));
            }
            return resultados;
        }
    }

    // --- Implementación del IndexWriter por Defecto (Usada en el constructor) ---

    /**
     * 🔨 Implementación por defecto del IndexWriterFactory
     */
    private IndexWriter createDefaultIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(directoryManager.getDirectory(), config);
    }

    // --- Setter para Inyección/Test ---

    /**
     * 🧪 Permite inyectar una IndexWriterFactory (ej. un mock para pruebas)
     */
    public void setIndexWriterFactory(IndexWriterFactory factory) {
        this.indexWriterFactory = factory;
    }

    // --- Métodos de utilidad ---

    /**
     * Convierte una publicación en un documento Lucene
     */
    private Document crearDocumentoDesdePublicacion(Publicacion p) {
        Document doc = new Document();
        doc.add(new StringField("id", String.valueOf(p.getId()), Field.Store.YES));
        if (p.getDescripcion() != null) {
            doc.add(new TextField("descripcion", p.getDescripcion(), Field.Store.YES));
        }
        return doc;
    }

    /**
     * Construye una query segura para los tags del usuario
     */
    private Query construirQuery(String textoUsuario) throws Exception {
        String queryInput = textoUsuario.replace(",", " ");
        QueryParser parser = new QueryParser("descripcion", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        return parser.parse(QueryParser.escape(queryInput));
    }


    public void setIndexado(boolean indexado) {
        this.indexado = indexado;
    }


    public boolean isIndexado() {
        return indexado;
    }

    public void limpiarIndice() throws IOException {
        directoryManager.clearDirectory();
        indexado = false;
    }

}