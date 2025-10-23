package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarMail(String destinatario, String asunto, String texto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatario);
        message.setSubject(asunto);
        message.setText(texto);

        mailSender.send(message);
    }
    public String generarCodigoConfirmacion() {
        int codigo = (int)(Math.random() * 900000) + 100000; // 100000 - 999999
        return String.valueOf(codigo);
    }
}