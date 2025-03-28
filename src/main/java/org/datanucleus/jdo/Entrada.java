package org.datanucleus.jdo;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents an entry for an event.
 */
@PersistenceCapable
public class Entrada {
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
    private long id;

    @Persistent(defaultFetchGroup = "true")
    private Usuario usuario;

    @Persistent(defaultFetchGroup = "true")
    private Cine cine;

    @Persistent private int precio;
    @Persistent private int asiento;
    @Persistent private TipoAsiento tipoAsiento;

    public Entrada() {
        this.usuario = new Usuario();
        this.cine = new Cine();
    }

    public Entrada(Usuario usuario, Cine cine, int precio, int asiento, TipoAsiento tipoAsiento) {
        this.usuario = usuario;
        this.cine = cine;
        this.precio = precio;
        this.asiento = asiento;
        this.tipoAsiento = tipoAsiento;
    }

    public long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Cine getCine() {
        return cine;
    }

    public void setCine(Cine cine) {
        this.cine = cine;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getAsiento() {
        return asiento;
    }

    public void setAsiento(int asiento) {
        this.asiento = asiento;
    }

    public TipoAsiento getTipoAsiento() {
        return tipoAsiento;
    }

    public void setTipoAsiento(TipoAsiento tipoAsiento) {
        this.tipoAsiento = tipoAsiento;
    }

    @Override
    public String toString() {
        return "Entrada " + id +
                " usuario: " + usuario.getNombre() +
                ", cine: " + cine.getNombre() +
                ", asiento: " + asiento +
                ", tipoAsiento: " + tipoAsiento;
    }

    public String toStringCorto() {
        return "Entrada " + id + " para cine -> " + cine.getNombre();
    }
}