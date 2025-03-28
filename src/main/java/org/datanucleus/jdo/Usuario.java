package org.datanucleus.jdo;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.*;

@PersistenceCapable
public class Usuario {
    @Persistent private String nombre;
    @Persistent private String apellidos; 
    @Persistent private String email;
    @Persistent private String direccion;
    @Persistent private String telefono;
    @Persistent private TipoUsuario tipoUsuario;   
    @PrimaryKey private String dni;

    public Usuario() {
    }

    public Usuario(String dni, String nombre, String apellidos, String email, String telefono, TipoUsuario tipoUsuario) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.tipoUsuario = tipoUsuario;
    }

    // Getters y Setters
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                '}';
    }


    public String toStringCorto() {
        return "Usuario " + nombre + " con DNI:" + dni;
    }
}
