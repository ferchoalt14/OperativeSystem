/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivosBinarios {


    private static final String CARPETA_RESPALDO =
            System.getProperty("user.home") + File.separator + "MiniWindows_Z";

   
    public static String ROOT_PATH =
            System.getProperty("os.name").toLowerCase().contains("win")
                    ? "Z:" + File.separator
                    : System.getProperty("user.home") + File.separator + "Z" + File.separator;

    private static final boolean ES_WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    private static final Object LOCK = new Object();

    private static String archivoUsuariosPath() {
        return ROOT_PATH + "usuarios.sop";
    }

   
    public static void inicializarSistema() {
        File raiz = new File(ROOT_PATH);

        if (!raiz.exists()) {
            raiz.mkdirs();
        }

        
        if (!raiz.exists() && ES_WINDOWS) {
            montarUnidadZAutomaticamente();
            raiz = new File(ROOT_PATH);
        }

        
        if (!raiz.exists()) {
            ROOT_PATH = CARPETA_RESPALDO + File.separator;
            raiz = new File(ROOT_PATH);
            raiz.mkdirs();
            System.err.println("No se pudo montar Z:\\, usando carpeta local: " + ROOT_PATH);
        }

        File archivoUsuarios = new File(archivoUsuariosPath());
        if (!archivoUsuarios.exists()) {
            List<Usuario> usuarios = new ArrayList<>();
            usuarios.add(Usuario.crearAdministradorPorDefecto());
            try {
                guardarUsuarios(usuarios);
                crearCarpetasUsuario(Usuario.crearAdministradorPorDefecto().getUsername());
            } catch (IOException e) {
               
                System.err.println("No se pudo inicializar usuarios.sop: " + e.getMessage());
            }
        } else {
            try {
                for (Usuario usuario : cargarUsuarios()) {
                    crearCarpetasUsuario(usuario.getUsername());
                }
            } catch (ArchivoCorruptoException e) {
                System.err.println("No se pudo restaurar la estructura de usuarios: " + e.getMessage());
            }
        }
    }

   
    private static void montarUnidadZAutomaticamente() {
        try {
            File carpetaReal = new File(CARPETA_RESPALDO);
            carpetaReal.mkdirs();

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c", "subst", "Z:", carpetaReal.getAbsolutePath());
            Process proceso = pb.start();
            proceso.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("No se pudo ejecutar 'subst' automáticamente: " + e.getMessage());
        }
    }

    
    public static void guardarUsuarios(List<Usuario> usuarios) throws IOException {
        synchronized (LOCK) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(archivoUsuariosPath()))) {
                oos.writeObject(usuarios);
            }
        }
    }

   
    @SuppressWarnings("unchecked")
    public static List<Usuario> cargarUsuarios() throws ArchivoCorruptoException {
        synchronized (LOCK) {
            String rutaArchivo = archivoUsuariosPath();
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                return new ArrayList<>();
            }
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(archivo))) {
                return (List<Usuario>) ois.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                throw new ArchivoCorruptoException(rutaArchivo, e);
            }
        }
    }

  
    public static void registrarUsuario(Usuario nuevo)
            throws UsernameDuplicadoException, ArchivoCorruptoException, IOException {

        List<Usuario> usuarios = cargarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(nuevo.getUsername())) {
                throw new UsernameDuplicadoException(nuevo.getUsername());
            }
        }

        usuarios.add(nuevo);
        guardarUsuarios(usuarios);
        crearCarpetasUsuario(nuevo.getUsername());
    }

   
    public static void crearCarpetasUsuario(String username) {
        String base = ROOT_PATH + username + File.separator;
        new File(base).mkdirs();
        new File(base + "Mis Documentos").mkdirs();
        new File(base + "Música").mkdirs();
        new File(base + "Mis Imágenes").mkdirs();
    }

    
    public static String rutaCarpetaUsuario(String username) {
        return ROOT_PATH + username + File.separator;
    }

   
    public static Usuario autenticar(String username, String password)
            throws CuentaDesactivadaException, ArchivoCorruptoException {

        List<Usuario> usuarios = cargarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                if (!u.isActiva()) {
                    throw new CuentaDesactivadaException(username);
                }
                return u;
            }
        }
        return null; 
    }

   
    public static void actualizarUsuario(Usuario actualizado)
            throws ArchivoCorruptoException, IOException {

        List<Usuario> usuarios = cargarUsuarios();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getUsername().equals(actualizado.getUsername())) {
                usuarios.set(i, actualizado);
                break;
            }
        }
        guardarUsuarios(usuarios);
    }
}
