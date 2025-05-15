package es.deusto.spq.server.jdo;

public class Mensaje {
    String telefono;
    String mensaje;

    public Mensaje(String telefono, String mensaje) {
        this.telefono = telefono;
        this.mensaje = mensaje;
    }

    public Mensaje() {
        this.telefono = "";
        this.mensaje = "";
    }

    public String getTelefono() {
        return telefono;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "Mensaje: " + mensaje + " para telefono: " + telefono;
    }
}
