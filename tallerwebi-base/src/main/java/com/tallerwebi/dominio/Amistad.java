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

    @Enumerated(EnumType.STRING)
    private EstadoAmistad estado;

    private LocalDateTime fechaSolicitud;

    public Amistad() {}

    public Amistad(Usuario solicitante, Usuario solicitado, EstadoAmistad estado) {
        this.solicitante = solicitante;
        this.solicitado = solicitado;
        this.estado = estado;
        this.fechaSolicitud = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public Usuario getSolicitado() { return solicitado; }
    public EstadoAmistad getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }

    public void setEstado(EstadoAmistad estado) { this.estado = estado; }

}
