package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class GestorPosts {

    private static final Random RANDOM_LIKES = new Random();

    private static File archivoPostsDe(String username) {
        return new File(GestorInstaPlus.rutaCarpetaInsta(username), "insta" + ArchivosInsta.EXTENSION);
    }

    @SuppressWarnings("unchecked")
    public static List<Post> cargarPostsDeUsuario(String username) throws ArchivoCorruptoException {
        List<Post> posts = new ArrayList<>();
        File archivo = archivoPostsDe(username);
        if (!archivo.exists()) {
            return posts;
        }
        try {
            Object obj = ArchivosInsta.leerObjeto(archivo);
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
        ArchivosInsta.guardarObjeto(archivoPostsDe(username), new ArrayList<Object>(posts));
    }

    
    public static Post crearPost(String username, String texto, String rutaImagen)
            throws ArchivoCorruptoException, IOException {
        List<Post> posts = cargarPostsDeUsuario(username);
        Post nuevo = new Post(username, texto, rutaImagen);

        UsuarioInsta autor = GestorInstaPlus.buscarPorUsername(username);
        if (autor != null && autor.isCuentaOficial()) {

            int bonus = 500 + RANDOM_LIKES.nextInt(49500); // entre 500 y 50,000
            nuevo.setLikesBase(bonus);
        }

        posts.add(0, nuevo); // más reciente primero
        guardarPostsDeUsuario(username, posts);

        notificarPublicacionNueva(username, nuevo);
        return nuevo;
    }


    private static void notificarPublicacionNueva(String username, Post nuevo) {
        try {
            for (String mencionado : nuevo.getMenciones()) {
                if (!mencionado.equalsIgnoreCase(username) && GestorInstaPlus.estaActiva(mencionado)) {
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

    

    public static List<Comentario> comentariosVisibles(Post post) {
        List<Comentario> visibles = new ArrayList<>();
        if (post == null) {
            return visibles;
        }
        java.util.Set<String> desactivados = GestorInstaPlus.usernamesDesactivados();
        for (Comentario c : post.getComentarios()) {
            String autor = c.getUsernameAutor();
            if (autor == null || !desactivados.contains(autor.toLowerCase())) {
                visibles.add(c);
            }
        }
        return visibles;
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

  
    static void renombrarUsuarioEnPosts(String viejo, String nuevo, List<UsuarioInsta> usuarios)
            throws ArchivoCorruptoException, IOException {
        for (UsuarioInsta u : usuarios) {
            String dueno = u.getUsername().equalsIgnoreCase(viejo) ? nuevo : u.getUsername();
            List<Post> posts = cargarPostsDeUsuario(dueno);
            boolean cambio = false;
            for (Post p : posts) {
                if (p.getUsernameAutor() != null && p.getUsernameAutor().equalsIgnoreCase(viejo)) {
                    cambiarAutor(p, nuevo);
                    cambio = true;
                }
                for (Comentario c : p.getComentarios()) {
                    if (c.getUsernameAutor() != null && c.getUsernameAutor().equalsIgnoreCase(viejo)) {
                        c.renombrarAutor(nuevo);
                        cambio = true;
                    }
                }
            }
            if (cambio) {
                guardarPostsDeUsuario(dueno, posts);
            }
        }
    }


    private static void cambiarAutor(Post post, String nuevoAutor) {
        try {
            java.lang.reflect.Field campo = Post.class.getDeclaredField("usernameAutor");
            campo.setAccessible(true);
            campo.set(post, nuevoAutor);
        } catch (ReflectiveOperationException | RuntimeException e) {
            System.out.println("No se pudo actualizar el autor del post " + post.getId() + ": " + e.getMessage());
        }
    }
}