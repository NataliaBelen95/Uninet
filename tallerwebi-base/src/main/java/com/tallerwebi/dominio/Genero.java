package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
@Table(name = "genero")
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    public Genero() {}
    public Genero(String nombre) {this.nombre = nombre;    }

    public String getNombre() {return nombre;    }
    public void setNombre(String nombre) {this.nombre = nombre;    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
}
