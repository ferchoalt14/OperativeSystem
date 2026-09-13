package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String usernameAutor;
    private String texto;
    private String rutaImagen;
    private final long fechaPublicacion;
    private int likes;

    public Post(String usernameAutor, String texto, String rutaImagen) {
        this.id = UUID.randomUUID().toString();
        this.usernameAutor = usernameAutor;
        this.texto = texto != null ? texto : "";
        this.rutaImagen = rutaImagen != null ? rutaImagen : "";
        this.fechaPublicacion = System.currentTimeMillis();
        this.likes = 0;
    }

    public String getId() { return id; }
    public String getUsernameAutor() { return usernameAutor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public long getFechaPublicacion() { return fechaPublicacion; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public void agregarLike() { this.likes++; }

    public String getFechaTexto() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fechaPublicacion));
    }
}