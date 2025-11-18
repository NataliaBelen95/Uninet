package com.tallerwebi.presentacion.DTO;

public class DatosLikeNotificacion {
    private String action;
    private int count;

    // Constructor
    public DatosLikeNotificacion(String action, int count) {
        this.action = action;
        this.count = count;
    }

    // Getters y Setters para que Spring pueda serializarlo a JSON
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}