package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivosBinarios {
    
    private static final String RUTA_RAIZ = System.getProperty("user.home") + "/MiniWindowsData/";
    private static final String RUTA_USERS = RUTA_RAIZ + "usuarios" + ArchivosInsta.EXTENSION;
    private static final String RUTA_USERS_ANTERIOR = RUTA_RAIZ + "users.ins";

    public static void inicializarSistema() {
        File raiz = new File(RUTA_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
        File file = new File(RUTA_USERS);
        migrarArchivoUsuariosAnterior(file);
        if (!file.exists()) {
            crearAdminPorDefecto();
        } else {
            
            try {
                List<Usuario> usuarios = cargarUsuarios();
                boolean modificado = false;
                for (Usuario u : usuarios) {
                    if (u.getUsername().equalsIgnoreCase("admin") && !u.isActiva()) {
                        u.setActiva(true);
                        modificado = true;
                    }
                }
                if (modificado) guardarUsuarios(usuarios);
            } catch (ArchivoCorruptoException | IOException e) {
                System.out.println("No se pudo verificar la activación del administrador: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Usuario> cargarUsuarios() throws ArchivoCorruptoException {
        List<Usuario> usuarios = new ArrayList<>();
        File file = new File(RUTA_USERS);
        if (!file.exists()) {
            crearAdminPorDefecto();
        }
        try {
            Object obj = ArchivosInsta.leerObjeto(file);
            if (obj instanceof List) {
                usuarios = (List<Usuario>) obj;
            }
        } catch (EOFException e) {
            // archivo vacío: se conserva una lista vacía
        } catch (Exception e) {
            throw new ArchivoCorruptoException(file.getName(), e);
        }
        return usuarios;
    }

    private static void guardarUsuarios(List<Usuario> usuarios) throws IOException {
        ArchivosInsta.guardarObjeto(new File(RUTA_USERS), new ArrayList<Object>(usuarios));
    }

    private static void migrarArchivoUsuariosAnterior(File destino) {
        File anterior = new File(RUTA_USERS_ANTERIOR);
        if (!destino.exists() && anterior.exists()) {
            try {
                java.nio.file.Files.move(anterior.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("No se pudo migrar users.ins a usuarios.sop: " + e.getMessage());
            }
        }
    }

    private static void crearAdminPorDefecto() {
        File raiz = new File(RUTA_RAIZ);
        if (!raiz.exists()) raiz.mkdirs();
        
        List<Usuario> lista = new ArrayList<>();
        Usuario admin = new Usuario("Administrador", 'M', "admin", "admin123", 99, "", true);
        lista.add(admin);
        try {
            guardarUsuarios(lista);
            crearCarpetasUsuario("admin");
        } catch (IOException e) {
            System.out.println("Error al crear admin: " + e.getMessage());
        }
    }

    public static void crearCarpetasUsuario(String username) {
        String base = RUTA_RAIZ + username + "/";
        new File(base + "Mis Documentos").mkdirs();
        new File(base + "Musica").mkdirs();
        new File(base + "Mis Imagenes").mkdirs();
        new File(base + "imagenes").mkdirs();
        new File(base + "folders_personales").mkdirs();
        new File(base + "stickers_personales").mkdirs();
    }
    
    public static String rutaCarpetaUsuario(String username) {
        return RUTA_RAIZ + username;
    }

    public static Usuario autenticar(String username, String password) throws CuentaDesactivadaException, ArchivoCorruptoException {
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

    public static void registrarUsuario(Usuario nuevo) throws UsernameDuplicadoException, ArchivoCorruptoException, IOException {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(nuevo.getUsername())) {
                throw new UsernameDuplicadoException(nuevo.getUsername());
            }
        }
        nuevo.setActiva(true);
        usuarios.add(nuevo);
        guardarUsuarios(usuarios);
        crearCarpetasUsuario(nuevo.getUsername());
    }

    public static void actualizarUsuario(Usuario actualizado) throws ArchivoCorruptoException, IOException {
        List<Usuario> usuarios = cargarUsuarios();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getUsername().equals(actualizado.getUsername())) {
                usuarios.set(i, actualizado);
                break;
            }
        }
        guardarUsuarios(usuarios);
    }

    public static void eliminarUsuario(String username) throws ArchivoCorruptoException, IOException {
        if (username.equalsIgnoreCase("admin")) {
            throw new IOException("No se puede eliminar la cuenta de administrador.");
        }
        
        List<Usuario> usuarios = cargarUsuarios();
        boolean eliminado = usuarios.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        
        if (eliminado) {
            guardarUsuarios(usuarios);
            borrarCarpetaRecursiva(new File(RUTA_RAIZ + username)); 
        }
    }

    private static void borrarCarpetaRecursiva(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    borrarCarpetaRecursiva(hijo);
                }
            }
        }
        archivo.delete();
    }

    public static boolean cambiarPassword(String username, String passActual, String nuevaPass) throws ArchivoCorruptoException, IOException {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getUsername().equals(username) && u.getPassword().equals(passActual)) {
                u.setPassword(nuevaPass);
                guardarUsuarios(usuarios);
                return true;
            }
        }
        return false;
    }
}