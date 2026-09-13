package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class GestorPosts {

    private static File archivoPostsDe(String username) {
        return new File(GestorInstaPlus.rutaCarpetaInsta(username), "insta.ins");
    }

    @SuppressWarnings("unchecked")
    public static List<Post> cargarPostsDeUsuario(String username) throws ArchivoCorruptoException {
        List<Post> posts = new ArrayList<>();
        File archivo = archivoPostsDe(username);
        if (!archivo.exists()) {
            return posts;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof Post) {
                        posts.add((Post) o);
                    }
                }
            }
        } catch (EOFException e) {
            
        } catch (Exception e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
        return posts;
    }

    private static void guardarPostsDeUsuario(String username, List<Post> posts) throws IOException {
        File carpeta = new File(GestorInstaPlus.rutaCarpetaInsta(username));
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivoPostsDe(username)))) {
            oos.writeObject(new ArrayList<Object>(posts));
        }
    }

    
    public static Post crearPost(String username, String texto, String rutaImagen)
            throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(username);
        Post nuevo = new Post(username, texto, rutaImagen);
        posts.add(0, nuevo); // más reciente primero
        guardarPostsDeUsuario(username, posts);
        return nuevo;
    }

    public static boolean eliminarPost(String username, String postId) throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(username);
        boolean eliminado = posts.removeIf(p -> p.getId().equals(postId));
        if (eliminado) {
            guardarPostsDeUsuario(username, posts);
        }
        return eliminado;
    }

    public static int contarPosts(String username) {
        try {
            return cargarPostsDeUsuario(username).size();
        } catch (ArchivoCorruptoException e) {
            return 0;
        }
    }

 
    public static List<Post> obtenerFeed(String username) throws ArchivoCorruptoException {
        List<Post> feed = new ArrayList<>();
        feed.addAll(cargarPostsDeUsuario(username));

        List<String> siguiendo = GestorInstaPlus.obtenerFollowing(username);
        for (String cuenta : siguiendo) {
            feed.addAll(cargarPostsDeUsuario(cuenta));
        }

        feed.sort(Comparator.comparingLong(Post::getFechaPublicacion).reversed());
        return feed;
    }
}