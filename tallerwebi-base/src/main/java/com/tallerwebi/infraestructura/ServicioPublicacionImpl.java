package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service("servicioPublicado")
@Transactional
public class ServicioPublicacionImpl implements ServicioPublicacion {

    private final RepositorioPublicacion repositorio;
    private final RepositorioComentario repositorioComentario;

    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioPublicacionImpl(RepositorioPublicacion repositorio, RepositorioComentario repositorioComentario,
                                   RepositorioUsuario repositorioUsuario) {
        this.repositorio = repositorio;
        this.repositorioComentario = repositorioComentario;

        this.repositorioUsuario = repositorioUsuario;
    }

    // ----------------- PUBLICACIÓN CON MULTIPARTFILE (USUARIOS) -----------------
    @Override
    public void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida, IOException {
        validarPublicacion(publicacion, 400, archivo);

        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);
        publicacion.setEsPublicidad(false);

        if (archivo != null && !archivo.isEmpty()) {
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null) throw new PublicacionFallida("El archivo no tiene nombre");

            boolean esPdf = nombreArchivo.toLowerCase().endsWith(".pdf");
            boolean esImagen = nombreArchivo.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$");

            if (!esPdf && !esImagen) {
                throw new PublicacionFallida("Solo se permiten archivos JPG o PDF");
            }

            String tipoContenido = esPdf ? "application/pdf" : archivo.getContentType();

            Path rutaArchivo = Paths.get(System.getProperty("user.dir"), "archivosPublicacion",
                    UUID.randomUUID() + "_" + nombreArchivo);
            Files.createDirectories(rutaArchivo.getParent());
            try (InputStream is = archivo.getInputStream()) {
                Files.copy(is, rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            }

            ArchivoPublicacion archivoPub = new ArchivoPublicacion();
            archivoPub.setNombreArchivo(rutaArchivo.getFileName().toString()); // Solo nombre para URL
            archivoPub.setRutaArchivo(rutaArchivo.toString());                  // Ruta física
            archivoPub.setTipoContenido(tipoContenido);
            archivoPub.setPublicacion(publicacion);

            publicacion.setArchivo(archivoPub);
        }

        actualizarUsuarioYGuardar(publicacion, usuario);
    }

    // ----------------- PUBLICACIÓN CON FILE (PDF INTERNOS) -----------------
    @Override
    public void realizar(Publicacion publicacion, Usuario usuario, File archivo) throws PublicacionFallida, IOException {
        validarPublicacion(publicacion, 400, archivo);

        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);
        publicacion.setEsPublicidad(false);

        if (archivo != null && archivo.exists() && archivo.length() > 0) {
            String nombreOriginal = archivo.getName();
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                throw new PublicacionFallida("Solo se permiten archivos PDF generados automáticamente");
            }

            String tipoContenido = "application/pdf";


            String nombreArchivoRelativo = UUID.randomUUID() + "_" + archivo.getName();

            Path rutaArchivo = Paths.get(System.getProperty("user.dir"), "archivosPublicacion", nombreArchivoRelativo);
            Files.createDirectories(rutaArchivo.getParent());
            try (InputStream is = Files.newInputStream(archivo.toPath())) {
                Files.copy(is, rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            }

            ArchivoPublicacion archivoPub = new ArchivoPublicacion();
            archivoPub.setNombreArchivo(nombreArchivoRelativo); // Solo nombre para URL
            archivoPub.setRutaArchivo(rutaArchivo.toString());
            archivoPub.setTipoContenido("application/pdf");
            archivoPub.setPublicacion(publicacion);

            publicacion.setArchivo(archivoPub);
        }

        actualizarUsuarioYGuardar(publicacion, usuario);
    }

    // ----------------- PUBLICACIÓN DEL BOT -----------------
    @Override
    public void guardarPubliBot(Publicacion publicacion, Usuario usuario, String urlImagen) throws PublicacionFallida {
        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();

        if (descripcionVacia && (urlImagen == null || urlImagen.isEmpty())) {
            throw new PublicacionFallida("La publicación del bot debe tener contenido o imagen.");
        }

        if (!descripcionVacia && publicacion.getDescripcion().length() > 400) {
            throw new PublicacionFallida("Pasaste los 400 caracteres disponibles");
        }

        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);
        publicacion.setEsPublicidad(true);
        publicacion.setUrlImagen(urlImagen);

        actualizarUsuarioYGuardar(publicacion, usuario);
    }

    // ----------------- MÉTODOS DE CONSULTA -----------------
    @Override
    public List<Publicacion> findAll() {
        return repositorio.listarTodas();
    }

    @Override
    public int obtenerCantidadDeLikes(long id) {
        return obtenerPublicacion(id).getLikes();
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
        return repositorio.obtenerPublicacionCompleta(id);
    }

    @Override
    public List<Publicacion> obtenerPorLikeDeUsuario(long id) {
        return repositorio.obtenerPublicacionesConLikeDeUsuario(id);
    }

    @Override
    public List<Publicacion> obtenerPublicacionesDeUsuario(long usuId) {
        return repositorio.obtenerPublicacionesDeUsuario(usuId);
    }

    @Override
    public List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario) {
        return repositorio.obtenerPublicacionesDirigidasA(usuario);
    }

    // ----------------- MÉTODOS PRIVADOS -----------------
    private void validarPublicacion(Publicacion publicacion, int maxCaracteres, MultipartFile archivo) throws PublicacionFallida {
        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();
        boolean sinArchivo = archivo == null || archivo.isEmpty();

        if (descripcionVacia && sinArchivo) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        if (!descripcionVacia && publicacion.getDescripcion().length() > maxCaracteres) {
            throw new PublicacionFallida("Pasaste los " + maxCaracteres + " caracteres disponibles");
        }
    }

    private void validarPublicacion(Publicacion publicacion, int maxCaracteres, File archivo) throws PublicacionFallida {
        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();
        boolean sinArchivo = archivo == null || !archivo.exists();

        if (descripcionVacia && sinArchivo) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        if (!descripcionVacia && publicacion.getDescripcion().length() > maxCaracteres) {
            throw new PublicacionFallida("Pasaste los " + maxCaracteres + " caracteres disponibles");
        }
    }

    private void actualizarUsuarioYGuardar(Publicacion publicacion, Usuario usuario) {
        usuario.setUltimaPublicacion(LocalDate.now());
        repositorioUsuario.actualizar(usuario);
        repositorio.guardar(publicacion);
    }
}
