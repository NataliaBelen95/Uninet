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

    // nuevo campo: ids de usuario (emisor y receptor)
    private Long usuarioEmisorId;
    private Long usuarioReceptorId;

    // campo antiguo para compatibilidad (amistadId / solicitudId)
    private Long amistadId;

    public DatosNotificacion() {}

    // nuevo constructor con campos extra
    public DatosNotificacion(Long id, String mensaje, boolean leida, LocalDateTime fecha,
                             String usuarioEmisor, String url, Long amistadId,
                             Long usuarioEmisorId, Long usuarioReceptorId) {
        this.id = id;
        this.mensaje = mensaje;
        this.leida = leida;
        this.fecha = fecha;
        this.usuarioEmisor = usuarioEmisor;
        this.url = url;
        this.amistadId = amistadId;
        this.usuarioEmisorId = usuarioEmisorId;
        this.usuarioReceptorId = usuarioReceptorId;
    }

    // Getters / setters
    public Long getId() { return id; }
    public String getMensaje() { return mensaje; }
    public boolean isLeida() { return leida; }
    public LocalDateTime getFecha() { return fecha; }
    public String getUsuarioEmisor() { return usuarioEmisor; }
    public String getUrl() { return url; }

    public void setId(Long id) { this.id = id; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setUsuarioEmisor(String usuarioEmisor) { this.usuarioEmisor = usuarioEmisor; }
    public void setUrl(String url) { this.url = url; }

    // campos nuevos
    public Long getUsuarioEmisorId() { return usuarioEmisorId; }
    public void setUsuarioEmisorId(Long usuarioEmisorId) { this.usuarioEmisorId = usuarioEmisorId; }

    public Long getUsuarioReceptorId() { return usuarioReceptorId; }
    public void setUsuarioReceptorId(Long usuarioReceptorId) { this.usuarioReceptorId = usuarioReceptorId; }

    // compatibilidad con nombre antiguo
    public Long getSolicitudId() { return amistadId; }
    public void setSolicitudId(Long solicitudId) { this.amistadId = solicitudId; }
}