package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class GestorInstaPlus {

    private static final String RUTA_INSTA_RAIZ = System.getProperty("user.home") + "/MiniWindowsData/INSTA_RAIZ/";
    private static final String RUTA_INSTA_USERS = RUTA_INSTA_RAIZ + "usuarios.sop";

    
    private static final String INSTA_PASSWORD_DEFECTO = "Insta#2024";

    private static final java.util.Random RANDOM_LIKES = new java.util.Random();

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

   
    // ------------------------------------------------------------------------------------------
    //  Sesión de INSTA+ POR CUENTA DEL SISTEMA OPERATIVO
    //  Cada usuario del mini-SO tiene su propio archivo de sesión, así que si cambias de cuenta
    //  del SO no verás la cuenta de INSTA+ de otra persona.
    // ------------------------------------------------------------------------------------------
    private static final String RUTA_SESIONES = RUTA_INSTA_RAIZ + "sesiones/";

    /** Usuario del SO que abrió INSTA+ por última vez (solo para los métodos de compatibilidad). */
    private static String usuarioSistemaActivo;

    public static synchronized void establecerUsuarioSistema(String usuarioSO) {
        usuarioSistemaActivo = usuarioSO;
    }

    private static File archivoSesion(String usuarioSO) {
        String limpio = usuarioSO.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        return new File(RUTA_SESIONES, limpio + ArchivosInsta.EXTENSION);
    }

    /** Guarda qué cuenta de INSTA+ tiene abierta el usuario del SO indicado. */
    public static synchronized void guardarSesion(String usuarioSO, UsuarioInsta usuario) {
        if (usuarioSO == null || usuarioSO.isBlank() || usuario == null) {
            return;
        }
        File archivo = archivoSesion(usuarioSO);
        archivo.getParentFile().mkdirs();
        try {
            ArchivosInsta.guardarCadena(archivo, usuario.getUsername());
        } catch (IOException e) {
            System.out.println("No se pudo guardar la sesión de INSTA+: " + e.getMessage());
        }
    }

    /** Devuelve la cuenta de INSTA+ que dejó abierta ESTE usuario del SO (o null). */
    public static synchronized UsuarioInsta obtenerSesion(String usuarioSO) {
        if (usuarioSO == null || usuarioSO.isBlank()) {
            return null;
        }
        File archivo = archivoSesion(usuarioSO);
        if (!archivo.exists()) {
            // Migración de compatibilidad con la versión anterior.
            String limpio = usuarioSO.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
            File anterior = new File(RUTA_SESIONES, limpio + ".ses");
            if (anterior.exists()) {
                try {
                    java.nio.file.Files.move(anterior.toPath(), archivo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                    return null;
                }
            } else {
                return null;
            }
        }
        try {
            String username = ArchivosInsta.leerCadena(archivo);
            UsuarioInsta u = buscarPorUsername(username);
            if (u == null) {
                archivo.delete();
            }
            return u;
        } catch (IOException | ArchivoCorruptoException e) {
            return null;
        }
    }

    /** Cierra la sesión de INSTA+ solo para el usuario del SO indicado. */
    public static synchronized void cerrarSesionGuardada(String usuarioSO) {
        if (usuarioSO == null || usuarioSO.isBlank()) {
            return;
        }
        File archivo = archivoSesion(usuarioSO);
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    /** Compatibilidad con código viejo: usa el usuario del SO que abrió INSTA+. */
    public static void guardarSesion(UsuarioInsta usuario) {
        guardarSesion(usuarioSistemaActivo, usuario);
    }

    /** Compatibilidad con código viejo: usa el usuario del SO que abrió INSTA+. */
    public static UsuarioInsta obtenerSesion() {
        return obtenerSesion(usuarioSistemaActivo);
    }

    /** Compatibilidad con código viejo: usa el usuario del SO que abrió INSTA+. */
    public static void cerrarSesionGuardada() {
        cerrarSesionGuardada(usuarioSistemaActivo);
    }

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
        migrarArchivosInstaAntiguos();
        migrarSesionesAntiguas();
        asegurarCuentasPorDefecto();
    }

    /** Migra una sola vez archivos .ins de la estructura anterior a la extensión propia .sop. */
    private static void migrarArchivosInstaAntiguos() {
        File raiz = new File(RUTA_INSTA_RAIZ);
        File usuariosViejos = new File(raiz, "usuarios.ins");
        File usuariosNuevos = new File(RUTA_INSTA_USERS);
        if (!usuariosNuevos.exists() && usuariosViejos.exists()) {
            try {
                java.nio.file.Files.move(usuariosViejos.toPath(), usuariosNuevos.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("No se pudo migrar usuarios.ins: " + e.getMessage());
            }
        }
        File[] carpetas = raiz.listFiles(File::isDirectory);
        if (carpetas != null) {
            for (File carpeta : carpetas) {
                if ("sesiones".equals(carpeta.getName())) {
                    continue;
                }
                for (String base : new String[]{"following", "followers", "insta", "inbox", "stickers", "notificaciones"}) {
                    migrarArchivoAntiguo(carpeta, base);
                }
            }
        }
    }

    private static void migrarSesionesAntiguas() {
        File carpeta = new File(RUTA_SESIONES);
        if (!carpeta.exists()) {
            return;
        }
        File[] antiguas = carpeta.listFiles((dir, nombre) -> nombre.endsWith(".ses"));
        if (antiguas == null) {
            return;
        }
        for (File antiguo : antiguas) {
            String base = antiguo.getName().substring(0, antiguo.getName().length() - 4);
            File nuevo = new File(carpeta, base + ArchivosInsta.EXTENSION);
            if (!nuevo.exists()) {
                try {
                    java.nio.file.Files.move(antiguo.toPath(), nuevo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("No se pudo migrar sesión " + antiguo.getName() + ": " + e.getMessage());
                }
            }
        }
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
                sembrarPostOficialSiHaceFalta(datos[1]);
            }
            if (modificado) {
                guardarUsuarios(usuarios);
            }

            // Las cuentas oficiales se siguen todas entre sí (eso sí queda automático).
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

   
    private static void sembrarPostOficialSiHaceFalta(String username) {
        try {
            if (GestorPosts.contarPosts(username) == 0) {
                String texto = "¡Hola a todos! Bienvenidos a mi cuenta oficial en INSTA+ 💜 Gracias por seguirme.";
                Post post = GestorPosts.crearPost(username, texto, "");
                int likesBase = 8000 + RANDOM_LIKES.nextInt(92000); // entre 8,000 y 100,000
                GestorPosts.establecerLikesBase(username, post.getId(), likesBase);
            }
        } catch (ArchivoCorruptoException | IOException e) {
            System.out.println("No se pudo sembrar el post oficial de " + username + ": " + e.getMessage());
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
        try {
            Object obj = ArchivosInsta.leerObjeto(file);
            if (obj instanceof List) {
                usuarios = (List<UsuarioInsta>) obj;
            }
        } catch (EOFException e) {
            
        } catch (Exception e) {
            throw new ArchivoCorruptoException(file.getName(), e);
        }
        return usuarios;
    }

    private static void guardarUsuarios(List<UsuarioInsta> usuarios) throws IOException {
        asegurarRaiz();
        ArchivosInsta.guardarObjeto(new File(RUTA_INSTA_USERS), new ArrayList<>(usuarios));
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


    public static void actualizarUsuario(UsuarioInsta actualizado, String usernameOriginal)
            throws ArchivoCorruptoException, IOException {
        List<UsuarioInsta> usuarios = cargarUsuarios();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getUsername().equalsIgnoreCase(usernameOriginal)) {
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

        for (String base : new String[]{"following", "followers", "insta", "inbox", "stickers", "notificaciones"}) {
            migrarArchivoAntiguo(carpeta, base);
            crearArchivoBinarioVacio(new File(carpeta, base + ArchivosInsta.EXTENSION));
        }
    }

    private static void migrarArchivoAntiguo(File carpeta, String base) {
        File antiguo = new File(carpeta, base + ".ins");
        File nuevo = new File(carpeta, base + ArchivosInsta.EXTENSION);
        if (!nuevo.exists() && antiguo.exists()) {
            try {
                java.nio.file.Files.move(antiguo.toPath(), nuevo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("No se pudo migrar " + antiguo.getName() + ": " + e.getMessage());
            }
        }
    }

    private static void crearArchivoBinarioVacio(File archivo) throws IOException {
        if (archivo.exists()) {
            return;
        }
        ArchivosInsta.guardarObjeto(archivo, new ArrayList<Object>());
    }

   

    @SuppressWarnings("unchecked")
    private static List<String> cargarListaStrings(File archivo) throws ArchivoCorruptoException {
        List<String> lista = new ArrayList<>();
        if (!archivo.exists()) {
            return lista;
        }
        try {
            Object obj = ArchivosInsta.leerObjeto(archivo);
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
        ArchivosInsta.guardarObjeto(archivo, new ArrayList<Object>(lista));
    }

    
    public static ListaEnlazada<String> obtenerFollowing(String username) throws ArchivoCorruptoException {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String u : cargarListaStrings(new File(rutaCarpetaInsta(username), "following" + ArchivosInsta.EXTENSION))) {
            lista.agregar(u);
        }
        return lista;
    }

    
    public static ListaEnlazada<String> obtenerFollowers(String username) throws ArchivoCorruptoException {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String u : cargarListaStrings(new File(rutaCarpetaInsta(username), "followers" + ArchivosInsta.EXTENSION))) {
            lista.agregar(u);
        }
        return lista;
    }

   
    public static int contarPublicaciones(String username) {
        return GestorPosts.contarPosts(username);
    }

  
    public static int contarFollowersParaMostrar(String username) throws ArchivoCorruptoException {
        int reales = obtenerFollowers(username).tamano();
        UsuarioInsta u = buscarPorUsername(username);
        int bonus = (u != null) ? u.getSeguidoresBonus() : 0;
        return reales + bonus;
    }

    /** usernameSeguidor empieza a seguir a usernameSeguido (actualiza ambos archivos). */
    public static void seguirCuenta(String usernameSeguidor, String usernameSeguido)
            throws ArchivoCorruptoException, IOException {
        if (usernameSeguidor.equalsIgnoreCase(usernameSeguido)) {
            return;
        }

        File archivoFollowing = new File(rutaCarpetaInsta(usernameSeguidor), "following" + ArchivosInsta.EXTENSION);
        List<String> following = cargarListaStrings(archivoFollowing);
        if (!following.contains(usernameSeguido)) {
            following.add(usernameSeguido);
            guardarListaStrings(archivoFollowing, following);
        }

        File archivoFollowers = new File(rutaCarpetaInsta(usernameSeguido), "followers" + ArchivosInsta.EXTENSION);
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
        File archivoFollowing = new File(rutaCarpetaInsta(usernameSeguidor), "following" + ArchivosInsta.EXTENSION);
        List<String> following = cargarListaStrings(archivoFollowing);
        if (following.remove(usernameSeguido)) {
            guardarListaStrings(archivoFollowing, following);
        }

        File archivoFollowers = new File(rutaCarpetaInsta(usernameSeguido), "followers" + ArchivosInsta.EXTENSION);
        List<String> followers = cargarListaStrings(archivoFollowers);
        if (followers.remove(usernameSeguidor)) {
            guardarListaStrings(archivoFollowers, followers);
        }
    }

    // ------------------------------------------------------------------------------------------
    //  Cambio de username: mueve la carpeta y actualiza las referencias en los demás archivos.
    // ------------------------------------------------------------------------------------------
    public static void renombrarUsuario(String viejo, String nuevo) throws ArchivoCorruptoException, IOException {
        if (viejo == null || nuevo == null || viejo.equals(nuevo)) {
            return;
        }
        List<UsuarioInsta> usuarios = cargarUsuarios();

        File carpetaVieja = new File(rutaCarpetaInsta(viejo));
        File carpetaNueva = new File(rutaCarpetaInsta(nuevo));
        if (carpetaVieja.exists() && !carpetaVieja.getAbsolutePath().equals(carpetaNueva.getAbsolutePath())) {
            if (carpetaNueva.exists() && !viejo.equalsIgnoreCase(nuevo)) {
                throw new IOException("Ya existe una carpeta de datos para @" + nuevo + ".");
            }
            try {
                java.nio.file.Files.move(carpetaVieja.toPath(), carpetaNueva.toPath());
            } catch (IOException e) {
                throw new IOException("No se pudo mover la carpeta de datos del usuario.", e);
            }
        }
        crearArchivosPersonales(nuevo);

        // following / followers de todas las cuentas
        for (UsuarioInsta u : usuarios) {
            String dueno = u.getUsername().equalsIgnoreCase(viejo) ? nuevo : u.getUsername();
            for (String nombreArchivo : new String[]{"following" + ArchivosInsta.EXTENSION, "followers" + ArchivosInsta.EXTENSION}) {
                File archivo = new File(rutaCarpetaInsta(dueno), nombreArchivo);
                List<String> lista = cargarListaStrings(archivo);
                boolean cambio = false;
                for (int i = 0; i < lista.size(); i++) {
                    if (lista.get(i).equalsIgnoreCase(viejo)) {
                        lista.set(i, nuevo);
                        cambio = true;
                    }
                }
                if (cambio) {
                    guardarListaStrings(archivo, lista);
                }
            }
        }

        GestorPosts.renombrarUsuarioEnPosts(viejo, nuevo, usuarios);
        GestorMensajes.renombrarUsuarioEnConversaciones(viejo, nuevo);
    }
}