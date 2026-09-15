package operativesystem;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Unidad de información que viaja por los sockets entre {@link ClienteInsta} y {@link ServidorInsta}.
 * Cada paquete tiene un tipo y un pequeño mapa de datos (todo como texto).
 */
public class PaqueteInsta implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        // ---- Cliente -> Servidor (solicitudes) ----
        CONECTAR, DESCONECTAR,
        ENVIAR_MENSAJE, MARCAR_LEIDOS, ELIMINAR_CHAT,
        COMENTAR, ALTERNAR_LIKE, CREAR_POST, ELIMINAR_POST,

        // ---- Servidor -> Cliente (respuestas a una solicitud) ----
        RESPUESTA_OK, RESPUESTA_ERROR,

        // ---- Servidor -> Cliente (eventos en tiempo real) ----
        MENSAJE_NUEVO, MENSAJES_LEIDOS, CHAT_ELIMINADO,
        COMENTARIO_NUEVO, LIKE_ACTUALIZADO, POST_NUEVO, POST_ELIMINADO, MENCION
    }

    // Claves de datos
    public static final String DE = "de";
    public static final String PARA = "para";
    public static final String TEXTO = "texto";
    public static final String AUTOR_POST = "autorPost";
    public static final String POST_ID = "postId";
    public static final String RUTA_IMAGEN = "rutaImagen";
    public static final String LIKES = "likes";
    public static final String LIKEADO = "likeado";

    private final Tipo tipo;
    private long idSolicitud;
    private final HashMap<String, String> datos = new HashMap<>();

    public PaqueteInsta(Tipo tipo) {
        this.tipo = tipo;
    }

    public PaqueteInsta con(String clave, Object valor) {
        datos.put(clave, valor == null ? null : String.valueOf(valor));
        return this;
    }

    public Tipo getTipo() { return tipo; }

    public long getIdSolicitud() { return idSolicitud; }

    void setIdSolicitud(long idSolicitud) { this.idSolicitud = idSolicitud; }

    public String get(String clave) {
        return datos.get(clave);
    }

    public String getOVacio(String clave) {
        String v = datos.get(clave);
        return v == null ? "" : v;
    }

    public int getInt(String clave) {
        try {
            return Integer.parseInt(getOVacio(clave).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getBool(String clave) {
        return Boolean.parseBoolean(getOVacio(clave));
    }

    public boolean esRespuesta() {
        return tipo == Tipo.RESPUESTA_OK || tipo == Tipo.RESPUESTA_ERROR;
    }

    public boolean esOk() {
        return tipo == Tipo.RESPUESTA_OK;
    }

    /** Texto de error (solo tiene sentido en RESPUESTA_ERROR). */
    public String getError() {
        String t = get(TEXTO);
        return (t == null || t.isBlank()) ? "Ocurrió un error inesperado." : t;
    }

    // ----------------------- Fábricas -----------------------

    public static PaqueteInsta ok(long id) {
        PaqueteInsta p = new PaqueteInsta(Tipo.RESPUESTA_OK);
        p.setIdSolicitud(id);
        return p;
    }

    public static PaqueteInsta error(long id, String mensaje) {
        PaqueteInsta p = new PaqueteInsta(Tipo.RESPUESTA_ERROR).con(TEXTO, mensaje);
        p.setIdSolicitud(id);
        return p;
    }

    public static PaqueteInsta conectar(String usuario) {
        return new PaqueteInsta(Tipo.CONECTAR).con(DE, usuario);
    }

    public static PaqueteInsta mensaje(String para, String texto) {
        return new PaqueteInsta(Tipo.ENVIAR_MENSAJE).con(PARA, para).con(TEXTO, texto);
    }

    public static PaqueteInsta marcarLeidos(String conQuien) {
        return new PaqueteInsta(Tipo.MARCAR_LEIDOS).con(PARA, conQuien);
    }

    public static PaqueteInsta eliminarChat(String conQuien) {
        return new PaqueteInsta(Tipo.ELIMINAR_CHAT).con(PARA, conQuien);
    }

    public static PaqueteInsta comentar(String autorPost, String postId, String texto) {
        return new PaqueteInsta(Tipo.COMENTAR).con(AUTOR_POST, autorPost).con(POST_ID, postId).con(TEXTO, texto);
    }

    public static PaqueteInsta like(String autorPost, String postId) {
        return new PaqueteInsta(Tipo.ALTERNAR_LIKE).con(AUTOR_POST, autorPost).con(POST_ID, postId);
    }

    public static PaqueteInsta crearPost(String texto, String rutaImagen) {
        return new PaqueteInsta(Tipo.CREAR_POST).con(TEXTO, texto).con(RUTA_IMAGEN, rutaImagen);
    }

    public static PaqueteInsta eliminarPost(String postId) {
        return new PaqueteInsta(Tipo.ELIMINAR_POST).con(POST_ID, postId);
    }

    @Override
    public String toString() {
        return "PaqueteInsta{" + tipo + ", id=" + idSolicitud + ", " + datos + "}";
    }
}