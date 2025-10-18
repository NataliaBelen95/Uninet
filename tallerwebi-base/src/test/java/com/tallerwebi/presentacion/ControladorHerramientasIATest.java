package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.*;

import com.tallerwebi.dominio.excepcion.NoSePudoExtraerElTextoDelPDFException;
import com.tallerwebi.dominio.excepcion.NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;

import com.tallerwebi.dominio.excepcion.NoSePuedeSubirArchivoPorFallaException;
import com.tallerwebi.infraestructura.ServicioSubirResumenAPublicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ControladorHerramientasIATest {

    private ServicioSubirArchivoALaIA servicioSubirArchivoALaIA;
    private ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;
    private ServicioHacerResumen servicioHacerResumen;
    private ControladorHerramientasIA controladorHerramientasIA;
    private HttpServletRequest request;
    private HttpSession session;
    private DatosUsuario usuario;
    private MultipartFile archivoMock;
    private ServicioUsuario servicioUsuario;

    @BeforeEach
    public void init(){
        servicioSubirArchivoALaIA = mock(ServicioSubirArchivoALaIA.class);
        servicioMostrarArchivosSubidos = mock(ServicioMostrarArchivosSubidos.class);
        servicioHacerResumen = mock(ServicioHacerResumen.class);
        controladorHerramientasIA = new ControladorHerramientasIA(servicioSubirArchivoALaIA, servicioMostrarArchivosSubidos,servicioHacerResumen);
        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);


        usuario = new DatosUsuario();
        usuario.setNombre("Test");
        usuario.setApellido("Testeador");
        usuario.setEmail("test.testeador@gmail.com");

        archivoMock = mock(MultipartFile.class);

        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void queAlIngresarALaPaginaElUsuarioEsteLogueado(){

        //simulo que esta en sesion
        when(session.getAttribute("usuarioLogueado")).thenReturn(usuario);

        //ejecuto el metodo
        ModelAndView mov = controladorHerramientasIA.mostrarDatosEnHerramientasIA(request);
//primero testeo la vista
        assertThat(mov.getViewName(),equalTo("herramientas-IA"));

        DatosUsuario usuarioMov = (DatosUsuario) mov.getModel().get("usuario");
       //ahora testeo que el nombre del usuario sea el que obtengo
        assertThat(usuarioMov.getNombre(),equalTo(usuario.getNombre()));

    }

    @Test
    public void queSiNoHayUsuarioLogueadoRedirijaAlLogin(){
        when(session.getAttribute("usuarioLogueado")).thenReturn(null);

        ModelAndView mov = controladorHerramientasIA.mostrarDatosEnHerramientasIA(request);

        assertThat(mov.getViewName(),equalTo("redirect:/login"));
    }

    @Test
    public void queSiElUsuarioCargaUnArchivoEsteSeGuardeCorrectamenteYmuestreMensajeDeExito(){

       when(servicioSubirArchivoALaIA.guardarArchivoPdf(archivoMock,usuario)).thenReturn("archivoPrueba.pdf");
       ModelAndView mov = controladorHerramientasIA.guardarArchivoSubidoEnHerramientasIA(usuario, archivoMock);

       assertThat(mov.getViewName(),equalTo("herramientas-IA"));

       String mensaje=mov.getModel().get("mensaje").toString();

       assertThat(mensaje,equalTo("Archivo guardado exitosamente: archivoPrueba.pdf"));

    }

    @Test
    public void queSiNoHayArchivoOEsNuloSeMuestreMensajeDeError(){
        when(archivoMock.isEmpty()).thenReturn(true);

        ModelAndView mov = controladorHerramientasIA.guardarArchivoSubidoEnHerramientasIA(usuario,archivoMock);
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("Archivo no encontrado."));
    }

    @Test
    public void queSiNoSepudoCopiarElArchivoEnCarpetaFinalLanceLaExcepcionYmuestreMensaje(){

        when(servicioSubirArchivoALaIA.guardarArchivoPdf(archivoMock,usuario)).thenThrow(new NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException());
        ModelAndView mov = controladorHerramientasIA.guardarArchivoSubidoEnHerramientasIA(usuario,archivoMock);

        assertThat(mov.getViewName(),equalTo("herramientas-IA"));
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("No se pudo copiar el archivo desde carpeta temporal a la carpeta final."));

    }

    @Test
    public void queSiNoSepudoSubirElArchivoPorFallaLanceLaExcepcionYmuestreMensaje(){

        when(servicioSubirArchivoALaIA.guardarArchivoPdf(archivoMock,usuario)).thenThrow(new NoSePuedeSubirArchivoPorFallaException());
        ModelAndView mov = controladorHerramientasIA.guardarArchivoSubidoEnHerramientasIA(usuario,archivoMock);

        assertThat(mov.getViewName(),equalTo("herramientas-IA"));
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("No se pudo subir el archivo al sistema.Falla general"));

    }

    @Test
    public void queSiSeSubioUnArchivoSePuedaExtraerElTextoQueContenga(){
        MultipartFile archivoMock = Mockito.mock(MultipartFile.class);

        when(archivoMock.getOriginalFilename()).thenReturn("archivoFalso.pdf");

        ServicioHacerResumen servHacerResumenMock=mock(ServicioHacerResumen.class);

        when(servHacerResumenMock.extraerTexto("archivos_pdf/archivoFalso.pdf")).thenReturn("texto falso");

        String textoExtraido= servHacerResumenMock.extraerTexto("archivos_pdf/archivoFalso.pdf");

        assertThat(textoExtraido,equalTo("texto falso"));
    }
    @Test
    public void queSiNoSePuedeExtraerTextoDelPdfSeLanceLaExcepcion() {

        MultipartFile archivoMock = Mockito.mock(MultipartFile.class);
        when(archivoMock.getOriginalFilename()).thenReturn("archivoPdfConImagen.pdf");

        ServicioHacerResumen servHacerResumenMock = mock(ServicioHacerResumen.class);

        when(servHacerResumenMock.extraerTexto("archivos_pdf/archivoPdfConImagen.pdf"))
                .thenThrow(new NoSePudoExtraerElTextoDelPDFException());

        assertThrows(NoSePudoExtraerElTextoDelPDFException.class, () -> {
            servHacerResumenMock.extraerTexto("archivos_pdf/archivoPdfConImagen.pdf");
        });
    }

}
