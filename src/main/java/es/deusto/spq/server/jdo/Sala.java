package es.deusto.spq.server.jdo;
import javax.jdo.annotations.*;
import java.util.List;

@PersistenceCapable
public class Sala {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent private int numero;
    @Persistent private int capacidad;
    @Persistent(defaultFetchGroup = "true")
    private List<Asiento> asientos;

    public Sala() {
    }

    public Sala(long id, int numero, int capacidad, List<Asiento> asientos) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.asientos = asientos;
    }

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

    @Override
    public String toString() {
        return "Sala{" +
                "id=" + id +
                ", numero=" + numero +
                ", capacidad=" + capacidad +
                ", asientos=" + asientos +
                '}';
    }

    public String toStringCorto() {
        return "Sala " + numero + " con capacidad de " + capacidad + " asientos.";
    }
}
