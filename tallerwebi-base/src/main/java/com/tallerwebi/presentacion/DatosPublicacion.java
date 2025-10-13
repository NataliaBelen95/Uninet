package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ArchivoPublicacion;
import com.tallerwebi.dominio.Comentario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class DatosPublicacion {
    private String nombreUsuario;
    private String apellidoUsuario;
    private String descripcion;
    private long id;
    private int cantLikes;
    private LocalDateTime fechaPublicacion;
    private List<DatosComentario> comentarios;
    private int cantComentarios;
    private String archivoNombre;  // Nombre del archivo
    private String archivoTipo;    // Tipo del archivo

    // Getters and Setters

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
}
