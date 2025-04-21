package es.deusto.spq.server.jdo;
import javax.jdo.annotations.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@PersistenceCapable
public class Pelicula {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent private String titulo;
    @Persistent private String genero;
    @Persistent private int duracion; // en minutos
    @Persistent private Date fechaEstreno;
    @Persistent private String director;
    @Persistent private String sinopsis;
    @Persistent private String horario; // Horarios de proyección
    @Persistent(defaultFetchGroup = "true")
    private Sala sala;
    

    // Constructor vacío
    public Pelicula() {}

    // Constructor con parámetros
    public Pelicula(String titulo, String genero, int duracion, Date fechaEstreno, String director, String sinopsis, String horario, Sala sala) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.director = director;
        this.sinopsis = sinopsis;
        this.horario = horario;
        this.sala = sala;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public Date getFechaEstreno() {
        return fechaEstreno;
    }

    public void setFechaEstreno(Date fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    @Override
    public String toString() {
        return "Pelicula{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", genero='" + genero + '\'' +
                ", duracion=" + duracion +
                ", fechaEstreno=" + fechaEstreno +
                ", director='" + director + '\'' +
                ", sinopsis='" + sinopsis + '\'' +
                ", horario=" + horario +
                ", sala=" + sala +
                '}';
    }

    public String toStringCorto() {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        String fechaFormateada = fechaEstreno != null ? formato.format(fechaEstreno) : "No disponible";

        return "ID: " + id +
                ", título: " + titulo +
                ", género: " + genero +
                ", fecha de estreno: " + fechaFormateada;
    }
}
