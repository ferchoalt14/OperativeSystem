package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String usernameAutor;
    private final String texto;
    private final long fecha;

    public Comentario(String usernameAutor, String texto) {
        this.usernameAutor = usernameAutor;
        this.texto = texto;
        this.fecha = System.currentTimeMillis();
    }

    public String getUsernameAutor() { return usernameAutor; }
    public String getTexto() { return texto; }
    public long getFecha() { return fecha; }

    public String getFechaTexto() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fecha));
    }
}