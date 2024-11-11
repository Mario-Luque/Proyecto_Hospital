package com.example.proyecto_hospital;

public class historial {
    String id,dni;
    String tipo,descripcion;
    double precio;
    String fecha,hora;

    public historial(String id, String dni, String tipo, String descripcion, double precio, String fecha, String hora) {
        this.id = id;
        this.dni = dni;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.fecha = fecha;
        this.hora = hora;
    }
}
