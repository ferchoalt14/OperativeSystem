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

 
    void cerrarSesion();
}