package operativesystem;

import java.util.function.Consumer;

public interface InstaControlador {

    void mostrarPantallaRaiz(String nombre);

    void mostrarSeccion(String nombre);

    void ingresarAlApp(UsuarioInsta usuario);

    void mostrarSeleccionSugeridos(UsuarioInsta usuario);

    UsuarioInsta getUsuarioActual();

    void refrescarPerfilPropio();

    void refrescarFeed();

    void refrescarBusqueda();

    void abrirPerfilAjeno(String username, String seccionOrigen);

    /** Abre (o crea) el chat/DM con "username". */
    void abrirChatConUsuario(String username);

    /** Vuelve a cargar la bandeja de mensajes (lista de conversaciones) y el contador de no leídos. */
    void refrescarMensajes();

    void cerrarSesion();

    // ------------------------- Nuevos -------------------------

    /** Muestra el detalle de una publicación dentro de la app (ya no es un diálogo emergente). */
    void abrirPost(Post post, Runnable alCambiar);

    /** Abre la pantalla de edición del perfil propio. */
    void abrirEditarPerfil();

    /** Avisar cuando se guardan cambios del perfil propio (si cambió el username se reconecta el socket). */
    void usuarioActualizado(String usernameAnterior);

    /** Envía una solicitud al servidor de sockets; la respuesta llega en el hilo de Swing. */
    void enviarAlServidor(PaqueteInsta paquete, Consumer<PaqueteInsta> alResponder);

    /** Muestra una notificación dentro de la ventana de INSTA+ (no es una ventana emergente). */
    void mostrarAviso(String titulo, String texto);

    /** Actualiza el contador de notificaciones no leídas dentro del Inbox. */
    void actualizarNotificacionesInbox(int noLeidas);
}