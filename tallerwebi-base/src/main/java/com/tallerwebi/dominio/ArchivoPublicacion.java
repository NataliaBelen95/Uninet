package com.tallerwebi.dominio;

import javax.persistence.*;
import javax.persistence.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"publicacion_id", "nombreArchivo"})
)
public class ArchivoPublicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    private String rutaArchivo;

    private String tipoContenido;

    @OneToOne
    @JoinColumn(name = "publicacion_id") // aquí se crea la columna en ArchivoPublicacion
    private Publicacion publicacion;

    // Getters y setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public String getNombre() {
        return  nombreArchivo;
    }
}

