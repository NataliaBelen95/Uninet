package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Comentario;

import java.util.ArrayList;
import java.util.List;

public class DatosPublicacion {
    private String nombreUsuario;
    private String apellidoUsuario;
    private String descripcion;
    private long id;
    private int cantLikes;
    private List<DatosComentario> comentarios;


    public List<DatosComentario> getComentarios() {
        return comentarios;
    }
    public void setComentariosDTO(List<DatosComentario> comentarios) {
        this.comentarios = comentarios;
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

    public void setId(long id) {this.id = id;}
    public long getId() {return id;}

    public void setCantLikes (int cantLikes) {
        this.cantLikes = cantLikes;
    }


    public int getCantLikes() {
        return cantLikes;
    }
}

