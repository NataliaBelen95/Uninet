package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoSeEncuentraPublicacion;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;  // CORRECTO PARA PRODUCCIÓN


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
    private final RepositorioPublicacion repositorioPublicacion;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioPublicacionImpl(RepositorioPublicacion repositorio, RepositorioComentario repositorioComentario, RepositorioPublicacion repositorioPublicacion
    , RepositorioUsuario repositorioUsuario) {
        this.repositorio = repositorio;
        this.repositorioComentario = repositorioComentario;
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioUsuario = repositorioUsuario;
    }
@Override
    public void realizar(Publicacion publicacion, Usuario usuario, MultipartFile archivo) throws PublicacionFallida {

        // Verificar si la descripción está vacía
        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();
        boolean sinArchivo = archivo == null || archivo.isEmpty(); // Verificar si no se ha subido archivo

        // Si ni la descripción ni el archivo están presentes, lanzar excepción
        if (descripcionVacia && sinArchivo) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        // Verificar que la descripción no exceda los 200 caracteres
        if (!descripcionVacia && publicacion.getDescripcion().length() > 200) {
            throw new PublicacionFallida("Pasaste los 200 caracteres disponibles");
        }

        // Verificar si ya existe una publicación igual
        if (repositorio.existeIgual(publicacion)) {
            throw new PublicacionFallida("Ya existe una publicación igual");
        }

        // Establecer fecha de publicación y usuario
        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);

        publicacion.setEsPublicidad(false);

        // Verificar si ya existe un archivo asociado a esta publicación
        if (publicacion.getArchivo() != null) {
            throw new PublicacionFallida("Ya existe un archivo asociado a esta publicación");
        }

        // Si hay archivo
        if (archivo != null && !archivo.isEmpty()) {
            String tipo = archivo.getContentType();

            // Verificar si el archivo es del tipo permitido (PDF o imágenes)
            if (!tipo.equals("application/pdf") && !tipo.startsWith("image/")) {
                throw new PublicacionFallida("Solo se permiten archivos JPG o PDF");
            }

            // Obtener nombre original del archivo
            String nombreOriginal = archivo.getOriginalFilename();

            // Generar nombre único para el archivo
            String nombreArchivo = UUID.randomUUID() + "_" + nombreOriginal;

            // Ruta donde se guardará el archivo
            Path rutaArchivo = Paths.get(System.getProperty("user.dir"), "archivosPublicacion", nombreArchivo);

            try (InputStream is = archivo.getInputStream()) {
                // Crear directorios si no existen
                Files.createDirectories(rutaArchivo.getParent());
                // Copiar archivo a la ruta especificada
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
        usuario.setUltimaPublicacion(LocalDate.now());
        repositorioUsuario.actualizar(usuario); // 🔒 asegurar persistencia
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
    public void realizar(Publicacion publicacion, Usuario usuario, File archivo) throws PublicacionFallida, IOException {
        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();
        boolean sinArchivo = archivo == null || !archivo.exists();

        if (descripcionVacia && sinArchivo) {
            throw new PublicacionFallida("La publicación debe tener texto o al menos un archivo adjunto");
        }

        if (!descripcionVacia && publicacion.getDescripcion().length() > 200) {
            throw new PublicacionFallida("Pasaste los 200 caracteres disponibles");
        }

        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);
        publicacion.setEsPublicidad(false);
        usuario.setUltimaPublicacion(LocalDate.now());



        if (archivo != null && archivo.exists()) {
            String tipo = Files.probeContentType(archivo.toPath());
            if (tipo == null || !tipo.equals("application/pdf")) {
                throw new PublicacionFallida("Solo se permiten archivos PDF generados automáticamente");
            }

            String nombreOriginal = archivo.getName();
            String nombreArchivo = UUID.randomUUID() + "_" + nombreOriginal;
            Path rutaArchivo = Paths.get(System.getProperty("user.dir"), "archivosPublicacion", nombreArchivo);

            try {
                Files.createDirectories(rutaArchivo.getParent());
                Files.copy(archivo.toPath(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new PublicacionFallida("Error al guardar archivo: " + nombreOriginal);
            }

            ArchivoPublicacion archivoPub = new ArchivoPublicacion();
            archivoPub.setNombreArchivo(nombreArchivo);
            archivoPub.setRutaArchivo(rutaArchivo.toString());
            archivoPub.setTipoContenido(tipo);
            archivoPub.setPublicacion(publicacion);

            publicacion.setArchivo(archivoPub);
        }
        usuario.setUltimaPublicacion(LocalDate.now());
        repositorioUsuario.actualizar(usuario); // 🔒 asegurar persistencia
        repositorio.guardar(publicacion);
    }

    @Override
    public List<Publicacion> obtenerPorLikeDeUsuario(long id) {
        return repositorioPublicacion.obtenerPublicacionesConLikeDeUsuario(id);
    }

    @Override
    public List<Publicacion> obtenerPublicacionesDeUsuario(long usuId) {
        return repositorioPublicacion.obtenerPublicacionesDeUsuario(usuId);
    }

    // 2. 🔑 NUEVO MÉTODO PARA EL BOT (URL de la Imagen)
    @Override
    public void guardarPubliBot(Publicacion publicacion, Usuario usuario, String urlImagen) throws PublicacionFallida {

        boolean descripcionVacia = publicacion.getDescripcion() == null || publicacion.getDescripcion().trim().isEmpty();

        // La publicación debe tener texto o una URL de imagen
        if (descripcionVacia && (urlImagen == null || urlImagen.isEmpty())) {
            throw new PublicacionFallida("La publicación del bot debe tener contenido o imagen.");
        }

        // (Opcional: Verificar que la descripción no exceda los 200 caracteres si aplica al bot)
        if (!descripcionVacia && publicacion.getDescripcion().length() > 200) {
            throw new PublicacionFallida("Pasaste los 200 caracteres disponibles");
        }

        // Establecer datos
        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setUsuario(usuario);
        publicacion.setEsPublicidad(true);

        // ASIGNAR LA URL GENERADA POR LA IA
        publicacion.setUrlImagen(urlImagen);

        // Guardar
        usuario.setUltimaPublicacion(LocalDate.now());
        repositorioUsuario.actualizar(usuario);
        repositorio.guardar(publicacion);
    }
    @Override
    public List<Publicacion> obtenerPublisBotsParaUsuario(Usuario usuario) {
       return repositorioPublicacion.obtenerPublisBotsParaUsuario(usuario);
    }


}
