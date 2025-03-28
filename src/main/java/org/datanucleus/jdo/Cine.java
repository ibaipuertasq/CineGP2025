package org.datanucleus.jdo;
import java.util.List;

public class Cine {
    private long id;
    private String nombre;
    private String direccion;
    private List<Sala> salas;

    // Constructor por defecto
    public Cine() {
    }

    // Constructor con parámetros
    public Cine(long id, String nombre, String direccion, List<Sala> salas) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.salas = salas;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<Sala> getSalas() {
        return salas;
    }

    public void setSalas(List<Sala> salas) {
        this.salas = salas;
    }

    // toString por defecto
    @Override
    public String toString() {
        return "Cine{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", salas=" + salas +
                '}';
    }

    // toString corto
    public String toShortString() {
        return "Cine{id=" + id + ", nombre='" + nombre + "'}";
    }
}
