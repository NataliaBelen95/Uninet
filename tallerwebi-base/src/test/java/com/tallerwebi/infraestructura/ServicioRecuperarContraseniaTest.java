package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.MailService;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioRecuperarContraseniaTest {


    private MailService mailService;
    private JavaMailSender mailSenderMock;

    @BeforeEach
    public void init() throws Exception {
        mailService = new MailService();

        // Mock del JavaMailSender y lo inyectamos en la instancia mediante reflexión
        mailSenderMock = mock(JavaMailSender.class);
        Field mailSenderField = MailService.class.getDeclaredField("mailSender");
        mailSenderField.setAccessible(true);
        mailSenderField.set(mailService, mailSenderMock);
    }

    @Test
    public void generarCodigoConfirmacion_devuelveSeisDigitosNumericos() {
        String codigo = mailService.generarCodigoConfirmacion();
        assertNotNull(codigo, "El código no debe ser nulo");
        assertEquals(6, codigo.length(), "El código debe tener 6 caracteres");
        // Debe ser parseable a entero y estar en rango 100000..999999
        int valor = Integer.parseInt(codigo);
        assertTrue(valor >= 100000 && valor <= 999999, "El código debe estar entre 100000 y 999999");
    }

    @Test
    public void enviarMail_delegaEnJavaMailSender_conMensajeBienFormado() {
        String destinatario = "usuario@ejemplo.com";
        String asunto = "Asunto de prueba";
        String texto = "Contenido del mensaje";

        // Ejecutar
        mailService.enviarMail(destinatario, asunto, texto);

        // Capturar el mensaje enviado al JavaMailSender
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSenderMock, times(1)).send(captor.capture());

        SimpleMailMessage enviado = captor.getValue();
        assertNotNull(enviado, "Se esperaba un SimpleMailMessage enviado");
        assertArrayEquals(new String[]{destinatario}, enviado.getTo(), "Destinatario incorrecto");
        assertEquals(asunto, enviado.getSubject(), "Asunto incorrecto");
        assertEquals(texto, enviado.getText(), "Texto del mensaje incorrecto");
    }
}