package org.datanucleus.jdo;

public class Resenya {
    private long id;
    private String comentario;
    private int puntuacion; // 1-5 estrellas
    private Usuario usuario;
    private Pelicula pelicula;

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
