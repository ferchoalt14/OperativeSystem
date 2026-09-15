package operativesystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Colores, botones, campos e imágenes compartidos por las pantallas de INSTA+. */
public final class EstiloInsta {

    public static final Color ROJO = new Color(214, 48, 72);
    public static final Color ROJO_SUAVE = new Color(253, 235, 238);
    public static final Color VERDE = new Color(28, 135, 78);
    public static final Color VERDE_SUAVE = new Color(230, 246, 236);
    public static final Color AZUL = new Color(38, 104, 196);
    public static final Color AZUL_SUAVE = new Color(233, 241, 252);
    public static final Color AMBAR = new Color(150, 96, 0);
    public static final Color AMBAR_SUAVE = new Color(255, 245, 224);
    public static final Color LIKE_ACTIVO = new Color(233, 38, 86);
    public static final Color LIKE_INACTIVO = new Color(180, 180, 188);

    private static final Map<String, ImageIcon> CACHE_IMAGENES = new LinkedHashMap<String, ImageIcon>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> mayor) {
            return size() > 80;
        }
    };

    private EstiloInsta() {
    }

    /** Mezcla dos colores (t = 0 → a, t = 1 → b). Sirve para derivar tonos que funcionen con el tema. */
    public static Color mezclar(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Botón plano tipo "enlace". */
    public static JButton botonTexto(String texto, Color color, int tamano, boolean negrita) {
        JButton b = new JButton(texto);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(color);
        b.setFont(new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, tamano));
        b.setMargin(new Insets(2, 4, 2, 4));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Botón con contorno redondeado (acción secundaria). */
    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? mezclar(TemaUI.SUPERFICIE, TemaUI.TEXTO, 0.06) : TemaUI.SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(TemaUI.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setForeground(TemaUI.TEXTO);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Campo de texto con borde suave y relleno interno. */
    public static JTextField campoTexto(int columnas) {
        JTextField t = new JTextField(columnas);
        estilizarCampo(t);
        return t;
    }

    public static void estilizarCampo(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBorder(new CompoundBorder(new LineBorder(TemaUI.BORDE, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static JLabel etiqueta(String texto, int tamano, boolean negrita, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, tamano));
        l.setForeground(color);
        return l;
    }

    /** Área de texto de solo lectura que se ajusta al ancho (para captions, comentarios, descripciones). */
    public static JTextArea textoAjustable(String texto, int tamano, Color color) {
        JTextArea t = new JTextArea(texto == null ? "" : texto);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setBorder(null);
        t.setFont(new Font("SansSerif", Font.PLAIN, tamano));
        t.setForeground(color);
        return t;
    }

    /** Panel que en un BoxLayout vertical nunca se estira más alto de lo que necesita. */
    public static JPanel filaAjustada(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    public static String escaparHtml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\n': sb.append("<br>"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        String limpio = s.replace('\n', ' ').trim();
        return limpio.length() <= max ? limpio : limpio.substring(0, Math.max(0, max - 1)) + "…";
    }

    // ------------------------------------------------------------------ Imágenes

    private static BufferedImage leer(File f) {
        try {
            return ImageIO.read(f);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static boolean rutaValida(String ruta) {
        return ruta != null && !ruta.isBlank() && new File(ruta).isFile();
    }

    /** Escala la imagen para que quepa en maxW x maxH sin deformarla. Devuelve null si no se puede leer. */
    public static ImageIcon imagenAjustada(String ruta, int maxW, int maxH) {
        if (!rutaValida(ruta)) {
            return null;
        }
        File f = new File(ruta);
        String clave = "A|" + f.getAbsolutePath() + "|" + f.lastModified() + "|" + maxW + "x" + maxH;
        synchronized (CACHE_IMAGENES) {
            ImageIcon c = CACHE_IMAGENES.get(clave);
            if (c != null) {
                return c;
            }
        }
        BufferedImage img = leer(f);
        if (img == null) {
            return null;
        }
        double escala = Math.min(1.0, Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight()));
        int w = Math.max(1, (int) Math.round(img.getWidth() * escala));
        int h = Math.max(1, (int) Math.round(img.getHeight() * escala));
        ImageIcon icono = new ImageIcon(dibujar(img, w, h, 0, 0, w, h));
        synchronized (CACHE_IMAGENES) {
            CACHE_IMAGENES.put(clave, icono);
        }
        return icono;
    }

    /** Miniatura cuadrada recortada al centro (como la cuadrícula de Instagram). */
    public static ImageIcon miniaturaCuadrada(String ruta, int lado) {
        if (!rutaValida(ruta)) {
            return null;
        }
        File f = new File(ruta);
        String clave = "C|" + f.getAbsolutePath() + "|" + f.lastModified() + "|" + lado;
        synchronized (CACHE_IMAGENES) {
            ImageIcon c = CACHE_IMAGENES.get(clave);
            if (c != null) {
                return c;
            }
        }
        BufferedImage img = leer(f);
        if (img == null) {
            return null;
        }
        double escala = Math.max((double) lado / img.getWidth(), (double) lado / img.getHeight());
        int w = (int) Math.ceil(img.getWidth() * escala);
        int h = (int) Math.ceil(img.getHeight() * escala);
        ImageIcon icono = new ImageIcon(dibujar(img, lado, lado, (lado - w) / 2, (lado - h) / 2, w, h));
        synchronized (CACHE_IMAGENES) {
            CACHE_IMAGENES.put(clave, icono);
        }
        return icono;
    }

    private static BufferedImage dibujar(BufferedImage src, int lienzoW, int lienzoH, int x, int y, int w, int h) {
        BufferedImage out = new BufferedImage(lienzoW, lienzoH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, x, y, w, h, null);
        g.dispose();
        return out;
    }

    /** Panel redondeado con fondo de color (tarjetas, burbujas, avisos). */
    public static class PanelRedondeado extends JPanel {
        private Color fondo;
        private Color borde;
        private final int radio;

        public PanelRedondeado(LayoutManager layout, Color fondo, Color borde, int radio) {
            super(layout);
            this.fondo = fondo;
            this.borde = borde;
            this.radio = radio;
            setOpaque(false);
        }

        public void setColores(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (fondo != null) {
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            }
            if (borde != null) {
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}