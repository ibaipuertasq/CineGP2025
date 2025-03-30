package es.deusto.spq.server.jdo;
import javax.jdo.annotations.*;

@PersistenceCapable
public class Asiento {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent private int numero;
    @Persistent private TipoAsiento tipo;
    @Persistent private boolean ocupado;

    public Asiento() {
    }

    public Asiento(long id, int numero, TipoAsiento tipo, boolean ocupado) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.ocupado = ocupado;
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

    @Override
    public String toString() {
        return "Asiento{" +
                "id=" + id +
                ", numero=" + numero +
                ", tipo=" + tipo +
                ", ocupado=" + ocupado +
                '}';
    }

    public String toStringCorto() {
        return "Asiento " + numero + " (" + tipo + ") - " + (ocupado ? "Ocupado" : "Libre");
    }
}
