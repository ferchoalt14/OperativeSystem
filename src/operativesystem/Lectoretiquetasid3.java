
package operativesystem;

/**
 *
 * @author Leandro
 */
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Lectoretiquetasid3 {
    
    public static class Etiquetas {
        public String titulo;
        public String artista;
        public String album;
        public byte[] caratula;
        
        public boolean tieneCaratula(){
            return caratula != null && caratula.length > 0;
        }
    }
    
    public static Etiquetas leer(File archivoMp3){
        Etiquetas resultado = new Etiquetas();
        try (RandomAccessFile raf = new RandomAccessFile(archivoMp3, "r")){
        byte[] cabecera = new byte[10];
        raf.readFully(cabecera);
        
            if (cabecera[0] != 'I' || cabecera[1] != 'D' || cabecera[2] != '3') {
                return resultado;
            }
            
            int version = cabecera[3] & 0xFF;
            int tamanoTag = leerEnteroSincronizado(cabecera, 6);
            
            byte[] cuerpo = new byte[tamanoTag];
            raf.readFully(cuerpo);
            
            int posicion = 0;
            while (posicion + 10 <= cuerpo.length) {
            String idFrame = new String(cuerpo, posicion, 4, StandardCharsets.ISO_8859_1);
                if (idFrame.charAt(0) == 0) {
                    break;
                }
                
                int tamanoFrame = (version >= 4) ? leerEnteroSincronizado(cuerpo, posicion + 4)
                        : leerEnteroNormal(cuerpo, posicion + 4);
                
                int inicioContenido = posicion + 10;
                int finContenido = inicioContenido + tamanoFrame;
                if (tamanoFrame <= 0 || finContenido > cuerpo.length) {
                    break;
                }
                switch (idFrame) {
                case "TIT2":
                        resultado.titulo = leerFrameDeTexto(cuerpo, inicioContenido, finContenido);
                        break;
                    case "TPE1":
                        resultado.artista = leerFrameDeTexto(cuerpo, inicioContenido, finContenido);
                        break;
                    case "TALB":
                        resultado.album = leerFrameDeTexto(cuerpo, inicioContenido, finContenido);
                        break;
                    case "APIC":
                        resultado.caratula = leerFrameDeImagen(cuerpo, inicioContenido, finContenido);
                        break;
                    default:
                        break;
                }
                
                posicion = finContenido;
                        
            }
        } catch (IOException | RuntimeException ex) {}
        return resultado;
    }
    
    private static int leerEnteroSincronizado(byte[] datos, int offset) {
        return ((datos[offset] & 0x7F) << 21)
                | ((datos[offset + 1] & 0x7F) << 14)
                | ((datos[offset + 2] & 0x7F) << 7)
                | (datos[offset + 3] & 0x7F);
    }
 
    private static int leerEnteroNormal(byte[] datos, int offset) {
        return ((datos[offset] & 0xFF) << 24)
                | ((datos[offset + 1] & 0xFF) << 16)
                | ((datos[offset + 2] & 0xFF) << 8)
                | (datos[offset + 3] & 0xFF);
    }
    
    
    private static String leerFrameDeTexto(byte[] datos, int inicio, int fin) {
        if (inicio >= fin) {
            return null;
        }
        int codificacion = datos[inicio] & 0xFF;
        int desdeTexto = inicio + 1;
        try {
            String texto;
            if (codificacion == 1 || codificacion == 2) {
                texto = new String(datos, desdeTexto, fin - desdeTexto, StandardCharsets.UTF_16);
            } else if (codificacion == 3) {
                texto = new String(datos, desdeTexto, fin - desdeTexto, StandardCharsets.UTF_8);
            } else {
                texto = new String(datos, desdeTexto, fin - desdeTexto, StandardCharsets.ISO_8859_1);
            }
            texto = texto.replace("\u0000", "").trim();
            return texto.isEmpty() ? null : texto;
        } catch (Exception ex) {
            return null;
        }
    }
    
     private static byte[] leerFrameDeImagen(byte[] datos, int inicio, int fin) {
        try {
            int codificacion = datos[inicio] & 0xFF;
            int cursor = inicio + 1;
 
            while (cursor < fin && datos[cursor] != 0) {
                cursor++;
            }
            cursor += 1;
 
            cursor += 1; // tipo de imagen dentro del estándar ID3 (3 = portada frontal)
 
            if (codificacion == 1 || codificacion == 2) {
                while (cursor + 1 < fin && !(datos[cursor] == 0 && datos[cursor + 1] == 0)) {
                    cursor += 2;
                }
                cursor += 2;
            } else {
                while (cursor < fin && datos[cursor] != 0) {
                    cursor++;
                }
                cursor += 1;
            }
 
            if (cursor >= fin) {
                return null;
            }
            return Arrays.copyOfRange(datos, cursor, fin);
        } catch (Exception ex) {
            return null;
        }
    }
    
    
}
