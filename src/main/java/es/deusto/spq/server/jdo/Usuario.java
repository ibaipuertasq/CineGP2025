package es.deusto.spq.server.jdo;
import javax.jdo.annotations.*;

@PersistenceCapable
public class Usuario {
    @Persistent private String nombre;
    @Persistent private String apellidos;
    @Persistent private String email;
    @Persistent private String nombreUsuario;
    @Persistent private String contrasenya;
    @Persistent private String direccion;
    @Persistent private String telefono;
    @Persistent private TipoUsuario tipoUsuario;   
    @PrimaryKey private String dni;

    public Usuario(String dni, String nombre, String apellidos, String email, String nombreUsuario, String contrasenya, String direccion, String telefono, TipoUsuario tipoUsuario) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.nombreUsuario = nombreUsuario;
        this.contrasenya = contrasenya;
        this.direccion = direccion;
        this.telefono = telefono;
        this.tipoUsuario = tipoUsuario;
    }

    public Usuario() {
        this.nombre = "";
        this.apellidos = "";
        this.email = "";
        this.nombreUsuario = "";
        this.contrasenya = "";
        this.direccion = "";
        this.telefono = "";
        this.tipoUsuario = TipoUsuario.CLIENTE;
        this.dni = "";
    }

    public Usuario(String nombreUsuario, String contrasenya) {
        this.nombreUsuario = nombreUsuario;
        this.contrasenya = contrasenya;
    }

    public Usuario(String nombreUsuario){
        this.nombreUsuario = nombreUsuario;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public String getContrasenya() {
        return contrasenya;
    }

    public void setContrasenya(String contrasenya) {
        this.contrasenya = contrasenya;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", telefono='" + telefono + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                '}';
    }

    public String toStringCorto() {
        return "Usuario " + nombre + " con DNI:" + dni + " y nombreUsuario: " + nombreUsuario;
    }
}
