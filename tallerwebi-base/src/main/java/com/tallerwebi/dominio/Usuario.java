package com.tallerwebi.dominio;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String rol;
    private Boolean activo = false;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private Integer dni;

    /*relacion con carrera*/
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;
    //9-10 Belén
    @Column(nullable = true)
    private String direccion;

    @Column(nullable = true)
    private String localidad;

    @Column(nullable = true)
    private String provincia;

    @Column(nullable = true)
    private String codigoPostal;

    @Column(nullable = true)
    private String telefono;

    @Column(nullable = true)
    private String emailPersonal;

    @Column(nullable = true)
    private String fotoPerfil;

    @Column(nullable = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;


    @Column(nullable = true)
    private String codigoConfirmacion;

    @Column(nullable = true)
    private boolean confirmado = false;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genero_id", nullable = true) //El name es lo que conecta ambas tablas Usuario1-NGenero, o sea que es la fk
    private Genero genero;

    /*publicaciones del usuario**/
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)  /*si una publi queda sin padre usuario, se borra la publi*/
    @Fetch(FetchMode.SUBSELECT)
    private List<Publicacion> publicaciones = new ArrayList<>();

    /*publicaciones guardadas del usuario**/
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_publicaciones_guardadas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "publicacion_id")
    )
    private List<Publicacion> publicacionesGuardadas= new ArrayList<>();

    /*relacion con likes*/
    @OneToMany(mappedBy = "usuario")
    private List<Like> likesDados = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comentario> comentarios;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
    public Boolean getActivo() {
        return activo;
    }
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public boolean activo() {
        return activo;
    }

    public void activar() {
        activo = true;
    }

    // getters y setters de los nuevos campos
    public String getCodigoConfirmacion() { return codigoConfirmacion; }
    public void setCodigoConfirmacion(String codigoConfirmacion) { this.codigoConfirmacion = codigoConfirmacion; }

    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getNombre(){
        return nombre;
    }

    public void setApellido(String apellido) {
        this.apellido =apellido;
    }
    public String getApellido(){
        return apellido;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }
    public Integer getDni() {
        return dni;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }
    public Carrera getCarrera() {
        return carrera;
    }

    public List<Like> getLikesDados() {
        return likesDados;
    }

    public void setLikesDados(List<Like> likesDados) {
        this.likesDados = likesDados;
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }

    public void setPublicaciones(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
    }

    public List<Publicacion> getPublicacionesGuardadas() {
        return publicacionesGuardadas;
    }

    public void setPublicacionesGuardadas(List<Publicacion> publicacionesGuardadas) {
        this.publicacionesGuardadas = publicacionesGuardadas;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public String getDireccion() {return direccion; }
    public void setDireccion(String direccion) {this.direccion = direccion;    }

    public String getLocalidad() {return localidad;    }
    public void setLocalidad(String localidad) {this.localidad = localidad;    }

    public String getProvincia() {return provincia;    }
    public void setProvincia(String provincia) {this.provincia = provincia;    }

    public String getCodigoPostal() {return codigoPostal;    }
    public void setCodigoPostal(String codigoPostal) {this.codigoPostal = codigoPostal;    }

    public String getTelefono() {return telefono;    }
    public void setTelefono(String telefono) {this.telefono = telefono;    }

    public String getEmailPersonal() {return emailPersonal;    }
    public void setEmailPersonal(String emailPersonal) {this.emailPersonal = emailPersonal;    }

    public Genero getGenero() {return genero;   }
    public void setGenero(Genero genero) {this.genero = genero;    }

    public String getFotoPerfil() {return fotoPerfil;    }
    public void setFotoPerfil(String fotoPerfil) {this.fotoPerfil = fotoPerfil;}

    public LocalDate getFechaNacimiento() {return fechaNacimiento;    }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {this.fechaNacimiento = fechaNacimiento;    }


}



