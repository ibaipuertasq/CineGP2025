package org.datanucleus.jdo;

public class Asiento {
    private long id;
    private int numero;
    private TipoAsiento tipo;
    private boolean ocupado;

    // Constructor por defecto
    public Asiento() {
    }

    // Constructor con parámetros
    public Asiento(long id, int numero, TipoAsiento tipo, boolean ocupado) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.ocupado = ocupado;
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

    public TipoAsiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAsiento tipo) {
        this.tipo = tipo;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    // Método toString
    @Override
    public String toString() {
        return "Asiento{" +
                "id=" + id +
                ", numero=" + numero +
                ", tipo=" + tipo +
                ", ocupado=" + ocupado +
                '}';
    }
}
