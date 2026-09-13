package operativesystem;


public interface InstaControlador {

    /** Cambia de LOGIN / REGISTRO / APP en el CardLayout raíz. */
    void mostrarPantallaRaiz(String nombre);

    /** Cambia de sección dentro de la app ya logueada (FEED, BUSCAR, CREAR, PERFIL, etc). */
    void mostrarSeccion(String nombre);

    /** Se llama tras un login o registro exitoso para entrar a la app. */
    void ingresarAlApp(UsuarioInsta usuario);

    /** Usuario de INSTA+ actualmente logueado (o null si no hay sesión). */
    UsuarioInsta getUsuarioActual();

    /** Refresca los datos mostrados en la sección de perfil propio. */
    void refrescarPerfilPropio();

    /** Refresca el feed (front page) con las publicaciones más recientes. */
    void refrescarFeed();

    /** Vuelve a ejecutar la última búsqueda para reflejar cambios de seguimiento. */
    void refrescarBusqueda();

    /** Abre el perfil de otra cuenta; seccionOrigen es a dónde volver con "Volver". */
    void abrirPerfilAjeno(String username, String seccionOrigen);

    /** Cierra la sesión actual de INSTA+ y regresa al login. */
    void cerrarSesion();
}