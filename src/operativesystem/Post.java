package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Post implements Serializable {

    private static final long serialVersionUID = 2L;

    /** Máximo de caracteres permitidos en el texto de una publicación. */
    public static final int MAX_CARACTERES = 220;

    private static final Pattern PATRON_HASHTAG = Pattern.compile("#(\\w+)");
    private static final Pattern PATRON_MENCION = Pattern.compile("@(\\w+)");

    private final String id;
    private final String usernameAutor;
    private String texto;
    private String rutaImagen;
    private final long fechaPublicacion;

    
    private int likesBase;
    
    private final List<String> likedBy;
    private final List<Comentario> comentarios;

    public Post(String usernameAutor, String texto, String rutaImagen) {
        this.id = UUID.randomUUID().toString();
        this.usernameAutor = usernameAutor;
        this.texto = texto != null ? texto : "";
        this.rutaImagen = rutaImagen != null ? rutaImagen : "";
        this.fechaPublicacion = System.currentTimeMillis();
        this.likesBase = 0;
        this.likedBy = new ArrayList<>();
        this.comentarios = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getUsernameAutor() { return usernameAutor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }

    public long getFechaPublicacion() { return fechaPublicacion; }

    
    public int getLikes() {
        int reales = (likedBy != null) ? likedBy.size() : 0;
        return likesBase + reales;
    }

    public void setLikesBase(int likesBase) { this.likesBase = likesBase; }
    public int getLikesBase() { return likesBase; }

    public boolean estaLikeadoPor(String username) {
        return username != null && likedBy != null && likedBy.contains(username);
    }

    
    public boolean alternarLike(String username) {
        if (username == null) {
            return false;
        }
        if (likedBy.contains(username)) {
            likedBy.remove(username);
            return false;
        } else {
            likedBy.add(username);
            return true;
        }
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void agregarComentario(Comentario comentario) {
        comentarios.add(comentario);
    }

    public String getFechaTexto() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fechaPublicacion));
    }

    /** Devuelve los hashtags (#palabra) detectados en el texto de la publicación, sin el símbolo. */
    public ListaEnlazada<String> getHashtags() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        Matcher m = PATRON_HASHTAG.matcher(texto);
        while (m.find()) {
            String tag = m.group(1);
            if (!lista.contiene(tag)) {
                lista.agregar(tag);
            }
        }
        return lista;
    }

    /** Devuelve las menciones (@usuario) detectadas en el texto de la publicación, sin el símbolo. */
    public ListaEnlazada<String> getMenciones() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        Matcher m = PATRON_MENCION.matcher(texto);
        while (m.find()) {
            String username = m.group(1);
            if (!lista.contiene(username)) {
                lista.agregar(username);
            }
        }
        return lista;
    }
}