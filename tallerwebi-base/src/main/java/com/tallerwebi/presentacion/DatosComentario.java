package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatosComentario {

    @JsonProperty("texto")
    private String texto;

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("apellidoUsuario")
    private String apellidoUsuario;

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
}
