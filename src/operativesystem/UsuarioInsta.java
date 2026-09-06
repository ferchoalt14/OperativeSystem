package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UsuarioInsta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private char genero;
    private String username;
    private String password;
    private int edad;
    private long fechaRegistro;
    private boolean activa;
    private String rutaFotoPerfil;

    public UsuarioInsta(String nombreCompleto, char genero, String username, String password, int edad) {
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.fechaRegistro = System.currentTimeMillis();
        this.activa = true;
        this.rutaFotoPerfil = "";
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public char getGenero() { return genero; }
    public void setGenero(char genero) { this.genero = genero; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public long getFechaRegistro() { return fechaRegistro; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getRutaFotoPerfil() { return rutaFotoPerfil; }
    public void setRutaFotoPerfil(String rutaFotoPerfil) {
        this.rutaFotoPerfil = rutaFotoPerfil != null ? rutaFotoPerfil : "";
    }

    public String getFechaRegistroTexto() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(this.fechaRegistro));
    }
}
