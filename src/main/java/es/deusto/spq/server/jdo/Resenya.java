package es.deusto.spq.server.jdo;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Resenya {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    @Persistent
    private String comentario;
    @Persistent
    private int puntuacion; // 1-5 estrellas
    @Persistent(defaultFetchGroup = "true")
    private Usuario usuario;
    @Persistent(defaultFetchGroup = "true")
    private Pelicula pelicula;


    // Constructor vacío
    public Resenya() {
    }

    // Constructor con parámetros
    public Resenya(String comentario, int puntuacion, Usuario usuario, Pelicula pelicula) {
        this.comentario = comentario;
        this.puntuacion = puntuacion;
        this.usuario = usuario;
        this.pelicula = pelicula;
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    // toString
    @Override
    public String toString() {
        return "Resenya{" +
                "id=" + id +
                ", comentario='" + comentario + '\'' +
                ", puntuacion=" + puntuacion +
                ", usuario=" + usuario +
                ", pelicula=" + pelicula +
                '}';
    }

    public String toStringCorto() {
        return "Resenya " + id + " -> Pelicula: " + pelicula.getTitulo() + ", Usuario: " + usuario.getNombre() +
               ", Comentario: " + comentario + ", Puntuacion: " + puntuacion;
    }
}
