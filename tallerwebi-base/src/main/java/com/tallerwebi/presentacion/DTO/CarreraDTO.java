package com.tallerwebi.presentacion.DTO;


import com.tallerwebi.dominio.Carrera;

public class CarreraDTO {
    private Long id;
    private String nombre;

    public CarreraDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public CarreraDTO(Carrera carrera) {
        this.id = carrera.getId();
        this.nombre = carrera.getNombre();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
