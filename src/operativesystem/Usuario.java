package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private String nombreCompleto;
    private char genero;
    private String username;
    private String password;
    private long fechaRegistro;
    private int edad;
    private boolean activa;
    private String rutaFoto;
    private String rutaWallpaper;

    // Constructor completo usado por PantallaLogin
    public Usuario(String nombreCompleto, char genero, String username, String password, int edad, String rutaFoto, boolean activa) {
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.edad = edad;
        this.fechaRegistro = System.currentTimeMillis();
        this.activa = activa; 
        this.rutaFoto = rutaFoto != null ? rutaFoto : "";
        this.rutaWallpaper = ""; 
    }

    // Constructor básico usado por defecto
    public Usuario(String nombreCompleto, char genero, String username, String password, int edad) {
        this(nombreCompleto, genero, username, password, edad, "", true);
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public char getGenero() { return genero; }
    public void setGenero(char genero) { this.genero = genero; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getFechaRegistro() { return fechaRegistro; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
    
    public String getFotoPerfil() { return rutaFoto; }
    public void setFotoPerfil(String rutaFoto) { this.rutaFoto = rutaFoto; }

    public String getRutaWallpaper() { return rutaWallpaper; }
    public void setRutaWallpaper(String rutaWallpaper) { this.rutaWallpaper = rutaWallpaper; }
    
    public boolean isAdministrador() {
        return "admin".equalsIgnoreCase(this.username);
    }
    
    public String getFechaRegistroTexto() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(this.fechaRegistro));
    }
}