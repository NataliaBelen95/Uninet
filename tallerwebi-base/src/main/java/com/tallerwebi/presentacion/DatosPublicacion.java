package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.Like;
import com.tallerwebi.dominio.Usuario;

import java.util.ArrayList;
import java.util.List;

public class DatosPublicacion {
    private String nombreUsuario;
    private String apellidoUsuario;
    private String descripcion;
    private long id;
    private int cantLikes;
    private List<Comentario> comentarios;


    public List<Comentario> getComentarios() {
        return comentarios;
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

    public void setComentarios(List<Comentario> comentarios) {
        if (this.comentarios == null) {
            this.comentarios = new ArrayList<>();
        }
        this.comentarios.addAll(comentarios);
    }
    public int getCantLikes() {
        return cantLikes;
    }
}

