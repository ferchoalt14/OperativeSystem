package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GestorInstaPlus {

    private static final String RUTA_INSTA_RAIZ = System.getProperty("user.home") + "/MiniWindowsData/INSTA_RAIZ/";
    private static final String RUTA_INSTA_USERS = RUTA_INSTA_RAIZ + "users_insta.ins";

    private static final String INSTA_PASSWORD_DEFECTO = "Insta#2024";

    private static final String[][] CUENTAS_POR_DEFECTO = {
            {"FC Barcelona", "fcbarcelona", "M", "1450000"},
            {"Real Madrid C.F.", "realmadrid", "M", "1520000"},
            {"Olivia Rodrigo", "oliviarodrigo", "F", "980000"},
            {"Drake", "drake", "M", "2100000"},
            {"Robert Pattinson", "robertpattinson", "M", "760000"},
            {"Cristiano Ronaldo", "cristiano", "M", "3200000"},
            {"Lionel Messi", "leomessi", "M", "3100000"},
            {"Taylor Swift", "taylorswift", "F", "2800000"},
            {"Bad Bunny", "badbunny", "M", "2450000"}
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

  
    public static String[][] getCuentasPorDefecto() {
        return CUENTAS_POR_DEFECTO;
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
                UsuarioInsta existente = null;
                for (UsuarioInsta u : usuarios) {
                    if (u.getUsername().equalsIgnoreCase(datos[1])) {
                        existente = u;
                        break;
                    }
                }
                int bonus = Integer.parseInt(datos[3]);
                if (existente == null) {
                    UsuarioInsta oficial = new UsuarioInsta(datos[0], datos[2].charAt(0), datos[1],
                            INSTA_PASSWORD_DEFECTO, 21);
                    oficial.setCuentaOficial(true);
                    oficial.setSeguidoresBonus(bonus);
                    usuarios.add(oficial);
                    modificado = true;
                } else if (!existente.isCuentaOficial() || existente.getSeguidoresBonus() != bonus) {
                    existente.setCuentaOficial(true);
                    existente.setSeguidoresBonus(bonus);
                    modificado = true;
                }

                crearArchivosPersonales(datos[1]);
            }
            if (modificado) {
                guardarUsuarios(usuarios);
            }

         
            for (String[] datosA : CUENTAS_POR_DEFECTO) {
                for (String[] datosB : CUENTAS_POR_DEFECTO) {
                    if (!datosA[1].equalsIgnoreCase(datosB[1])) {
                        seguirCuenta(datosA[1], datosB[1]);
                    }
                }
            }
        } catch (ArchivoCorruptoException | IOException e) {
            System.out.println("No se pudieron preparar las cuentas por defecto de INSTA+: " + e.getMessage());
        }
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

    /** Cantidad de publicaciones reales del usuario (delegado a GestorPosts, que es quien las administra). */
    public static int contarPublicaciones(String username) {
        return GestorPosts.contarPosts(username);
    }

    
    public static int contarFollowersParaMostrar(String username) throws ArchivoCorruptoException {
        int reales = obtenerFollowers(username).size();
        UsuarioInsta u = buscarPorUsername(username);
        int bonus = (u != null) ? u.getSeguidoresBonus() : 0;
        return reales + bonus;
    }

  
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

    
    public static void seguirVarias(String usernameSeguidor, List<String> usernamesASeguir)
            throws ArchivoCorruptoException, IOException {
        for (String usernameSeguido : usernamesASeguir) {
            seguirCuenta(usernameSeguidor, usernameSeguido);
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