package com.tallerwebi.dominio;

import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

@FunctionalInterface
public interface IndexWriterFactory {
    IndexWriter create() throws IOException;
}