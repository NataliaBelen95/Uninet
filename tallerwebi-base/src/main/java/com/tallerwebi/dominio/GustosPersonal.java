package com.tallerwebi.dominio;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class GustosPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Relación con el Usuario
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    // Campos para almacenar los gustos analizados por Gemini

    @Lob // Usar TEXT para almacenar listas de tags o resúmenes
    private String tagsIntereses; // Ej: "Ciencia Ficción, Programación, Deporte, Criptomonedas"

    @Lob
    @Column(nullable = true)
    private String resumenPerfil; // Un breve resumen del perfil del usuario generado por la IA

    @Column(nullable = true)
    private String temaPrincipal;
    @Column
    private LocalDateTime fechaUltimoAnalisis; //


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTagsIntereses() {
        return tagsIntereses;
    }

    public void setTagsIntereses(String tagsIntereses) {
        this.tagsIntereses = tagsIntereses;
    }

    public String getResumenPerfil() {
        return resumenPerfil;
    }

    public void setResumenPerfil(String resumenPerfil) {
        this.resumenPerfil = resumenPerfil;
    }

    public String getTemaPrincipal() {
        return temaPrincipal;
    }

    public void setTemaPrincipal(String temaPrincipal) {
        this.temaPrincipal = temaPrincipal;
    }

    public LocalDateTime getFechaUltimoAnalisis() {
        return fechaUltimoAnalisis;
    }

    public void setFechaUltimoAnalisis(LocalDateTime fechaUltimoAnalisis) {
        this.fechaUltimoAnalisis = fechaUltimoAnalisis;
    }
}
