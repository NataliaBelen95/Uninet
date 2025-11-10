package com.tallerwebi.dominio;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;

@Component
public class LuceneDirectoryManager {

    private Directory directory;

    public LuceneDirectoryManager() {
        //usar disk
        try {
            this.directory = FSDirectory.open(Paths.get("lucene-index"));
            System.out.println("Lucene: Índice persistente en 'lucene-index'");
        } catch (IOException e) {
            e.printStackTrace();
            this.directory = new ByteBuffersDirectory(); // fallback
        }

    }

    public Directory getDirectory() {
        return this.directory;
    }

    public IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig();
        return new IndexWriter(directory, config);
    }

    /**
     * 🔄 Limpia el índice actual sin reemplazar el Directory.
     * Elimina todos los documentos indexados.
     */
    public void clearDirectory() throws IOException {
        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig())) {
            writer.deleteAll();
            writer.commit();
        }
        System.out.println("🧹 Lucene: Índice limpiado (se eliminaron todas las publicaciones).");
    }

    /**
     * 🔁 Reinicia completamente el índice creando un nuevo directorio en memoria.
     */
    public void resetDirectory() throws IOException {
        this.directory.close();
        this.directory = new ByteBuffersDirectory();
        System.out.println("🔄 Lucene: Índice reiniciado en memoria.");
    }

    /**
     * 💾 Cambia el almacenamiento a un índice persistente en disco.
     */
    public void switchToDisk(String path) throws IOException {
        this.directory.close();
        this.directory = FSDirectory.open(Paths.get(path));
        System.out.println("💾 Lucene: Ahora almacenando índice en disco en " + path);
    }

    public void close() throws IOException {
        this.directory.close();
    }


}
