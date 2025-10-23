package com.tallerwebi.dominio;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "archivo_id")
    private ArchivoPublicacion archivo;

    @Column(nullable = true, length = 200)
    private String descripcion;


    private LocalDateTime fechaPublicacion;



    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /*relacion con entidad like */
    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<Like> likesDePublicacion = new ArrayList<>();

    /*relacion con entidad comentarios , una publicacion muchos comentarios, comentario a una publi*/
    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Comentario> comentarios;

    // getters y setters


    public Usuario getUsuario() {
        return usuario;
    }


    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLikes() {
        return this.likesDePublicacion.size();
    }


    public List<Like> getLikesDePublicacion() {
        return likesDePublicacion;
    }

    public void setLikesDePublicacion(List<Like> likesDePublicacion) {
        this.likesDePublicacion = likesDePublicacion;
    }

    public List<Comentario> getComentarios() {
        if (comentarios == null) {
            comentarios = new ArrayList<>();
        }
        return comentarios;
    }
    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArchivoPublicacion getArchivo() {
        return archivo;
    }

    public void setArchivo(ArchivoPublicacion archivo) {
        this.archivo = archivo;
    }


}