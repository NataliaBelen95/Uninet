package com.tallerwebi.dominio;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;


/*Propósito: Registrar un evento o acción específica que ocurrió en un momento
dado entre un Usuario y una Publicacion (Like, Comentario, Vista, etc.).

Rol en el Sistema de Recomendación: Actúa como la fuente de datos primarios (crudos) p
ara el análisis. Necesitas saber qué hizo el usuario para poder analizar sus gustos.*/

@Entity
public class Interaccion {

@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private long id;

@ManyToOne
@JoinColumn(name="usuario_id")
private Usuario usuario;

@ManyToOne
@JoinColumn(name="publicacion_id")
private Publicacion publicacion;

private String tipo; // LIKE, COMENTARIO

private LocalDateTime fecha;

private String contenido;

private Double peso;
private boolean vista;
private LocalDateTime fechaVista;

public long getId() {
    return id;
}

public void setId(long id) {
    this.id = id;
}

public Usuario getUsuario() {
    return usuario;
}

public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
}

public Publicacion getPublicacion() {
    return publicacion;
}

public void setPublicacion(Publicacion publicacion) {
    this.publicacion = publicacion;
}

public String getTipo() {
    return tipo;
}

public void setTipo(String tipo) {
    this.tipo = tipo;
}

public LocalDateTime getFecha() {
    return fecha;
}

public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
}

public String getContenido() {
    return contenido;
}

public void setContenido(String contenido) {
    this.contenido = contenido;
}

public Double getPeso() {
    return peso;
}

public void setPeso(Double peso) {
    this.peso = peso;
}

public boolean isVista() {
    return vista;
}

public void setVista(boolean vista) {
    this.vista = vista;
}

public LocalDateTime getFechaVista() {
    return fechaVista;
}

public void setFechaVista(LocalDateTime fechaVista) {
    this.fechaVista = fechaVista;
}

// equals y hashCode para usar Set correctamente
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Interaccion)) return false;
    Interaccion that = (Interaccion) o;
    return Objects.equals(usuario, that.usuario)
            && Objects.equals(publicacion, that.publicacion)
            && Objects.equals(tipo, that.tipo);
}

@Override
public int hashCode() {
    return Objects.hash(usuario, publicacion, tipo);
}
}
