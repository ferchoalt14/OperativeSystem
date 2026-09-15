package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class GestorPosts {

    private static final Random RANDOM_LIKES = new Random();

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

        UsuarioInsta autor = GestorInstaPlus.buscarPorUsername(username);
        if (autor != null && autor.isCuentaOficial()) {
            // Toda publicación de una cuenta oficial arranca con un impulso de likes, no solo la primera.
            int bonus = 500 + RANDOM_LIKES.nextInt(49500); // entre 500 y 50,000
            nuevo.setLikesBase(bonus);
        }

        posts.add(0, nuevo); // más reciente primero
        guardarPostsDeUsuario(username, posts);

        notificarPublicacionNueva(username, nuevo);
        return nuevo;
    }

    /** Genera notificaciones de mención (a quien se etiquetó con @) y de publicación nueva (a los seguidores). */
    private static void notificarPublicacionNueva(String username, Post nuevo) {
        try {
            for (String mencionado : nuevo.getMenciones()) {
                if (!mencionado.equalsIgnoreCase(username) && GestorInstaPlus.buscarPorUsername(mencionado) != null) {
                    GestorNotificaciones.agregarNotificacion(mencionado, TipoNotificacion.MENCION,
                            username, "te mencionó en una publicación", nuevo.getUsernameAutor(), nuevo.getId());
                }
            }
        } catch (ArchivoCorruptoException | IOException ex) {
            
        }
        try {
            for (String seguidor : GestorInstaPlus.obtenerFollowers(username)) {
                GestorNotificaciones.agregarNotificacion(seguidor, TipoNotificacion.PUBLICACION,
                        username, "publicó algo nuevo", nuevo.getUsernameAutor(), nuevo.getId());
            }
        } catch (ArchivoCorruptoException | IOException ex) {
            
        }
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

    public static Post obtenerPostPorId(String username, String postId) throws ArchivoCorruptoException {
        for (Post p : cargarPostsDeUsuario(username)) {
            if (p.getId().equals(postId)) {
                return p;
            }
        }
        return null;
    }

    /** Da o quita el like de usernameQueDaLike sobre el post. Devuelve el nuevo estado (true = quedó likeado). */
    public static boolean alternarLike(String usernameAutorPost, String postId, String usernameQueDaLike)
            throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(usernameAutorPost);
        boolean nuevoEstado = false;
        for (Post p : posts) {
            if (p.getId().equals(postId)) {
                nuevoEstado = p.alternarLike(usernameQueDaLike);
                break;
            }
        }
        guardarPostsDeUsuario(usernameAutorPost, posts);
        return nuevoEstado;
    }

    public static Comentario agregarComentario(String usernameAutorPost, String postId,
            String usernameComentador, String texto) throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(usernameAutorPost);
        Comentario nuevo = new Comentario(usernameComentador, texto);
        for (Post p : posts) {
            if (p.getId().equals(postId)) {
                p.agregarComentario(nuevo);
                break;
            }
        }
        guardarPostsDeUsuario(usernameAutorPost, posts);
        return nuevo;
    }

    /** Fija el "impulso" de likes de un post (usado para sembrar likes iniciales en cuentas oficiales). */
    public static void establecerLikesBase(String usernameAutorPost, String postId, int likesBase)
            throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(usernameAutorPost);
        for (Post p : posts) {
            if (p.getId().equals(postId)) {
                p.setLikesBase(likesBase);
                break;
            }
        }
        guardarPostsDeUsuario(usernameAutorPost, posts);
    }

    
    public static ListaEnlazada<Post> obtenerFeed(String username) throws ArchivoCorruptoException {
        ListaEnlazada<Post> feed = new ListaEnlazada<>();

        ListaEnlazada<String> siguiendo = GestorInstaPlus.obtenerFollowing(username);
        for (String cuenta : siguiendo) {
            for (Post p : cargarPostsDeUsuario(cuenta)) {
                feed.agregar(p);
            }
        }

        feed.ordenarPor(Comparator.comparingLong(Post::getFechaPublicacion).reversed());
        return feed;
    }

    /** Busca posts cuyo texto contenga el hashtag indicado (sin el símbolo #). */
    public static ListaEnlazada<Post> buscarPorHashtag(String username, String hashtag) throws ArchivoCorruptoException {
        ListaEnlazada<Post> resultado = new ListaEnlazada<>();
        String buscado = hashtag.toLowerCase();
        ListaEnlazada<String> siguiendo = GestorInstaPlus.obtenerFollowing(username);
        siguiendo.agregar(username);
        for (String cuenta : siguiendo) {
            for (Post p : cargarPostsDeUsuario(cuenta)) {
                for (String tag : p.getHashtags()) {
                    if (tag.toLowerCase().equals(buscado)) {
                        resultado.agregar(p);
                        break;
                    }
                }
            }
        }
        resultado.ordenarPor(Comparator.comparingLong(Post::getFechaPublicacion).reversed());
        return resultado;
    }
}