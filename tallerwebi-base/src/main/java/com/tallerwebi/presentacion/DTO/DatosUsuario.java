package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.Genero;
import com.tallerwebi.dominio.departamento.Departamento;

import java.time.LocalDate;
import java.util.List;

public class DatosUsuario {

    private long id;
    private String nombre;
    private String apellido;
    private Carrera carrera;
    private Departamento departamento;
    private Genero genero;
    private String email;
    private String emailPersonal;
    private int dni;
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
    private String url;
    private LocalDate ultimaFechaPublicacion;

    // NUEVO CAMPO
    private boolean esBot;

    // --- Getters y Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailPersonal() { return emailPersonal; }
    public void setEmailPersonal(String emailPersonal) { this.emailPersonal = emailPersonal; }

    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public List<DatosPublicacion> getDtopublicaciones() { return dtopublicaciones; }
    public void setDtopublicaciones(List<DatosPublicacion> dtopublicaciones) { this.dtopublicaciones = dtopublicaciones; }

    public List<DatosPublicacion> getLikesGuardados() { return likesGuardados; }
    public void setLikesGuardados(List<DatosPublicacion> likesGuardados) { this.likesGuardados = likesGuardados; }

    public int getCantidadNotificaciones() { return cantidadNotificaciones; }
    public void setCantidadNotificaciones(int cantidadNotificaciones) { this.cantidadNotificaciones = cantidadNotificaciones; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public LocalDate getUltimaFechaPublicacion() { return ultimaFechaPublicacion; }
    public void setUltimaFechaPublicacion(LocalDate ultimaFechaPublicacion) { this.ultimaFechaPublicacion = ultimaFechaPublicacion; }

    public String getUrl() { return "/perfil/" + (this.slug != null ? this.slug : ""); }
    public void setUrl(String url) { this.url = url; }

    public boolean isBot() { return esBot; }
    public void setIsBot(boolean esBot) { this.esBot = esBot; }
}
