package operativesystem;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Guardado "seguro" de archivos binarios: se escribe primero a un temporal y luego se reemplaza el
 * archivo real. Así, si el servidor de sockets escribe mientras la interfaz lee, nunca se lee un
 * archivo a medio escribir (lo que antes podía dar ArchivoCorruptoException).
 */
final class ArchivosInsta {

    /** Extensión propia utilizada por la persistencia binaria del sistema. */
    static final String EXTENSION = ".sop";

    private ArchivosInsta() {
    }

    /** Guarda una cadena en binario usando DataOutputStream. */
    static void guardarCadena(File destino, String valor) throws IOException {
        File carpeta = destino.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destino)))) {
            dos.writeUTF(valor == null ? "" : valor);
        }
    }

    /** Lee una cadena guardada con guardarCadena. */
    static String leerCadena(File origen) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(origen)))) {
            return dis.readUTF();
        }
    }

    /** Lee un objeto Java serializado desde un archivo binario. */
    static Object leerObjeto(File origen) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(origen)))) {
            return ois.readObject();
        }
    }

    static void guardarObjeto(File destino, Object objeto) throws IOException {
        File carpeta = destino.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        File temporal = File.createTempFile(destino.getName() + "_", ".tmp", carpeta);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(temporal)))) {
            oos.writeObject(objeto);
        } catch (IOException e) {
            temporal.delete();
            throw e;
        }

        IOException ultimo = null;
        for (int intento = 0; intento < 6; intento++) {
            try {
                try {
                    Files.move(temporal.toPath(), destino.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            } catch (IOException e) {
                // En Windows puede fallar si otro hilo tiene el archivo abierto para lectura: reintentamos.
                ultimo = e;
                try {
                    Thread.sleep(25L * (intento + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Files.deleteIfExists(temporal.toPath());
        throw ultimo != null ? ultimo : new IOException("No se pudo guardar " + destino.getName());
    }
}
