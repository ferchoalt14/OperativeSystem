package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GestorInstaPlus {

    private static final String RUTA_INSTA_RAIZ = System.getProperty("user.home") + "/MiniWindowsData/INSTA_RAIZ/";
    private static final String RUTA_INSTA_USERS = RUTA_INSTA_RAIZ + "users_insta.ins";

   
    private static final String[][] CUENTAS_POR_DEFECTO = {
            {"FC Barcelona", "fcbarcelona", "M"},
            {"Olivia Rodrigo", "oliviarodrigo", "F"},
            {"Drake", "drake", "M"},
            {"Robert Pattinson", "robertpattinson", "M"}
    };

    private static void asegurarRaiz() {
        File raiz = new File(RUTA_INSTA_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
    }

    public static String rutaCarpetaInsta(String username) {
        return RUTA_INSTA_RAIZ + username + "/";
    }

   
    public static void inicializarSistema() {
        asegurarRaiz();
        asegurarCuentasPorDefecto();
    }

    private static void asegurarCuentasPorDefecto() {
        try {
            List<UsuarioInsta> usuarios = cargarUsuarios();
            boolean modificado = false;
            for (String[] datos : CUENTAS_POR_DEFECTO) {
                boolean existe = false;
                for (UsuarioInsta u : usuarios) {
                    if (u.getUsername().equalsIgnoreCase(datos[1])) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    UsuarioInsta oficial = new UsuarioInsta(datos[0], datos[2].charAt(0), datos[1], "Insta#2024", 21);
                    usuarios.add(oficial);
                    modificado = true;
                }
                
                crearArchivosPersonales(datos[1]);
            }
            if (modificado) {
                guardarUsuarios(usuarios);
            }
        } catch (ArchivoCorruptoException | IOException e) {
            System.out.println("No se pudieron preparar las cuentas por defecto de INSTA+: " + e.getMessage());
        }
    }

    private static boolean esCuentaPorDefecto(String username) {
        for (String[] datos : CUENTAS_POR_DEFECTO) {
            if (datos[1].equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<UsuarioInsta> cargarUsuarios() throws ArchivoCorruptoException {
        asegurarRaiz();
        List<UsuarioInsta> usuarios = new ArrayList<>();
        File file = new File(RUTA_INSTA_USERS);
        if (!file.exists()) {
            return usuarios;
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

    public static UsuarioInsta buscarPorUsername(String username) throws ArchivoCorruptoException {
        for (UsuarioInsta u : cargarUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
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
        seguirCuentasPorDefecto(nuevo.getUsername());
    }

    private static void seguirCuentasPorDefecto(String username) throws ArchivoCorruptoException, IOException {
        if (esCuentaPorDefecto(username)) {
            return; 
        }
        for (String[] datos : CUENTAS_POR_DEFECTO) {
            seguirCuenta(username, datos[1]);
        }
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

   

    @SuppressWarnings("unchecked")
    private static List<String> cargarListaStrings(File archivo) throws ArchivoCorruptoException {
        List<String> lista = new ArrayList<>();
        if (!archivo.exists()) {
            return lista;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof String) {
                        lista.add((String) o);
                    }
                }
            }
        } catch (EOFException e) {
          
        } catch (Exception e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
        return lista;
    }

    private static void guardarListaStrings(File archivo, List<String> lista) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(new ArrayList<Object>(lista));
        }
    }

    
    public static List<String> obtenerFollowing(String username) throws ArchivoCorruptoException {
        return cargarListaStrings(new File(rutaCarpetaInsta(username), "following.ins"));
    }

    
    public static List<String> obtenerFollowers(String username) throws ArchivoCorruptoException {
        return cargarListaStrings(new File(rutaCarpetaInsta(username), "followers.ins"));
    }

    @SuppressWarnings("unchecked")
    private static int contarElementos(File archivo) {
        if (!archivo.exists()) {
            return 0;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                return ((List<?>) obj).size();
            }
        } catch (Exception e) {
           
        }
        return 0;
    }

  
    public static int contarPublicaciones(String username) {
        return contarElementos(new File(rutaCarpetaInsta(username), "insta.ins"));
    }

    /** usernameSeguidor empieza a seguir a usernameSeguido (actualiza ambos archivos). */
    public static void seguirCuenta(String usernameSeguidor, String usernameSeguido)
            throws ArchivoCorruptoException, IOException {
        if (usernameSeguidor.equalsIgnoreCase(usernameSeguido)) {
            return;
        }

        File archivoFollowing = new File(rutaCarpetaInsta(usernameSeguidor), "following.ins");
        List<String> following = cargarListaStrings(archivoFollowing);
        if (!following.contains(usernameSeguido)) {
            following.add(usernameSeguido);
            guardarListaStrings(archivoFollowing, following);
        }

        File archivoFollowers = new File(rutaCarpetaInsta(usernameSeguido), "followers.ins");
        List<String> followers = cargarListaStrings(archivoFollowers);
        if (!followers.contains(usernameSeguidor)) {
            followers.add(usernameSeguidor);
            guardarListaStrings(archivoFollowers, followers);
        }
    }


    public static void dejarDeSeguir(String usernameSeguidor, String usernameSeguido)
            throws ArchivoCorruptoException, IOException {
        File archivoFollowing = new File(rutaCarpetaInsta(usernameSeguidor), "following.ins");
        List<String> following = cargarListaStrings(archivoFollowing);
        if (following.remove(usernameSeguido)) {
            guardarListaStrings(archivoFollowing, following);
        }

        File archivoFollowers = new File(rutaCarpetaInsta(usernameSeguido), "followers.ins");
        List<String> followers = cargarListaStrings(archivoFollowers);
        if (followers.remove(usernameSeguidor)) {
            guardarListaStrings(archivoFollowers, followers);
        }
    }
}