package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.ServicioMostrarArchivosSubidos;
import com.tallerwebi.dominio.ServicioSubirArchivo;

import com.tallerwebi.dominio.excepcion.NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException;

import com.tallerwebi.dominio.excepcion.NoSePuedeSubirArchivoPorFallaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorSubidaArchivoTest {

    private ServicioSubirArchivo servicioSubirArchivo;
    private ServicioMostrarArchivosSubidos servicioMostrarArchivosSubidos;
    private ControladorSubidaArchivo controladorSubidaArchivo;
    private HttpServletRequest request;
    private HttpSession session;
    private DatosUsuario usuario;
    private MultipartFile archivoMock;

    @BeforeEach
    public void init(){
        servicioSubirArchivo = mock(ServicioSubirArchivo.class);
        servicioMostrarArchivosSubidos = mock(ServicioMostrarArchivosSubidos.class);

        controladorSubidaArchivo = new ControladorSubidaArchivo(servicioSubirArchivo, servicioMostrarArchivosSubidos);
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
        ModelAndView mov = controladorSubidaArchivo.mostrarDatosEnSubirArchivo(request);
//primero testeo la vista
        assertThat(mov.getViewName(),equalTo("subir-archivo"));

        DatosUsuario usuarioMov = (DatosUsuario) mov.getModel().get("usuario");
       //ahora testeo que el nombre del usuario sea el que obtengo
        assertThat(usuarioMov.getNombre(),equalTo(usuario.getNombre()));

    }

    @Test
    public void queSiNoHayUsuarioLogueadoRedirijaAlLogin(){
        when(session.getAttribute("usuarioLogueado")).thenReturn(null);

        ModelAndView mov = controladorSubidaArchivo.mostrarDatosEnSubirArchivo(request);

        assertThat(mov.getViewName(),equalTo("redirect:/login"));
    }

    @Test
    public void queSiElUsuarioCargaUnArchivoEsteSeGuardeCorrectamenteYmuestreMensajeDeExito(){

       when(servicioSubirArchivo.guardarArchivoPdf(archivoMock,usuario)).thenReturn("archivoPrueba.pdf");
       ModelAndView mov = controladorSubidaArchivo.guardarArchivoDeSubirArchivo(usuario, archivoMock);

       assertThat(mov.getViewName(),equalTo("subir-archivo"));

       String mensaje=mov.getModel().get("mensaje").toString();

       assertThat(mensaje,equalTo("Archivo guardado exitosamente: archivoPrueba.pdf"));

    }

    @Test
    public void queSiNoHayArchivoOEsNuloSeMuestreMensajeDeError(){
        when(archivoMock.isEmpty()).thenReturn(true);

        ModelAndView mov = controladorSubidaArchivo.guardarArchivoDeSubirArchivo(usuario,archivoMock);
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("Archivo no encontrado."));
    }

    @Test
    public void queSiNoSepudoCopiarElArchivoEnCarpetaFinalLanceLaExcepcionYmuestreMensaje(){

        when(servicioSubirArchivo.guardarArchivoPdf(archivoMock,usuario)).thenThrow(new NoSePuedeCopiarArchivoDesdeTempACarpetaFinalException());
        ModelAndView mov = controladorSubidaArchivo.guardarArchivoDeSubirArchivo(usuario,archivoMock);

        assertThat(mov.getViewName(),equalTo("subir-archivo"));
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("No se pudo copiar el archivo desde carpeta temporal a la carpeta final."));

    }

    @Test
    public void queSiNoSepudoSubirElArchivoPorFallaLanceLaExcepcionYmuestreMensaje(){

        when(servicioSubirArchivo.guardarArchivoPdf(archivoMock,usuario)).thenThrow(new NoSePuedeSubirArchivoPorFallaException());
        ModelAndView mov = controladorSubidaArchivo.guardarArchivoDeSubirArchivo(usuario,archivoMock);

        assertThat(mov.getViewName(),equalTo("subir-archivo"));
        String mensaje=mov.getModel().get("mensaje").toString();

        assertThat(mensaje,equalTo("No se pudo subir el archivo al sistema.Falla general"));

    }

}
