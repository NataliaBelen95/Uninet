package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.presentacion.DatosUsuario;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.util.Streams;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;  // CORRECTO PARA PRODUCCIÓN


import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Service("servicioPublicado")
@Transactional
public class ServicioPublicacionImpl implements ServicioPublicacion {

    private final RepositorioPublicacion repositorio;
    private final RepositorioComentario repositorioComentario;
    private final ServicioUsuario servicioUsuario;
    private final RepositorioPublicacion repositorioPublicacion;

    @Autowired
    public ServicioPublicacionImpl(RepositorioPublicacion repositorio, RepositorioComentario repositorioComentario, RepositorioPublicacion repositorioPublicacion, ServicioUsuario servicioUsuario) {
        this.repositorio = repositorio;
        this.repositorioComentario = repositorioComentario;
        this.repositorioPublicacion = repositorioPublicacion;
        this.servicioUsuario = servicioUsuario;

    }

    public void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida {

        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();
        boolean sinArchivo = archivo == null || archivo.isEmpty();

        if (descripcionVacia && sinArchivo) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        if (!descripcionVacia && publicacion.getDescripcion().length() > 200) {
            throw new PublicacionFallida("Pasaste los 200 caracteres disponibles");
        }

        // No verificar duplicados para permitir subir varias veces lo mismo

        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);

        if (publicacion.getArchivo() != null) {
            throw new PublicacionFallida("Ya existe un archivo asociado a esta publicación");
        }

        if (archivo != null && !archivo.isEmpty()) {
            String tipo = archivo.getContentType();

            if (!tipo.equals("application/pdf") && !tipo.startsWith("image/")) {
                throw new PublicacionFallida("Solo se permiten archivos JPG o PDF");
            }

            String nombreOriginal = archivo.getOriginalFilename();
            String nombreArchivo = UUID.randomUUID() + "_" + nombreOriginal;
            Path rutaArchivo = Paths.get(System.getProperty("user.dir"), "archivosPublicacion", nombreArchivo);

            try (InputStream is = archivo.getInputStream()) {
                Files.createDirectories(rutaArchivo.getParent());
                Files.copy(is, rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new PublicacionFallida("Error al guardar archivo: " + archivo.getOriginalFilename());
            }

            ArchivoPublicacion archivoPub = new ArchivoPublicacion();
            archivoPub.setNombreArchivo(nombreArchivo);
            archivoPub.setRutaArchivo(rutaArchivo.toString());
            archivoPub.setTipoContenido(tipo);
            archivoPub.setPublicacion(publicacion);

            publicacion.setArchivo(archivoPub);
        }

        repositorio.guardar(publicacion);
    }

    @Override
    public List<Publicacion> findAll() {
        return repositorio.listarTodas();
    }

    @Override
    public int obtenerCantidadDeLikes(long id) {
        Publicacion p = obtenerPublicacion(id);
        return p.getLikes();
    }

    @Override
    public void eliminarPublicacionEntera(Publicacion publicacion) {
        if (publicacion != null) {

            repositorio.eliminarPubli(publicacion);
        } else {
            throw new NoSeEncuentraPublicacion();
        }
    }

    @Override
    public List<Comentario> obtenerComentariosDePublicacion(long publicacionId) {
        return repositorioComentario.findComentariosByPublicacionId(publicacionId);
    }

    @Override
    public Publicacion obtenerPublicacion(long id) {
        return repositorioPublicacion.obtenerPublicacionCompleta(id);
    }


    @Override
    public void compartirResumen(DatosUsuario dtoUsuario, String resumen, String nombreArchivo) throws PublicacionFallida {
        // Buscar al usuario
        Usuario usuario = servicioUsuario.buscarPorId(dtoUsuario.getId());

        Publicacion publicacion = new Publicacion();

        // Si hay resumen, lo agregamos a la publicación
        if (resumen != null && !resumen.trim().isEmpty()) {
            publicacion.setDescripcion("📄 Resumen generado por IA:\n\n" + resumen);
        }

        File archivo = null;

        // Si hay un nombre de archivo, buscamos el archivo correspondiente
        if (nombreArchivo != null && !nombreArchivo.isEmpty()) {
            String rutaArchivo = System.getProperty("user.dir") + "/archivos_pdf/" + nombreArchivo;
            archivo = new File(rutaArchivo);

            // Si el archivo no existe, lanzamos una excepción
            if (!archivo.exists()) {
                throw new PublicacionFallida("El archivo PDF original no existe.");
            }
        }

        // Si no hay ni descripción ni archivo, no podemos publicar
        if ((publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty())
                && (archivo == null)) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        // Realizamos la publicación con los datos que tenemos (publicación, usuario y archivo)
        realizar(publicacion, usuario, (MultipartFile) archivo);
    }


}
