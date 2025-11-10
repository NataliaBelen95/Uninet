package com.tallerwebi.punta_a_punta;
//playwrigth navegador virtual
import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaMiPerfil;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaMiPerfilE2E {

    static Playwright playwright; //ibrería de automatización de navegadores
    static Browser browser;
    BrowserContext context;
    Page page;
    VistaLogin vistaLogin;
    VistaMiPerfil vistaPerfil;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();
        context = browser.newContext();
        page = context.newPage();
        vistaLogin = new VistaLogin(page);
        vistaPerfil = new VistaMiPerfil(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }


    // TESTS
    @Test
    void deberiaRedirigirALoginSiNoEstaLogueado() throws MalformedURLException {
        vistaPerfil.navegarAMiPerfil();
        URL url = vistaPerfil.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/login(?:;jsessionid=[^/\\s]+)?$"));
    }

    @Test
    void deberiaMostrarElPerfilDelUsuarioLogueado() {
        // 1. LOGIN
        dadoQueElUsuarioEstaLogueadoCon("test@unlam.edu.ar", "test");

        // 2. ESPERAR HOME (Esperamos la redirección tras el login exitoso)
        // Redirige a /home según ControladorLogin.java
        page.waitForURL("**/home", new Page.WaitForURLOptions().setTimeout(60000));

        // 3. NAVEGAR A MI PERFIL
        vistaPerfil.darClickEnMiPerfil();

        // 4. ESPERAR PERFIL (El destino final después del clic)
        page.waitForURL("**/miPerfil");

        // --- VALIDACIONES ---
        String nombre = vistaPerfil.obtenerNombreUsuario();
        Assertions.assertTrue(nombre.toLowerCase().contains("test") || nombre.toLowerCase().contains("admin"));

        // validar título y datos de usuario
        entoncesDeberiaVerTitulo("Test User");
        String carrera = vistaPerfil.obtenerCarreraUsuario();
        Assertions.assertNotNull(carrera);
    }
    @Test
    void deberiaMostrarPublicacionesDelUsuario() {
        // 1. LOGIN
        dadoQueElUsuarioEstaLogueadoCon("test@unlam.edu.ar", "test");

        // espero home
        page.waitForURL("**/home");

        // doy click en miPerfil
        vistaPerfil.darClickEnMiPerfil();

        // espero mi perfil
        page.waitForURL("**/miPerfil");

        // Asegurarse  que el CONTENEDOR de publicaciones exista en el DOM.
        Assertions.assertTrue(vistaPerfil.existeContenedorPublicaciones(),
                "El contenedor de publicaciones no está presente.");

        // verificar si hay publicaciones
        boolean hayPublicaciones = vistaPerfil.hayPublicaciones();

        if (hayPublicaciones) {
            // Se chequea contenido si solo hay publicacion (solo si las hay)
            String textoPublicacion = vistaPerfil.obtenerTextoPrimeraPublicacion();
            Assertions.assertFalse(textoPublicacion.isEmpty(), "La publicación no tiene texto");

            String likes = vistaPerfil.obtenerLikesPrimeraPublicacion();
            Assertions.assertTrue(Integer.parseInt(likes) >= 0, "Cantidad de likes inválida");

            String comentarios = vistaPerfil.obtenerTextoBotonComentariosPrimeraPublicacion();
            Assertions.assertNotNull(comentarios, "El botón de comentarios no debería ser null");
        }
        // Si no hay publicaciones, el test pasa.
    }

    // MÉTODOS AUXILIARES

    private void dadoQueElUsuarioEstaLogueadoCon(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
        vistaLogin.darClickEnIniciarSesion();
    }

    private void entoncesDeberiaVerTitulo(String tituloEsperado) {
        String titulo = vistaPerfil.obtenerTitulo();
        assertThat(tituloEsperado, equalToIgnoringCase(titulo));
    }
}
