package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "solicitado_id")
    private Usuario solicitado;


    public Amistad() {}

    public Amistad(Usuario solicitante, Usuario solicitado) {
        this.solicitante = solicitante;
        this.solicitado = solicitado;

    }

    // Getters y Setters
    public Long getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public Usuario getSolicitado() { return solicitado; }


    public void setId(Long id) {
        this.id = id;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public void setSolicitado(Usuario solicitado) {
        this.solicitado = solicitado;
    }


}
