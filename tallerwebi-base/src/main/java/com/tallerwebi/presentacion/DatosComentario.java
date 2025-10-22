package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class DatosComentario {

    @JsonProperty("texto")
    private String texto;

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("apellidoUsuario")
    private String apellidoUsuario;

    @JsonProperty("fechaComentario")
    private LocalDate fechaComentario;

    // Getters y Setters
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public LocalDate getFechaComentario() {
        return fechaComentario;
    }
    public void setFechaComentario(LocalDate fechaComentario) {}
}
