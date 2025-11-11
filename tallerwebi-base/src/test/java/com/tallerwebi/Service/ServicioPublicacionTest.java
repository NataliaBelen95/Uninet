package com.tallerwebi.Service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.infraestructura.ServicioPublicacionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class ServicioPublicacionTest {

    private ServicioPublicacionImpl servicioPublicacion;
    private RepositorioPublicacion repositorioPublicacionMock;
    private RepositorioComentario repositorioComentarioMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private File tempFile;

    @BeforeEach
    public void init() {
        repositorioPublicacionMock = mock(RepositorioPublicacion.class);
        repositorioComentarioMock = mock(RepositorioComentario.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioPublicacion = new ServicioPublicacionImpl(repositorioPublicacionMock, repositorioComentarioMock, repositorioUsuarioMock);

    }

    @AfterEach
    public void cleanup() {
        //usar para test file, crear temporal y luego eliminarlo
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void queSePuedaRealizarUnaPublicacionSoloConDescripcion() throws Exception {
        // 1. Datos
        Usuario usuario = crearUsuario("Juan", "Perez", "jp@un", 12456789);
        Publicacion publicacion = crearPublicacion(usuario, false, null); // Fecha null, se setea en el servicio
        publicacion.setDescripcion("Mi primera publicación sin archivo adjunto.");

        // Mockear un archivo , resultado empty
        MultipartFile archivoMock = mock(MultipartFile.class);
        when(archivoMock.isEmpty()).thenReturn(true);

        // 2. Ejecución
        servicioPublicacion.realizar(publicacion, usuario, archivoMock);

        // 3. Verificación
        // a) La publicación se guarda
        verify(repositorioPublicacionMock, times(1)).guardar(eq(publicacion));
        // b) El usuario se actualiza (por la fecha de última publicación)
        verify(repositorioUsuarioMock, times(1)).actualizar(eq(usuario));
        // c) El servicio setea los valores automáticos
        assertNotNull(publicacion.getFechaPublicacion());
        assertEquals(usuario, publicacion.getUsuario());
    }


    @Test
    public void queSePuedaRealizarUnaPublicacionSoloConArchivo() throws Exception {
        // 1. Datos
        Usuario usuario = crearUsuario("Ana", "Gomez", "ag@un", 98765432);
        Publicacion publicacion = crearPublicacion(usuario, false, null);
        publicacion.setDescripcion(null); // Descripción nula (o "")

        // Mockear un archivo de imagen NO vacío
        MultipartFile archivoMock = mock(MultipartFile.class);
        when(archivoMock.isEmpty()).thenReturn(false);
        when(archivoMock.getOriginalFilename()).thenReturn("grafico.png");
        when(archivoMock.getContentType()).thenReturn("image/png");

        // Simular que el archivo tiene contenido para pasar la lógica de copia (necesario)
        when(archivoMock.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));

        // 2. Ejecución
        servicioPublicacion.realizar(publicacion, usuario, archivoMock);

        // 3. Verificación
        // a) La publicación se guarda
        verify(repositorioPublicacionMock, times(1)).guardar(eq(publicacion));
        // b) Se creó y asignó un ArchivoPublicacion
        assertNotNull(publicacion.getArchivo());
        // c) El servicio setea los valores automáticos
        assertNotNull(publicacion.getFechaPublicacion());
    }
    @Test
    public void queFallaSiNoHayDescripcionNiArchivoAdjunto() {
        // 1. Datos
        Usuario usuario = crearUsuario("Luis", "Diaz", "ld@un", 11223344);
        Publicacion publicacion = crearPublicacion(usuario, false, null);
        publicacion.setDescripcion(""); // Descripción vacía

        // mockear archivo , como que esta empty
        MultipartFile archivoMock = mock(MultipartFile.class);
        when(archivoMock.isEmpty()).thenReturn(true);

        // Ejecución y Verificación
        // lanzar  PublicacionFallida al ejecutar el método
        assertThrows(PublicacionFallida.class, () -> {
            servicioPublicacion.realizar(publicacion, usuario, archivoMock);
        }, "Debe lanzar PublicacionFallida si la descripción y el archivo están vacíos.");

        // 3. Verificación adicional: Nada debe ser guardado si la validación falla
        verify(repositorioPublicacionMock, never()).guardar(any());
        verify(repositorioUsuarioMock, never()).actualizar(any());
    }

    @Test
    public void queSePuedaRealizarUnaPublicacionConArchivoPDFDeOrigenInterno_ResumenHechoConIA() throws Exception {

        Usuario usuario = crearUsuario("Pedro", "Reyes", "pr@un", 99887766);
        Publicacion publicacion = crearPublicacion(usuario, false, null);
        publicacion.setDescripcion("Reporte interno.");

        //  archivo PDF temporal real para el mock
        tempFile = File.createTempFile("temp", ".pdf");
        // hacer que el file tenga contenido
        Files.writeString(tempFile.toPath(), "Contenido PDF simulado");

        // Ejecución
        servicioPublicacion.realizar(publicacion, usuario, tempFile);

        // 3. Verificación
        // a) La publicación se guarda
        verify(repositorioPublicacionMock, times(1)).guardar(eq(publicacion));
        // b) El usuario se actualiza
        verify(repositorioUsuarioMock, times(1)).actualizar(eq(usuario));
        // c) Se creó y asignó correctamente el ArchivoPublicacion
        assertNotNull(publicacion.getArchivo());
        assertEquals("application/pdf", publicacion.getArchivo().getTipoContenido());
    }
    @Test
    public void queFallaLaPublicacionDeBotSiExcedeLos400Caracteres() {

        Usuario bot = crearUsuario("Bot", "Publicador", "bot@ia", 00000001);
        bot.setEsBot(true);
        Publicacion publicacion = crearPublicacion(bot, true, null);

        // setear 401 caracteres
        String descripcionLarga = "a".repeat(401);
        publicacion.setDescripcion(descripcionLarga);

        final String urlImagen = "url_valida.jpg";

        // Ejecución y Verificación de exception
        assertThrows(PublicacionFallida.class, () -> {
            servicioPublicacion.guardarPubliBot(publicacion, bot, urlImagen);
        }, "Debe fallar si la descripción del bot supera los 400 caracteres.");

        // verificar que NO SE HAYA GUARDADO.
       // any()	-> Cualquier objeto Publicacion que se intente pasar como argumento.
        //never() -> ser llamado 0 veces
        verify(repositorioPublicacionMock, never()).guardar(any());
    }

    @Test
    public void queUnUsuarioBotPuedaGuardarPublicacionConDescripcionYUrl() throws PublicacionFallida {
        // 1. Datos
        Usuario bot = crearUsuario("Bot", "Publicador", "bot@ia", 00000001);
        bot.setEsBot(true);
        Publicacion publicacion = crearPublicacion(bot, true, null); // Es publicidad = true

        publicacion.setDescripcion("Contenido generado por IA.");
        final String urlImagen = "https://ia.com/imagen_generada.jpg";

        // 2. Ejecución
        servicioPublicacion.guardarPubliBot(publicacion, bot, urlImagen);

        // 3. Verificación
        // a) La publicación se guarda
        verify(repositorioPublicacionMock, times(1)).guardar(eq(publicacion));
        // b) El usuario se actualiza (por la fecha de última publicación)
        verify(repositorioUsuarioMock, times(1)).actualizar(eq(bot));

        // c) El servicio setea los valores automáticos
        assertNotNull(publicacion.getFechaPublicacion());
        assertEquals(true, publicacion.getEsPublicidad());
        assertEquals(urlImagen, publicacion.getUrlImagen());
    }


    private Usuario crearUsuario(String nombre, String apellido, String email, int dni) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setDni(dni);
        u.setEsBot(false);
        u.setPassword("password");
        return u;
    }

    private Publicacion crearPublicacion(Usuario usuario, Boolean esPublicida, LocalDateTime fecha) {
        Publicacion p = new Publicacion();
        p.setUsuario(usuario);
        p.setEsPublicidad(esPublicida);
        p.setFechaPublicacion(fecha);
        return p;
    }

}
