package es.deusto.spq.server.jdo;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Cine {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent private String nombre;
    @Persistent private String direccion;
    @Persistent(defaultFetchGroup = "true")
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
        result = prime * result + ((direccion == null) ? 0 : direccion.hashCode());
        result = prime * result + ((salas == null) ? 0 : salas.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cine other = (Cine) obj;
        if (id != other.id)
            return false;
        if (nombre == null) {
            if (other.nombre != null)
                return false;
        } else if (!nombre.equals(other.nombre))
            return false;
        if (direccion == null) {
            if (other.direccion != null)
                return false;
        } else if (!direccion.equals(other.direccion))
            return false;
        if (salas == null) {
            if (other.salas != null)
                return false;
        } else if (!salas.equals(other.salas))
            return false;
        return true;
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
