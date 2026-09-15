package operativesystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TipoNotificacion tipo;
    private final String usernameOrigen;
    private final String mensaje;
    private final String usernameAutorPost;
    private final String postId;
    private final long fecha;
    private boolean leida;

    public Notificacion(TipoNotificacion tipo, String usernameOrigen, String mensaje,
                         String usernameAutorPost, String postId) {
        this.tipo = tipo;
        this.usernameOrigen = usernameOrigen;
        this.mensaje = mensaje;
        this.usernameAutorPost = usernameAutorPost;
        this.postId = postId;
        this.fecha = System.currentTimeMillis();
        this.leida = false;
    }

    public TipoNotificacion getTipo() { return tipo; }
    public String getUsernameOrigen() { return usernameOrigen; }
    public String getMensaje() { return mensaje; }
    public String getUsernameAutorPost() { return usernameAutorPost; }
    public String getPostId() { return postId; }
    public long getFecha() { return fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getFechaTexto() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fecha));
    }
}