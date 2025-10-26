package com.tallerwebi.dominio.departamento;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.Usuario;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Departamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String nombre;

    // Un Depto tiene muchas Carreras, pero cada carrera pertenece a un solo depto
    @OneToMany(mappedBy = "departamento")
    private List<Carrera> carreras = new ArrayList<>();

    //un depto tiene muchos usuarios
    @OneToMany(mappedBy = "departamento")
    private List<Usuario> usuarios = new ArrayList<>();

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }
    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
