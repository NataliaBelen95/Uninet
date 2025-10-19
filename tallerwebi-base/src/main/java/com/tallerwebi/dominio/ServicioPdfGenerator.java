package com.tallerwebi.dominio;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tallerwebi.infraestructura.FileMultipartFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public interface ServicioPdfGenerator {
    byte[] generarPdfDesdeHtml(String resumenHtml) throws IOException;

    File generarPdfYGuardar(String resumenHtml, String nombreArchivo) throws IOException;

    MultipartFile obtenerArchivoPdf(String nombreArchivoOriginal) throws IOException;

    }


