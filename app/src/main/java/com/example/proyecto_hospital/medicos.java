package com.example.proyecto_hospital;

public class medicos {
    String id;
    String nombres, apellidos;
    double precio;
    String area;
    String descripcion,urlimagen;

    public medicos(String id, String nombres, String apellidos, double precio, String area, String descripcion, String urlimagen) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.precio = precio;
        this.area = area;
        this.descripcion = descripcion;
        this.urlimagen = urlimagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlimagen() {
        return urlimagen;
    }

    public void setUrlimagen(String urlimagen) {
        this.urlimagen = urlimagen;
    }
}
