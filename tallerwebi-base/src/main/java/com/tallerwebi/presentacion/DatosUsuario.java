package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrera;

import java.util.List;
public class DatosUsuario {
    private String nombre;
    private String apellido;
    private Carrera carrera; // ahora pasamos la entidad completa
    private String email;
    private Long id ;
    private String fotoPerfil;

    public String getFotoPerfil() {return fotoPerfil;}
    public void setFotoPerfil(String fotoPerfil) {this.fotoPerfil = fotoPerfil;}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id ;}
}