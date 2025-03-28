package org.datanucleus.jdo;

import java.util.List;

public class Sala {
    private long id;
    private int numero;
    private int capacidad;
    private List<Asiento> asientos;

    // Constructor por defecto
    public Sala() {
    }

    // Constructor con parámetros
    public Sala(long id, int numero, int capacidad, List<Asiento> asientos) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.asientos = asientos;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public List<Asiento> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<Asiento> asientos) {
        this.asientos = asientos;
    }

    // Método toString
    @Override
    public String toString() {
        return "Sala{" +
                "id=" + id +
                ", numero=" + numero +
                ", capacidad=" + capacidad +
                ", asientos=" + asientos +
                '}';
    }
}
