package com.tallerwebi.dominio;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneService {

    private final Directory directory = new ByteBuffersDirectory();
    private final Analyzer analyzer = new StandardAnalyzer();
    private boolean indexado = false; // bandera para saber si ya indexamos

    /**
     * Indexa una lista de publicaciones en memoria
     */
    public void indexarPublicaciones(List<Publicacion> publicaciones) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (Publicacion p : publicaciones) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(p.getId()), Field.Store.YES));
                if (p.getDescripcion() != null) {
                    doc.add(new TextField("titulo", p.getDescripcion(), Field.Store.YES));
                    doc.add(new TextField("descripcion", p.getDescripcion(), Field.Store.YES));
                }
                writer.addDocument(doc);
            }
        }
        indexado = true;
    }

    /**
     * Busca publicaciones similares en base al texto del perfil del usuario
     */
    public List<String> buscarSimilares(String textoUsuario, int limite) throws Exception {
        if (!indexado) {
            // Si no se ha indexado, devolvemos vacío
            return new ArrayList<>();
        }

        QueryParser parser = new QueryParser("descripcion", analyzer);
        Query query = parser.parse(QueryParser.escape(textoUsuario));

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, limite);

            List<String> resultados = new ArrayList<>();
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                resultados.add(doc.get("id")); // devolvemos los IDs de las publicaciones
            }
            return resultados;
        }
    }

    public Directory getDirectory() {
        return directory;
    }

    public boolean isIndexado() {
        return indexado;
    }
}