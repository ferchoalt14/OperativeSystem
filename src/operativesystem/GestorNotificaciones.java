package operativesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorNotificaciones {

    private static File archivoDe(String username) {
        return new File(GestorInstaPlus.rutaCarpetaInsta(username), "notificaciones.ins");
    }

    @SuppressWarnings("unchecked")
    public static List<Notificacion> cargarNotificaciones(String username) throws ArchivoCorruptoException {
        List<Notificacion> lista = new ArrayList<>();
        File archivo = archivoDe(username);
        if (!archivo.exists()) {
            return lista;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof Notificacion) {
                        lista.add((Notificacion) o);
                    }
                }
            }
        } catch (EOFException e) {
            
        } catch (Exception e) {
            throw new ArchivoCorruptoException(archivo.getName(), e);
        }
        return lista;
    }

    private static void guardar(String username, List<Notificacion> lista) throws IOException {
        File carpeta = new File(GestorInstaPlus.rutaCarpetaInsta(username));
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        ArchivosInsta.guardarObjeto(archivoDe(username), new ArrayList<Object>(lista));
    }

    public static void agregarNotificacion(String usernameDestino, TipoNotificacion tipo, String usernameOrigen,
                                            String mensaje, String usernameAutorPost, String postId)
            throws ArchivoCorruptoException, IOException {
        List<Notificacion> lista = cargarNotificaciones(usernameDestino);
        lista.add(0, new Notificacion(tipo, usernameOrigen, mensaje, usernameAutorPost, postId));
        // conservar solo las últimas 100 para no crecer indefinidamente
        if (lista.size() > 100) {
            lista = new ArrayList<>(lista.subList(0, 100));
        }
        guardar(usernameDestino, lista);
    }

    public static int contarNoLeidas(String username) throws ArchivoCorruptoException {
        int cuenta = 0;
        for (Notificacion n : cargarNotificaciones(username)) {
            if (!n.isLeida()) {
                cuenta++;
            }
        }
        return cuenta;
    }

    public static void marcarTodasComoLeidas(String username) throws ArchivoCorruptoException, IOException {
        List<Notificacion> lista = cargarNotificaciones(username);
        boolean cambio = false;
        for (Notificacion n : lista) {
            if (!n.isLeida()) {
                n.setLeida(true);
                cambio = true;
            }
        }
        if (cambio) {
            guardar(username, lista);
        }
    }
}
