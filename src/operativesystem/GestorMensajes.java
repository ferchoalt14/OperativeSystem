package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Maneja las conversaciones (DMs) entre usuarios de INSTA+. */
public class GestorMensajes {

    private static final String RUTA_CONVERSACIONES =
            System.getProperty("user.home") + "/MiniWindowsData/INSTA_RAIZ/conversaciones/";

    private static void asegurarCarpeta() {
        File carpeta = new File(RUTA_CONVERSACIONES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    /** Nombre de archivo determinístico para la conversación entre dos usuarios (sin importar el orden). */
    private static File archivoConversacion(String usuarioA, String usuarioB) {
        String u1 = usuarioA.toLowerCase();
        String u2 = usuarioB.toLowerCase();
        String nombre = (u1.compareTo(u2) <= 0)
                ? "conv_" + u1 + "__" + u2 + ".ins"
                : "conv_" + u2 + "__" + u1 + ".ins";
        return new File(RUTA_CONVERSACIONES, nombre);
    }

    @SuppressWarnings("unchecked")
    public static List<Mensaje> cargarConversacion(String usuarioA, String usuarioB) throws ArchivoCorruptoException {
        asegurarCarpeta();
        List<Mensaje> mensajes = new ArrayList<>();
        File archivo = archivoConversacion(usuarioA, usuarioB);
        if (!archivo.exists()) {
            return mensajes;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof Mensaje) {
                        mensajes.add((Mensaje) o);
                    }
                }
            }
        } catch (EOFException e) {
            
        } catch (Exception e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
        return mensajes;
    }

    private static void guardarConversacion(String usuarioA, String usuarioB, List<Mensaje> mensajes) throws IOException {
        asegurarCarpeta();
        File archivo = archivoConversacion(usuarioA, usuarioB);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(new ArrayList<Object>(mensajes));
        }
    }

    /** Envía un mensaje de "remitente" a "destinatario" y lo guarda en la conversación de ambos. */
    public static Mensaje enviarMensaje(String remitente, String destinatario, String texto)
            throws ArchivoCorruptoException, IOException {
        List<Mensaje> mensajes = cargarConversacion(remitente, destinatario);
        Mensaje nuevo = new Mensaje(remitente, destinatario, texto);
        mensajes.add(nuevo);
        guardarConversacion(remitente, destinatario, mensajes);
        return nuevo;
    }

    /** Marca como leídos todos los mensajes que "usuarioQueLee" recibió de "otroUsuario". */
    public static void marcarComoLeidos(String usuarioQueLee, String otroUsuario)
            throws ArchivoCorruptoException, IOException {
        List<Mensaje> mensajes = cargarConversacion(usuarioQueLee, otroUsuario);
        boolean cambio = false;
        for (Mensaje m : mensajes) {
            if (m.getDestinatario().equalsIgnoreCase(usuarioQueLee) && !m.isLeido()) {
                m.setLeido(true);
                cambio = true;
            }
        }
        if (cambio) {
            guardarConversacion(usuarioQueLee, otroUsuario, mensajes);
        }
    }

    /** Cuenta los mensajes no leídos que "username" tiene pendientes de "otroUsuario". */
    public static int contarNoLeidosDe(String username, String otroUsuario) throws ArchivoCorruptoException {
        int cuenta = 0;
        for (Mensaje m : cargarConversacion(username, otroUsuario)) {
            if (m.getDestinatario().equalsIgnoreCase(username) && !m.isLeido()) {
                cuenta++;
            }
        }
        return cuenta;
    }

    /** Lista, para "username", todas las personas con las que tiene conversación, ordenadas por mensaje más reciente. */
    public static ListaEnlazada<String> obtenerConversaciones(String username) throws ArchivoCorruptoException {
        asegurarCarpeta();
        String prefijo = "conv_";
        File carpeta = new File(RUTA_CONVERSACIONES);
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith(prefijo) && nombre.endsWith(".ins"));

        List<String> otros = new ArrayList<>();
        List<Long> ultimaFecha = new ArrayList<>();

        if (archivos != null) {
            String buscado = username.toLowerCase();
            for (File archivo : archivos) {
                String nombreSinExt = archivo.getName().substring(prefijo.length(), archivo.getName().length() - 4);
                String[] partes = nombreSinExt.split("__");
                if (partes.length != 2) {
                    continue;
                }
                String otro = null;
                if (partes[0].equals(buscado)) {
                    otro = partes[1];
                } else if (partes[1].equals(buscado)) {
                    otro = partes[0];
                }
                if (otro == null) {
                    continue;
                }
                List<Mensaje> mensajes = cargarConversacion(username, otro);
                if (mensajes.isEmpty()) {
                    continue;
                }
                long ultima = mensajes.get(mensajes.size() - 1).getFecha();
                otros.add(otro);
                ultimaFecha.add(ultima);
            }
        }

        // Ordenar por fecha del último mensaje, más reciente primero (selection sort simple).
        for (int i = 0; i < otros.size() - 1; i++) {
            int mejor = i;
            for (int j = i + 1; j < otros.size(); j++) {
                if (ultimaFecha.get(j) > ultimaFecha.get(mejor)) {
                    mejor = j;
                }
            }
            if (mejor != i) {
                Long f = ultimaFecha.get(i); ultimaFecha.set(i, ultimaFecha.get(mejor)); ultimaFecha.set(mejor, f);
                String u = otros.get(i); otros.set(i, otros.get(mejor)); otros.set(mejor, u);
            }
        }

        ListaEnlazada<String> resultado = new ListaEnlazada<>();
        for (String u : otros) {
            resultado.agregar(u);
        }
        return resultado;
    }

    /** Total de mensajes no leídos de "username" sumando todas sus conversaciones (para el contador/badge). */
    public static int contarNoLeidosTotal(String username) throws ArchivoCorruptoException {
        int total = 0;
        for (String otro : obtenerConversaciones(username)) {
            total += contarNoLeidosDe(username, otro);
        }
        return total;
    }
}