package operativesystem;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Servidor de INSTA+ basado en sockets TCP (localhost).
 *
 * Todo lo que es "tiempo real" pasa por aquí: mensajes directos, lecturas (✓✓), comentarios,
 * likes, publicaciones nuevas, eliminaciones y menciones. El servidor guarda en disco usando los
 * gestores y luego avisa (push) a los clientes conectados que les interesa el cambio.
 *
 * Se levanta automáticamente la primera vez que un {@link ClienteInsta} se conecta. Si otro proceso
 * ya tiene el puerto ocupado, este proceso simplemente se conecta como cliente a ese servidor.
 */
public final class ServidorInsta {

    public static final int PUERTO = 5055;

    private static ServidorInsta instancia;

    private final ServerSocket serverSocket;
    private final Map<String, Set<Conexion>> conectados = new ConcurrentHashMap<>();
    private final Object candadoDatos = new Object();

    private ServidorInsta(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Inicia el servidor si todavía no está corriendo en este proceso.
     * @return true si el servidor quedó corriendo en este proceso.
     */
    public static synchronized boolean iniciar() {
        if (instancia != null) {
            return true;
        }
        try {
            ServerSocket ss = new ServerSocket(PUERTO, 50, InetAddress.getLoopbackAddress());
            instancia = new ServidorInsta(ss);
            Thread hilo = new Thread(instancia::aceptarConexiones, "insta-servidor");
            hilo.setDaemon(true);
            hilo.start();
            System.out.println("[INSTA+] Servidor de sockets escuchando en el puerto " + PUERTO);
            return true;
        } catch (IOException e) {
            // Puerto ocupado: normalmente otra instancia del sistema ya es el servidor.
            return false;
        }
    }

    public static synchronized boolean estaCorriendo() {
        return instancia != null;
    }

    private void aceptarConexiones() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                Conexion conexion = new Conexion(socket);
                conexion.setDaemon(true);
                conexion.start();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    break;
                }
            }
        }
        synchronized (ServidorInsta.class) {
            instancia = null;
        }
    }

    // ------------------------------------------------------------------------------------------
    //  Registro de conexiones
    // ------------------------------------------------------------------------------------------

    private static String clave(String username) {
        return username == null ? "" : username.toLowerCase();
    }

    private void registrar(Conexion c) {
        conectados.computeIfAbsent(clave(c.usuario), k -> new CopyOnWriteArraySet<>()).add(c);
    }

    private void quitar(Conexion c) {
        if (c.usuario == null) {
            return;
        }
        Set<Conexion> set = conectados.get(clave(c.usuario));
        if (set != null) {
            set.remove(c);
            if (set.isEmpty()) {
                conectados.remove(clave(c.usuario), set);
            }
        }
    }

    /** Envía el paquete a todas las conexiones de cada usuario indicado (sin repetir usuarios). */
    private void enviarA(PaqueteInsta paquete, String... usernames) {
        enviarA(paquete, Arrays.asList(usernames));
    }

    private void enviarA(PaqueteInsta paquete, Iterable<String> usernames) {
        Set<String> yaEnviados = new HashSet<>();
        for (String u : usernames) {
            if (u == null || !yaEnviados.add(clave(u))) {
                continue;
            }
            Set<Conexion> set = conectados.get(clave(u));
            if (set != null) {
                for (Conexion c : set) {
                    c.enviar(paquete);
                }
            }
        }
    }

    private void difundir(PaqueteInsta paquete) {
        for (Set<Conexion> set : conectados.values()) {
            for (Conexion c : set) {
                c.enviar(paquete);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    //  Lógica de cada solicitud
    // ------------------------------------------------------------------------------------------

    private void procesar(Conexion c, PaqueteInsta p) {
        long id = p.getIdSolicitud();
        String yo = c.usuario;
        try {
            synchronized (candadoDatos) {
                switch (p.getTipo()) {
                    case ENVIAR_MENSAJE:
                        procesarMensaje(c, p, yo, id);
                        break;
                    case MARCAR_LEIDOS:
                        procesarMarcarLeidos(c, p, yo, id);
                        break;
                    case ELIMINAR_CHAT:
                        procesarEliminarChat(c, p, yo, id);
                        break;
                    case COMENTAR:
                        procesarComentario(c, p, yo, id);
                        break;
                    case ALTERNAR_LIKE:
                        procesarLike(c, p, yo, id);
                        break;
                    case CREAR_POST:
                        procesarCrearPost(c, p, yo, id);
                        break;
                    case ELIMINAR_POST:
                        procesarEliminarPost(c, p, yo, id);
                        break;
                    default:
                        c.enviar(PaqueteInsta.error(id, "Solicitud no reconocida."));
                        break;
                }
            }
        } catch (ArchivoCorruptoException | IOException e) {
            c.enviar(PaqueteInsta.error(id, "Error de archivos: " + e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            c.enviar(PaqueteInsta.error(id, "Error inesperado en el servidor."));
        }
    }

    private void procesarMensaje(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String para = p.getOVacio(PaqueteInsta.PARA).trim();
        String texto = p.getOVacio(PaqueteInsta.TEXTO).trim();
        if (para.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "No se indicó el destinatario."));
            return;
        }
        if (texto.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "El mensaje está vacío."));
            return;
        }
        if (texto.length() > 1000) {
            c.enviar(PaqueteInsta.error(id, "El mensaje es demasiado largo (máx. 1000 caracteres)."));
            return;
        }
        if (GestorInstaPlus.buscarPorUsername(para) == null) {
            c.enviar(PaqueteInsta.error(id, "La cuenta @" + para + " ya no existe."));
            return;
        }
        GestorMensajes.enviarMensaje(yo, para, texto);
        c.enviar(PaqueteInsta.ok(id));
        enviarA(new PaqueteInsta(PaqueteInsta.Tipo.MENSAJE_NUEVO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.PARA, para).con(PaqueteInsta.TEXTO, texto), para, yo);
    }

    private void procesarMarcarLeidos(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String con = p.getOVacio(PaqueteInsta.PARA).trim();
        if (con.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "Conversación inválida."));
            return;
        }
        if (GestorMensajes.contarNoLeidosDe(yo, con) > 0) {
            GestorMensajes.marcarComoLeidos(yo, con);
            enviarA(new PaqueteInsta(PaqueteInsta.Tipo.MENSAJES_LEIDOS)
                    .con(PaqueteInsta.DE, yo).con(PaqueteInsta.PARA, con), con, yo);
        }
        c.enviar(PaqueteInsta.ok(id));
    }

    private void procesarEliminarChat(Conexion c, PaqueteInsta p, String yo, long id) {
        String con = p.getOVacio(PaqueteInsta.PARA).trim();
        if (con.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "Conversación inválida."));
            return;
        }
        GestorMensajes.eliminarConversacion(yo, con);
        c.enviar(PaqueteInsta.ok(id));
        enviarA(new PaqueteInsta(PaqueteInsta.Tipo.CHAT_ELIMINADO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.PARA, con), con, yo);
    }

    private void procesarComentario(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String autor = p.getOVacio(PaqueteInsta.AUTOR_POST);
        String postId = p.getOVacio(PaqueteInsta.POST_ID);
        String texto = p.getOVacio(PaqueteInsta.TEXTO).trim();
        if (texto.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "Escribe algo antes de comentar."));
            return;
        }
        if (texto.length() > 300) {
            c.enviar(PaqueteInsta.error(id, "El comentario es demasiado largo (máx. 300 caracteres)."));
            return;
        }
        if (GestorPosts.obtenerPostPorId(autor, postId) == null) {
            c.enviar(PaqueteInsta.error(id, "Esta publicación ya no existe."));
            return;
        }
        GestorPosts.agregarComentario(autor, postId, yo, texto);
        c.enviar(PaqueteInsta.ok(id));
        difundir(new PaqueteInsta(PaqueteInsta.Tipo.COMENTARIO_NUEVO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.AUTOR_POST, autor)
                .con(PaqueteInsta.POST_ID, postId).con(PaqueteInsta.TEXTO, texto));
    }

    private void procesarLike(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String autor = p.getOVacio(PaqueteInsta.AUTOR_POST);
        String postId = p.getOVacio(PaqueteInsta.POST_ID);
        if (GestorPosts.obtenerPostPorId(autor, postId) == null) {
            c.enviar(PaqueteInsta.error(id, "Esta publicación ya no existe."));
            return;
        }
        boolean likeado = GestorPosts.alternarLike(autor, postId, yo);
        Post actualizado = GestorPosts.obtenerPostPorId(autor, postId);
        int likes = actualizado != null ? actualizado.getLikes() : 0;

        c.enviar(PaqueteInsta.ok(id).con(PaqueteInsta.LIKES, likes).con(PaqueteInsta.LIKEADO, likeado));
        difundir(new PaqueteInsta(PaqueteInsta.Tipo.LIKE_ACTUALIZADO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.AUTOR_POST, autor)
                .con(PaqueteInsta.POST_ID, postId).con(PaqueteInsta.LIKES, likes)
                .con(PaqueteInsta.LIKEADO, likeado));
    }

    private void procesarCrearPost(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String texto = p.getOVacio(PaqueteInsta.TEXTO).trim();
        String ruta = p.getOVacio(PaqueteInsta.RUTA_IMAGEN).trim();

        if (texto.isEmpty() && ruta.isEmpty()) {
            c.enviar(PaqueteInsta.error(id, "Agrega una imagen o un texto para poder publicar."));
            return;
        }
        if (texto.length() > Post.MAX_CARACTERES) {
            c.enviar(PaqueteInsta.error(id, "El texto supera el máximo de " + Post.MAX_CARACTERES + " caracteres."));
            return;
        }
        if (!ruta.isEmpty()) {
            File original = new File(ruta);
            if (!original.isFile()) {
                c.enviar(PaqueteInsta.error(id, "La imagen seleccionada ya no existe."));
                return;
            }
            // Copiamos la imagen a la carpeta personal para que la publicación no dependa del archivo original.
            File carpetaImagenes = new File(GestorInstaPlus.rutaCarpetaInsta(yo), "imagenes");
            carpetaImagenes.mkdirs();
            File destino = new File(carpetaImagenes, System.currentTimeMillis() + "_" + original.getName());
            try {
                Files.copy(original.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                ruta = destino.getAbsolutePath();
            } catch (IOException ex) {
                ruta = original.getAbsolutePath();
            }
        }

        Post nuevo = GestorPosts.crearPost(yo, texto, ruta);
        c.enviar(PaqueteInsta.ok(id).con(PaqueteInsta.POST_ID, nuevo.getId()));

        PaqueteInsta evento = new PaqueteInsta(PaqueteInsta.Tipo.POST_NUEVO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.AUTOR_POST, yo).con(PaqueteInsta.POST_ID, nuevo.getId());
        List<String> destinatarios = new ArrayList<>();
        destinatarios.add(yo);
        for (String seguidor : GestorInstaPlus.obtenerFollowers(yo)) {
            destinatarios.add(seguidor);
        }
        enviarA(evento, destinatarios);

        for (String mencionado : nuevo.getMenciones()) {
            if (!mencionado.equalsIgnoreCase(yo)) {
                enviarA(new PaqueteInsta(PaqueteInsta.Tipo.MENCION)
                        .con(PaqueteInsta.DE, yo).con(PaqueteInsta.AUTOR_POST, yo)
                        .con(PaqueteInsta.POST_ID, nuevo.getId()), mencionado);
            }
        }
    }

    private void procesarEliminarPost(Conexion c, PaqueteInsta p, String yo, long id)
            throws ArchivoCorruptoException, IOException {
        String postId = p.getOVacio(PaqueteInsta.POST_ID);
        // Solo el autor puede eliminar: siempre se busca en la carpeta del usuario conectado.
        if (!GestorPosts.eliminarPost(yo, postId)) {
            c.enviar(PaqueteInsta.error(id, "No se encontró la publicación (quizá ya fue eliminada)."));
            return;
        }
        c.enviar(PaqueteInsta.ok(id));
        difundir(new PaqueteInsta(PaqueteInsta.Tipo.POST_ELIMINADO)
                .con(PaqueteInsta.DE, yo).con(PaqueteInsta.AUTOR_POST, yo).con(PaqueteInsta.POST_ID, postId));
    }

    // ------------------------------------------------------------------------------------------
    //  Conexión individual (un hilo por cliente)
    // ------------------------------------------------------------------------------------------

    private final class Conexion extends Thread {

        private final Socket socket;
        private ObjectOutputStream salida;
        private volatile String usuario;

        Conexion(Socket socket) {
            super("insta-conexion-" + socket.getPort());
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                salida = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                salida.flush();
                ObjectInputStream entrada = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                while (!socket.isClosed()) {
                    Object obj = entrada.readObject();
                    if (!(obj instanceof PaqueteInsta)) {
                        continue;
                    }
                    PaqueteInsta p = (PaqueteInsta) obj;

                    if (p.getTipo() == PaqueteInsta.Tipo.DESCONECTAR) {
                        break;
                    }
                    if (p.getTipo() == PaqueteInsta.Tipo.CONECTAR) {
                        String u = p.getOVacio(PaqueteInsta.DE).trim();
                        if (u.isEmpty()) {
                            enviar(PaqueteInsta.error(p.getIdSolicitud(), "Usuario inválido."));
                            continue;
                        }
                        quitar(this);
                        usuario = u;
                        registrar(this);
                        enviar(PaqueteInsta.ok(p.getIdSolicitud()));
                        continue;
                    }
                    if (usuario == null) {
                        enviar(PaqueteInsta.error(p.getIdSolicitud(), "Primero debes identificarte."));
                        continue;
                    }
                    procesar(this, p);
                }
            } catch (EOFException | SocketException e) {
                // El cliente se desconectó.
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("[INSTA+] Conexión cerrada por error: " + e.getMessage());
            } finally {
                quitar(this);
                cerrar();
            }
        }

        void enviar(PaqueteInsta paquete) {
            synchronized (this) {
                if (salida == null || socket.isClosed()) {
                    return;
                }
                try {
                    salida.reset();
                    salida.writeObject(paquete);
                    salida.flush();
                } catch (IOException e) {
                    cerrar();
                }
            }
        }

        void cerrar() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // nada que hacer
            }
        }
    }
}