package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.Genero;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class DatosUsuario {
    private long id;
    private String nombre;
    private String apellido;
    private Carrera carrera;
    private Genero genero; // para el select
    private String email;
    private String emailPersonal;
    private int dni;  // o int/long según definas
    private String password;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
    private String localidad;
    private String provincia;
    private String codigoPostal;
    private String fotoPerfil;
    private List<DatosPublicacion> dtopublicaciones;
    private List<DatosPublicacion> likesGuardados;
    private int cantidadNotificaciones;
    private String slug;


    public String getFotoPerfil() {return fotoPerfil;}
    public void setFotoPerfil(String fotoPerfil) {this.fotoPerfil = fotoPerfil;}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id ;}

    public List<DatosPublicacion> getDtopublicaciones() {return dtopublicaciones;}
    public void setDtopublicaciones(List<DatosPublicacion> dtopublicaciones) {this.dtopublicaciones = dtopublicaciones;}

    public List<DatosPublicacion> getLikesGuardados() {return likesGuardados;}
    public void setLikesGuardados(List<DatosPublicacion> guardados) {
        this.likesGuardados = guardados;
    }
    public int getCantidadNotificaciones() { return cantidadNotificaciones; }
    public void setCantidadNotificaciones(int cantidadNotificaciones) { this.cantidadNotificaciones = cantidadNotificaciones; }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getEmailPersonal() {
        return emailPersonal;
    }

    public void setEmailPersonal(String emailPersonal) {
        this.emailPersonal = emailPersonal;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}