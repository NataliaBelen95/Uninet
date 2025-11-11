package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class DatosNotificacion {
    private Long id;
    private String mensaje;
    private boolean leida;
    private String usuarioEmisor;
    private String url;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    // nuevo campo
    private Long amistadId;

    // Constructor vacío (necesario para Jackson)
    public DatosNotificacion() {}

    // Constructor (añadí solicitudId al final)
    public DatosNotificacion(Long id, String mensaje, boolean leida, LocalDateTime fecha, String usuarioEmisor, String url, Long amistadId) {
        this.id = id;
        this.mensaje = mensaje;
        this.leida = leida;
        this.fecha = fecha;
        this.usuarioEmisor = usuarioEmisor;
        this.url = url;
        this.amistadId = amistadId;
    }

    // Getters y setters (añadir getter/setter para solicitudId)
    public Long getId() { return id; }
    public String getMensaje() { return mensaje; }
    public boolean isLeida() { return leida; }
    public LocalDateTime getFecha() { return fecha; }
    public void setId(Long id) { this.id = id; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuarioEmisor() { return usuarioEmisor; }
    public void setUsuarioEmisor(String usuarioEmisor) { this.usuarioEmisor = usuarioEmisor; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    // nuevo getter/setter
    public Long getSolicitudId() { return amistadId; }
    public void setSolicitudId(Long solicitudId) { this.amistadId = solicitudId; }
}