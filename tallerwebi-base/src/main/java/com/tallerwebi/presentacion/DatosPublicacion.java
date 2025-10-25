package com.tallerwebi.presentacion;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class DatosPublicacion {

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("apellidoUsuario")
    private String apellidoUsuario;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("id")
    private long id;

    @JsonProperty("cantLikes")
    private int cantLikes;

    @JsonProperty("fechaPublicacion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPublicacion;

    @JsonProperty("comentarios")
    private List<DatosComentario> comentarios;

    @JsonProperty("cantComentarios")
    private int cantComentarios;

    @JsonProperty("archivoNombre")
    private String archivoNombre;

    @JsonProperty("archivoTipo")
    private String archivoTipo;

    @JsonProperty("usuarioId")
    private long usuarioId;

    @JsonProperty("dioLike")
    private Boolean dioLike;

    @JsonProperty("esPropio")
    private boolean esPropio;

    @JsonProperty("slugUsuario") // // para perfil de otro usuario perfil/{slug}
    private String slugUsuario;


    // Getters and setters
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public int getCantLikes() {
        return cantLikes;
    }

    public void setCantLikes(int cantLikes) {
        this.cantLikes = cantLikes;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<DatosComentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<DatosComentario> comentarios) {
        this.comentarios = comentarios;
    }

    public int getCantComentarios() {
        return cantComentarios;
    }

    public void setCantComentarios(int cantComentarios) {
        this.cantComentarios = cantComentarios;
    }

    public String getArchivoNombre() {
        return archivoNombre;
    }

    public void setArchivoNombre(String archivoNombre) {
        this.archivoNombre = archivoNombre;
    }

    public String getArchivoTipo() {
        return archivoTipo;
    }

    public void setArchivoTipo(String archivoTipo) {
        this.archivoTipo = archivoTipo;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getDioLike() {return dioLike;}
    public void setDioLike(Boolean yadioLike) {this.dioLike = yadioLike;}

    // --- Getters y setters ---
    public boolean isEsPropio() {
        return esPropio;
    }

    public void setEsPropio(boolean esPropio) {
        this.esPropio = esPropio;
    }

    public String getSlugUsuario() {
        return slugUsuario;
    }

    public void setSlugUsuario(String slugUsuario) {
        this.slugUsuario = slugUsuario;
    }
}
