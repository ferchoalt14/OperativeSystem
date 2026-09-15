package operativesystem;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Cliente de sockets de INSTA+. Hay uno por sesión abierta de INSTA+.
 *
 * - {@link #enviar(PaqueteInsta, Consumer)} manda una solicitud y la respuesta llega al callback (en el EDT).
 * - Los eventos en tiempo real que empuja el servidor llegan a {@link Oyente#alRecibirEvento} (en el EDT).
 * - Si se pierde la conexión, se reintenta sola cada pocos segundos.
 */
public class ClienteInsta {

    public interface Oyente {
        void alRecibirEvento(PaqueteInsta evento);

        void alCambiarConexion(boolean conectado);
    }

    private static final int ESPERA_RESPUESTA_MS = 6000;
    private static final int INTERVALO_RECONEXION_MS = 3000;
    /** Host del servidor; en LAN se define con -Dinsta.server.host=IP_DEL_SERVIDOR. */
    private static final String HOST_SERVIDOR = System.getProperty("insta.server.host", "127.0.0.1");

    private final String usuario;
    private final Oyente oyente;
    private final AtomicLong secuencia = new AtomicLong(1);
    private final Map<Long, Consumer<PaqueteInsta>> pendientes = new ConcurrentHashMap<>();
    private final Object candadoEscritura = new Object();

    private volatile Socket socket;
    private ObjectOutputStream salida;
    private volatile boolean activo;
    private Timer timerReconexion;

    public ClienteInsta(String usuario, Oyente oyente) {
        this.usuario = usuario;
        this.oyente = oyente;
    }

    public String getUsuario() {
        return usuario;
    }

    public boolean estaConectado() {
        Socket s = socket;
        return s != null && s.isConnected() && !s.isClosed();
    }

    /** Abre la conexión (y levanta el servidor si hace falta). Llamar desde el EDT. */
    public void conectar() {
        activo = true;
        if (!intentarConectar()) {
            avisarConexion(false);
            programarReconexion();
        }
    }

    /** Cierra la conexión y deja de reintentar. */
    public void desconectar() {
        activo = false;
        detenerReconexion();
        Socket s;
        synchronized (this) {
            s = socket;
            socket = null;
        }
        if (s != null) {
            try {
                escribir(new PaqueteInsta(PaqueteInsta.Tipo.DESCONECTAR));
            } catch (IOException ignored) {
                // ya estaba cerrada
            }
            cerrarSilencioso(s);
        }
        synchronized (candadoEscritura) {
            salida = null;
        }
        fallarPendientes("La sesión se cerró.");
    }

    /**
     * Envía una solicitud al servidor. El callback (puede ser null) recibe RESPUESTA_OK o RESPUESTA_ERROR
     * y siempre se ejecuta en el hilo de Swing.
     */
    public void enviar(PaqueteInsta paquete, Consumer<PaqueteInsta> alResponder) {
        if (!estaConectado() && !intentarConectar()) {
            programarReconexion();
            responderError(alResponder, "Sin conexión con el servidor de INSTA+. Reintentando…");
            return;
        }
        long id = secuencia.getAndIncrement();
        paquete.setIdSolicitud(id);

        if (alResponder != null) {
            pendientes.put(id, alResponder);
            Timer espera = new Timer(ESPERA_RESPUESTA_MS, e -> {
                Consumer<PaqueteInsta> cb = pendientes.remove(id);
                if (cb != null) {
                    cb.accept(PaqueteInsta.error(id, "El servidor tardó demasiado en responder."));
                }
            });
            espera.setRepeats(false);
            espera.start();
        }

        try {
            escribir(paquete);
        } catch (IOException e) {
            Consumer<PaqueteInsta> cb = pendientes.remove(id);
            responderError(cb, "No se pudo enviar: se perdió la conexión.");
            perdidaDeConexion(socket);
        }
    }

    // ------------------------------------------------------------------------------------------

    private synchronized boolean intentarConectar() {
        if (!activo) {
            return false;
        }
        if (estaConectado()) {
            return true;
        }
        ServidorInsta.iniciar(); // si nadie es servidor todavía, este proceso lo será

        Socket nuevo = new Socket();
        try {
            nuevo.connect(new InetSocketAddress(InetAddress.getByName(HOST_SERVIDOR), ServidorInsta.PUERTO), 2000);
            nuevo.setTcpNoDelay(true);
            nuevo.setSoTimeout(3000); // solo para el saludo inicial
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(nuevo.getOutputStream()));
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(nuevo.getInputStream()));
            nuevo.setSoTimeout(0);

            synchronized (candadoEscritura) {
                salida = out;
            }
            socket = nuevo;

            Thread lector = new Thread(() -> leer(nuevo, in), "insta-cliente-" + usuario);
            lector.setDaemon(true);
            lector.start();

            escribir(PaqueteInsta.conectar(usuario));
            detenerReconexion();
            avisarConexion(true);
            return true;
        } catch (IOException e) {
            cerrarSilencioso(nuevo);
            if (socket == nuevo) {
                socket = null;
            }
            return false;
        }
    }

    private void leer(Socket s, ObjectInputStream in) {
        try {
            while (!s.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof PaqueteInsta) {
                    despachar((PaqueteInsta) obj);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // conexión cerrada
        } finally {
            perdidaDeConexion(s);
        }
    }

    private void despachar(PaqueteInsta p) {
        if (p.esRespuesta()) {
            Consumer<PaqueteInsta> cb = pendientes.remove(p.getIdSolicitud());
            if (cb != null) {
                SwingUtilities.invokeLater(() -> cb.accept(p));
            }
        } else if (oyente != null) {
            SwingUtilities.invokeLater(() -> {
                if (activo) {
                    oyente.alRecibirEvento(p);
                }
            });
        }
    }

    private void perdidaDeConexion(Socket s) {
        synchronized (this) {
            if (s == null || socket != s) {
                return; // ya se había reemplazado o cerrado a propósito
            }
            socket = null;
        }
        cerrarSilencioso(s);
        fallarPendientes("Se perdió la conexión con el servidor.");
        if (activo) {
            SwingUtilities.invokeLater(() -> {
                avisarConexion(false);
                programarReconexion();
            });
        }
    }

    private void escribir(PaqueteInsta p) throws IOException {
        synchronized (candadoEscritura) {
            if (salida == null) {
                throw new IOException("Sin conexión");
            }
            salida.reset();
            salida.writeObject(p);
            salida.flush();
        }
    }

    private void programarReconexion() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::programarReconexion);
            return;
        }
        if (!activo) {
            return;
        }
        if (timerReconexion == null) {
            timerReconexion = new Timer(INTERVALO_RECONEXION_MS, e -> {
                if (!activo || intentarConectar()) {
                    detenerReconexion();
                }
            });
        }
        if (!timerReconexion.isRunning()) {
            timerReconexion.start();
        }
    }

    private void detenerReconexion() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::detenerReconexion);
            return;
        }
        if (timerReconexion != null) {
            timerReconexion.stop();
        }
    }

    private void avisarConexion(boolean conectado) {
        if (oyente == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            oyente.alCambiarConexion(conectado);
        } else {
            SwingUtilities.invokeLater(() -> oyente.alCambiarConexion(conectado));
        }
    }

    private void fallarPendientes(String motivo) {
        List<Consumer<PaqueteInsta>> copia = new ArrayList<>(pendientes.values());
        pendientes.clear();
        for (Consumer<PaqueteInsta> cb : copia) {
            responderError(cb, motivo);
        }
    }

    private static void responderError(Consumer<PaqueteInsta> cb, String mensaje) {
        if (cb != null) {
            SwingUtilities.invokeLater(() -> cb.accept(PaqueteInsta.error(0, mensaje)));
        }
    }

    private static void cerrarSilencioso(Socket s) {
        try {
            s.close();
        } catch (IOException ignored) {
            // nada
        }
    }
}