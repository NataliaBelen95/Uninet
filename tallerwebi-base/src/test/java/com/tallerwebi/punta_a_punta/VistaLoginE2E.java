package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoUsuario;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.net.URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaLoginE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaLogin vistaLogin;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        //browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();

        context = browser.newContext();
        Page page = context.newPage();
        vistaLogin = new VistaLogin(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaAparecerIsologoUninetEnElNavbar() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeLogin();
        entoncesDeberiaVerIsologoUninetnElNavbar();
    }

    @Test
    void deberiaDarUnErrorAlIntentarIniciarSesionConUnUsuarioQueNoExiste() {
        dadoQueElUsuarioCargaSusDatosDeLoginCon("damian@unlam.edu.ar", "unlam");
        cuandoElUsuarioTocaElBotonDeLogin();
        entoncesDeberiaVerUnMensajeDeError();
    }

    @Test
    void deberiaNavegarAlHomeSiElUsuarioExiste() throws MalformedURLException {
        dadoQueElUsuarioCargaSusDatosDeLoginCon("test@unlam.edu.ar", "test");
        cuandoElUsuarioTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoALaVistaDeHome();
    }
//
//    @Test
//    void deberiaRegistrarUnUsuarioEIniciarSesionExistosamente() throws MalformedURLException {
//        String email = "juan@unlam.edu.ar";
//        String clave = "123456";
//        // falla porqeu el mail sertvice genera codigos nuevos entonces nunca es el mismo
//        final String CODIGO_DE_PRUEBA_FIJO = "12345";
//        dadoQueElUsuarioNavegaALaVistaDeRegistro();
//        dadoQueElUsuarioSeRegistraCon(email, clave);
//        //   llegamos a la vista de validación
//        entoncesDeberiaSerRedirigidoALaVistaDeValidarCodigo();
//        //  Ingresar el código y validar
//        dadoQueElUsuarioValidaElCodigo(email, CODIGO_DE_PRUEBA_FIJO);
//        // login (después de validar el código)
//        entoncesDeberiaSerRedirigidoALaVistaDeLogin();
//        dadoQueElUsuarioCargaSusDatosDeLoginCon(email, clave);
//        cuandoElUsuarioTocaElBotonDeLogin();
//        // redirigidoAHome
//        entoncesDeberiaSerRedirigidoALaVistaDeHome();
//    }

    private void entoncesDeberiaVerIsologoUninetnElNavbar() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeLogin();
        entoncesDeberiaVerElIsologoEnElNavbar();
    }

    private void dadoQueElUsuarioEstaEnLaVistaDeLogin() throws MalformedURLException {
        URL urlLogin = vistaLogin.obtenerURLActual();
        assertThat(urlLogin.getPath(), matchesPattern("^/spring/login(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void cuandoElUsuarioTocaElBotonDeLogin() {
        vistaLogin.darClickEnIniciarSesion();
    }

    private void entoncesDeberiaSerRedirigidoALaVistaDeHome() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/home(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void entoncesDeberiaVerUnMensajeDeError() {
        String texto = vistaLogin.obtenerMensajeDeError();
        assertThat("Error Usuario o clave incorrecta", equalToIgnoringCase(texto));
    }

    private void dadoQueElUsuarioCargaSusDatosDeLoginCon(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
    }

    private void dadoQueElUsuarioNavegaALaVistaDeRegistro() {
        vistaLogin.darClickEnRegistrarse();
    }

    private void dadoQueElUsuarioSeRegistraCon(String email, String clave) {
        VistaNuevoUsuario vistaNuevoUsuario = new VistaNuevoUsuario(context.pages().get(0));

        // 1. Campos de texto y fecha obligatorios
        vistaNuevoUsuario.escribirNombre("Juan");
        vistaNuevoUsuario.escribirApellido("Perez");
        vistaNuevoUsuario.escribirDNI("99999999");
        vistaNuevoUsuario.escribirFechaNacimiento("1990-01-15"); // Fecha requerida

        // 2. Email y clave
        vistaNuevoUsuario.escribirEMAIL(email);
        vistaNuevoUsuario.escribirClave(clave);

        // 3. Claves Foráneas (Basadas en las inserciones de ReiniciarDB.java)
        // Usamos el ID '1' insertado en la DB limpia
        vistaNuevoUsuario.seleccionarDepartamento("1");
        vistaNuevoUsuario.seleccionarCarrera("1");

        // 4. Enviar el formulario
        vistaNuevoUsuario.darClickEnRegistrarme();

        // Nota: El siguiente paso en el test (la aserción)
        // debe esperar la vista de validación de código, NO la de login.
    }
    private void entoncesDeberiaVerElIsologoEnElNavbar() {

        Assertions.assertTrue(vistaLogin.existeIsologoDeUninet(), "El isologo de UNLAM no es visible en el navbar.");
    }
    private void entoncesDeberiaSerRedirigidoALaVistaDeValidarCodigo() throws MalformedURLException {
//esperar la URL que se cargó después del POST, que es /registrarme
        URL url = vistaLogin.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/registrarme(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void dadoQueElUsuarioValidaElCodigo(String email, String codigo) {
        VistaNuevoUsuario vistaValidacion = new VistaNuevoUsuario(context.pages().get(0));
        vistaValidacion.escribirEmailOculto(email);
        vistaValidacion.escribirCodigo(codigo);
        vistaValidacion.darClickEnValidarCodigo();
    }

    private void entoncesDeberiaSerRedirigidoALaVistaDeLogin() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/login(?:;jsessionid=[^/\\s]+)?$"));
    }
}