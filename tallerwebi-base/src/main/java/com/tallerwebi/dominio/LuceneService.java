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
@Service
public class LuceneService {

    private final LuceneDirectoryManager directoryManager;
    private final Analyzer analyzer = new StandardAnalyzer();
    private boolean indexado = false;

    public LuceneService(LuceneDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    /** 🔹 Punto de entrada: indexa publicaciones */
    public void indexarPublicaciones(List<Publicacion> publicaciones) throws IOException {
        if (indexado) return;
        System.out.println("Indexando " + publicaciones.size() + " publicaciones...");

        try (IndexWriter writer = crearIndexWriter()) {
            for (Publicacion p : publicaciones) {
                writer.addDocument(crearDocumentoDesdePublicacion(p));
            } indexado = true;
            //no es necesario catch : Si algo falla, el writer se cierra automáticamente y la excepción sube limpia.
        }catch (IOException e) {
            System.err.println("Error al indexar publicaciones: " + e.getMessage());
            e.printStackTrace();
        }


    }

    /** 🔍 Busca publicaciones similares según texto */
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

    //  (delegación interna)

    /** Crea el IndexWriter con configuración */
    private IndexWriter crearIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(directoryManager.getDirectory(), config);
    }

    /** Convierte una publicación en un documento Lucene */
    private Document crearDocumentoDesdePublicacion(Publicacion p) {
        Document doc = new Document();
        doc.add(new StringField("id", String.valueOf(p.getId()), Field.Store.YES));
        if (p.getDescripcion() != null) {
            doc.add(new TextField("descripcion", p.getDescripcion(), Field.Store.YES));
        }
        return doc;
    }
    /** Construye una query segura para los tags del usuario */
    //query parser consulta de lucene : descripcion:deporte OR descripcion:salud OR descripcion:energía
    private Query construirQuery(String textoUsuario) throws Exception {
        String queryInput = textoUsuario.replace(",", " ");
        QueryParser parser = new QueryParser("descripcion", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        return parser.parse(QueryParser.escape(queryInput));
    }


//    public void limpiarIndice() throws IOException {
//        directoryManager.clearDirectory();
//        indexado = false;
//    }
//
//    public void reiniciarIndice() throws IOException {
//        directoryManager.resetDirectory();
//        indexado = false;
//    }
//
//    public boolean isIndexado() {
//        return indexado;
//    }
}
