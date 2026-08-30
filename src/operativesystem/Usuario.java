/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    private Date fechaRegistro;   
    private int edad;
    private boolean activa;      
    private String fotoPerfil;    
    private boolean administrador;

    public Usuario(String nombreCompleto, char genero, String username, String password,
                   int edad, String fotoPerfil, boolean administrador) {
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.username = username;
        this.password = password;
        this.fechaRegistro = new Date();
        this.edad = edad;
        this.activa = true;
        this.fotoPerfil = fotoPerfil;
        this.administrador = administrador;
    }

    
    public static Usuario crearAdministradorPorDefecto() {
        return new Usuario(
                "Administrador del Sistema",
                'M',
                "admin",
                "admin123",
                30,
                null,
                true
        );
    }



    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public char getGenero() { return genero; }
    public void setGenero(char genero) { this.genero = genero; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getFechaRegistro() { return fechaRegistro; }

    public String getFechaRegistroTexto() {
        return new SimpleDateFormat("dd/MM/yyyy").format(fechaRegistro);
    }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public boolean isAdministrador() { return administrador; }
    public void setAdministrador(boolean administrador) { this.administrador = administrador; }

    @Override
    public String toString() {
        return username + " (" + nombreCompleto + ")";
    }
}
