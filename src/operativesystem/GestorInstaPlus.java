package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GestorInstaPlus {

    private static final String RUTA_INSTA_RAIZ = "Z:/INSTA_RAIZ/";
    private static final String RUTA_INSTA_USERS = RUTA_INSTA_RAIZ + "users_insta.ins";

    private static void asegurarRaiz() {
        File raiz = new File(RUTA_INSTA_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
    }

    public static String rutaCarpetaInsta(String username) {
        return RUTA_INSTA_RAIZ + username + "/";
    }

  
    @SuppressWarnings("unchecked")
    public static List<UsuarioInsta> cargarUsuarios() throws ArchivoCorruptoException {
        asegurarRaiz();
        List<UsuarioInsta> usuarios = new ArrayList<>();
        File file = new File(RUTA_INSTA_USERS);
        if (!file.exists()) {
            return usuarios; // aún no existe ninguna cuenta de INSTA+
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            usuarios = (List<UsuarioInsta>) ois.readObject();
        } catch (EOFException e) {
            // archivo vacío recién creado, se ignora
        } catch (Exception e) {
            throw new ArchivoCorruptoException(file.getName(), e);
        }
        return usuarios;
    }

    private static void guardarUsuarios(List<UsuarioInsta> usuarios) throws IOException {
        asegurarRaiz();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_INSTA_USERS))) {
            oos.writeObject(usuarios);
        }
    }

  
    public static UsuarioInsta autenticar(String username, String password)
            throws CuentaDesactivadaException, ArchivoCorruptoException {
        List<UsuarioInsta> usuarios = cargarUsuarios();
        for (UsuarioInsta u : usuarios) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                if (!u.isActiva()) {
                    throw new CuentaDesactivadaException(username);
                }
                return u;
            }
        }
        return null;
    }

   
    public static void registrarUsuario(UsuarioInsta nuevo)
            throws UsernameDuplicadoException, ArchivoCorruptoException, IOException {
        List<UsuarioInsta> usuarios = cargarUsuarios();
        for (UsuarioInsta u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(nuevo.getUsername())) {
                throw new UsernameDuplicadoException(nuevo.getUsername());
            }
        }
        usuarios.add(nuevo);
        guardarUsuarios(usuarios);
        crearArchivosPersonales(nuevo.getUsername());
    }

    
    public static void actualizarUsuario(UsuarioInsta actualizado) throws ArchivoCorruptoException, IOException {
        List<UsuarioInsta> usuarios = cargarUsuarios();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getUsername().equals(actualizado.getUsername())) {
                usuarios.set(i, actualizado);
                break;
            }
        }
        guardarUsuarios(usuarios);
    }

    
    public static void crearArchivosPersonales(String username) throws IOException {
        File carpeta = new File(rutaCarpetaInsta(username));
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        new File(carpeta, "imagenes").mkdirs();
        new File(carpeta, "folders_personales").mkdirs();
        new File(carpeta, "stickers_personales").mkdirs();

        crearArchivoBinarioVacio(new File(carpeta, "following.ins"));
        crearArchivoBinarioVacio(new File(carpeta, "followers.ins"));
        crearArchivoBinarioVacio(new File(carpeta, "insta.ins"));
        crearArchivoBinarioVacio(new File(carpeta, "inbox.ins"));
        crearArchivoBinarioVacio(new File(carpeta, "stickers.ins"));
    }

    private static void crearArchivoBinarioVacio(File archivo) throws IOException {
        if (archivo.exists()) {
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(new ArrayList<Object>());
        }
    }
}