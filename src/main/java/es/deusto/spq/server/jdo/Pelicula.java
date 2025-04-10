package es.deusto.spq.server.jdo;
import javax.jdo.annotations.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    @Persistent private List<String> horarios; // Horarios de proyección
    @Persistent private Sala sala; // Sala donde se proyecta
    

    // Constructor vacío
    public Pelicula() {}

    // Constructor con parámetros
    public Pelicula(String titulo, String genero, int duracion, Date fechaEstreno, String director, String sinopsis, List<String> horarios, Sala sala) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.director = director;
        this.sinopsis = sinopsis;
        this.horarios = horarios;
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

    public List<String> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<String> horarios) {
        this.horarios = horarios;
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
                ", horarios=" + horarios +
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
