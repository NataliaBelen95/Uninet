package com.tallerwebi.presentacion.DTO;

public class DatosAmigos {
    private Long id;
    private String nombre;
    private String apellido;
    private String foto;


    public DatosAmigos() {}

    public DatosAmigos(Long id, String nombre, String apellido, String foto) {
        this.id = id;
        this.foto = foto;
        this.nombre = nombre;
        this.apellido = apellido;

    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    // Getters y setters
    public Long getId() { return id; }



}