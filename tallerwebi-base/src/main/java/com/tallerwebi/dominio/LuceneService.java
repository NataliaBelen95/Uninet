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
        // 🔑 CORRECCIÓN: Si ya indexamos, salimos inmediatamente.
        if (this.indexado) {
            return;
        }

        System.out.println("⚠ LUCENE: Iniciando indexación de " + publicaciones.size() + " publicaciones...");
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (Publicacion p : publicaciones) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(p.getId()), Field.Store.YES));
                if (p.getDescripcion() != null) {
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

        // 1. Limpiar y Preparar la Query de Tags
        // Reemplazamos comas por espacios. Esto convierte "Tag1,Tag2" en "Tag1 Tag2".
        String queryInput = textoUsuario.replace(",", " ");

        QueryParser parser = new QueryParser("descripcion", analyzer);

        // 2. OPTIMIZACIÓN CRÍTICA: Establecer el operador por defecto a OR (O)
        // Esto le dice a Lucene que busque coincidencias con CUALQUIERA de los tags.
        parser.setDefaultOperator(QueryParser.Operator.OR);

        // 3. Ejecutar la búsqueda con el texto limpio y escapado.
        // El escape se asegura de manejar caracteres especiales que la IA podría generar.
        Query query = parser.parse(QueryParser.escape(queryInput));

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
//    public void setIndexado(boolean estado) {
//        // 🔑 Permite que el Bot le diga a Lucene que reinicie el índice.
//        this.indexado = estado;
//    }
}