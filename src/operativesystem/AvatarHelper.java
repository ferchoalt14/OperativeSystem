package operativesystem;

import javax.swing.*;
import java.io.File;


public class AvatarHelper {

    public static Icon avatarPara(String username, int size) {
        try {
            UsuarioInsta u = (username == null) ? null : GestorInstaPlus.buscarPorUsername(username);
            String ruta = (u != null) ? u.getRutaFotoPerfil() : null;
            if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
                return TemaUI.crearIconoCircularDeImagen(new File(ruta), size);
            }
        } catch (ArchivoCorruptoException ex) {
            // se usa el avatar con la inicial
        }
        String inicial = (username == null || username.isBlank()) ? "?" : username.substring(0, 1).toUpperCase();
        return TemaUI.crearIconoCircular(inicial, TemaUI.colorApp(username == null ? 0 : Math.floorMod(username.toLowerCase().hashCode(), 1000)), size);
    }
}