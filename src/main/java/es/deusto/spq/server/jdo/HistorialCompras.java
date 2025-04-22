package es.deusto.spq.server.jdo;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.Date;

@PersistenceCapable(table = "historialcompras")
public class HistorialCompras {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private Usuario usuario;

    @Persistent
    private Entrada entrada;

    @Persistent
    private Date fechaCompra;

    // Constructor
    public HistorialCompras(Usuario usuario, Entrada entrada, Date fechaCompra) {
        this.usuario = usuario;
        this.entrada = entrada;
        this.fechaCompra = fechaCompra;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    @Override
    public String toString() {
        return "HistorialCompras{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNombreUsuario() : "null") +
                ", entrada=" + (entrada != null ? entrada.getId() : "null") +
                ", fechaCompra=" + fechaCompra +
                '}';
    }
}