package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrera;

import java.util.List;

public class DatosUsuario {

    private String nombre;
    private String apellido;
    private Carrera carrera;



    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre= nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido= apellido;
    }

    public Carrera getCarrera() {
        return carrera;
    }


    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }
}


