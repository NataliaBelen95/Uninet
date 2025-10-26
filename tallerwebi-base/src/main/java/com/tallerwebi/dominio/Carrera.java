package com.tallerwebi.dominio;

import com.tallerwebi.dominio.departamento.Departamento;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "carrera_materia",
            joinColumns = @JoinColumn(name = "carrera_id"),
            inverseJoinColumns = @JoinColumn(name = "materia_id")
    )
    private List<Materia> materias = new ArrayList<>();


    @OneToMany(mappedBy = "carrera")
    private List<Usuario> usuarios= new ArrayList<>();

    //muchas carreras pertenecen a un solo depto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="departamento_id")
    private Departamento departamento;



    public Long getId() {
        return id;
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


    public List<Materia> getMaterias() {
        return materias;
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias;
    }


    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Departamento getDepartamento() {
        return departamento;
    }
    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }
}