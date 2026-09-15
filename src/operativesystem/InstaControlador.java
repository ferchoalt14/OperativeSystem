package operativesystem;


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
}