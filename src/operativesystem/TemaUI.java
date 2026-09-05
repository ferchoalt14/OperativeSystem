/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Paleta de colores y utilidades visuales de Mini-Windows.
 * Se encarga de reemplazar el aspecto gris por defecto de Swing
 * por una interfaz moderna (basada en Nimbus) y de generar los
 * íconos circulares que se usan como accesos directos a las apps
 * y como foto de perfil.
 *
 * @author User
 */
public final class TemaUI {

    private TemaUI() {
    }

    public static final Color ACCENT = new Color(88, 86, 214);
    public static final Color ACCENT_OSCURO = new Color(60, 58, 158);
    public static final Color ACCENT_CLARO = new Color(170, 168, 235);
    public static final Color FONDO = new Color(244, 245, 250);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(32, 32, 48);
    public static final Color TEXTO_SUAVE = new Color(112, 112, 132);
    public static final Color BORDE = new Color(222, 224, 236);

    private static final Color[] COLORES_APP = {
            new Color(99, 102, 241),
            new Color(236, 72, 153),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(59, 130, 246),
            new Color(139, 92, 246),
            new Color(239, 68, 68),
            new Color(20, 184, 166)
    };

    public static Color colorApp(int indice) {
        return COLORES_APP[Math.floorMod(indice, COLORES_APP.length)];
    }

    /** Activa Nimbus (si está disponible) y aplica la paleta de colores sobre él. */
    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Si Nimbus no está disponible, se sigue con la apariencia por defecto.
        }

        UIManager.put("control", FONDO);
        UIManager.put("nimbusBase", ACCENT);
        UIManager.put("nimbusBlueGrey", new Color(214, 216, 232));
        UIManager.put("nimbusLightBackground", SUPERFICIE);
        UIManager.put("nimbusFocus", ACCENT_CLARO);
        UIManager.put("nimbusSelectionBackground", ACCENT);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("text", TEXTO);
        UIManager.put("info", SUPERFICIE);
        UIManager.put("nimbusDisabledText", TEXTO_SUAVE);

        Font fuenteBase = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", new FontUIResource(fuenteBase));
    }

    /** Ícono circular con iniciales/texto centrado, usado como avatar o ícono de app. */
    public static Icon crearIconoCircular(String texto, Color color, int diametro) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, diametro, diametro);
                g2.setColor(Color.WHITE);
                Font f = new Font("SansSerif", Font.BOLD, Math.max(11, diametro / 3));
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                String etiqueta = texto == null ? "?" : texto;
                int tx = x + (diametro - fm.stringWidth(etiqueta)) / 2;
                int ty = y + (diametro - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(etiqueta, tx, ty);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return diametro;
            }

            @Override
            public int getIconHeight() {
                return diametro;
            }
        };
    }

    /** Ícono circular recortado a partir de una imagen (foto de perfil). */
    public static Icon crearIconoCircularDeImagen(File archivo, int diametro) {
        Image imagen = new ImageIcon(archivo.getAbsolutePath()).getImage();
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Float(x, y, diametro, diametro));
                g2.drawImage(imagen, x, y, diametro, diametro, null);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return diametro;
            }

            @Override
            public int getIconHeight() {
                return diametro;
            }
        };
    }

    /** Botón de "aplicación": círculo de color con iniciales arriba y el nombre debajo. */
    public static JButton crearBotonApp(String nombre, String iniciales, Color color) {
        JButton boton = new JButton(nombre);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setIcon(crearIconoCircular(iniciales, color, 56));
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setForeground(TEXTO);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static JButton crearBotonApp(String nombre, String iniciales, Color color, ActionListener accion) {
        JButton boton = crearBotonApp(nombre, iniciales, color);
        boton.addActionListener(accion);
        return boton;
    }
}