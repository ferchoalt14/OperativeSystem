package operativesystem;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Arc2D;
import java.io.File;
import java.util.regex.Pattern;


public final class TemaUI {

    private TemaUI() {
    }


    public static final Color ACCENT = new Color(0, 120, 212);      
    public static final Color ACCENT_OSCURO = new Color(0, 90, 158);   
    public static final Color ACCENT_CLARO = new Color(158, 205, 236); 
    public static final Color ACCENT_HOVER = new Color(16, 110, 190);  

    public static final Color FONDO = new Color(243, 243, 243);       
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(32, 31, 30);
    public static final Color TEXTO_SUAVE = new Color(96, 94, 92);
    public static final Color BORDE = new Color(225, 225, 225);

    public static final Color LOGIN_GRAD_INICIO = new Color(0, 137, 209);
    public static final Color LOGIN_GRAD_FIN = new Color(0, 45, 90);

    public static final Color BARRA_TITULO = new Color(245, 245, 245);
    public static final Color BARRA_TITULO_TEXTO = TEXTO;
    public static final Color BOTON_CERRAR_HOVER = new Color(232, 17, 35);
    public static final Color TASKBAR_FONDO = new Color(32, 32, 32, 235);

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


    public static final Pattern PATRON_PASSWORD_SEGURA =
            Pattern.compile("^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$");
    public static final String REQUISITOS_PASSWORD =
            "Mínimo 8 caracteres, con al menos 1 mayúscula y 1 carácter especial (@, #, $, %, !, etc.)";


    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {

        }

        UIManager.put("control", FONDO);
        UIManager.put("nimbusBase", ACCENT);
        UIManager.put("nimbusBlueGrey", new Color(225, 225, 225));
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

    
    public static Icon crearIconoPersonaGenerica(int diametro) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 165, 170));
                g2.fillOval(x, y, diametro, diametro);
                g2.setClip(new Ellipse2D.Float(x, y, diametro, diametro));
                g2.setColor(Color.WHITE);
                int cabezaD = (int) (diametro * 0.38);
                int cabezaX = x + (diametro - cabezaD) / 2;
                int cabezaY = y + (int) (diametro * 0.16);
                g2.fillOval(cabezaX, cabezaY, cabezaD, cabezaD);
                int cuerpoD = (int) (diametro * 0.9);
                int cuerpoX = x + (diametro - cuerpoD) / 2;
                int cuerpoY = y + (int) (diametro * 0.62);
                g2.fill(new Arc2D.Float(cuerpoX, cuerpoY, cuerpoD, cuerpoD, 0, 180, Arc2D.CHORD));
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

    /**
     * Ícono circular a partir de una imagen empaquetada en el paquete "images" del proyecto
     * (Source Packages / images / nombreRecurso). Si el recurso no se encuentra, se usa un
     * círculo gris de reserva para no romper la interfaz.
     */
    public static Icon crearIconoCircularDeRecurso(String nombreRecurso, int diametro) {
        java.net.URL url = TemaUI.class.getResource("/images/" + nombreRecurso);
        Image imagen = (url != null) ? new ImageIcon(url).getImage() : null;
        if (url == null) {
            System.err.println("[UI] Recurso no encontrado en classpath: /images/" + nombreRecurso);
        }
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Float(x, y, diametro, diametro));
                if (imagen != null) {
                    g2.drawImage(imagen, x, y, diametro, diametro, null);
                } else {
                    g2.setColor(new Color(160, 165, 170));
                    g2.fillOval(x, y, diametro, diametro);
                }
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

    /** Botón de app con el logo real (imagen) en vez del círculo de color con siglas. */
    public static JButton crearBotonAppConImagen(String nombre, String nombreRecursoImagen) {
        JButton boton = new JButton(nombre);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setIcon(crearIconoCircularDeRecurso(nombreRecursoImagen, 56));
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setForeground(TEXTO);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static JButton crearBotonAppConImagen(String nombre, String nombreRecursoImagen, ActionListener accion) {
        JButton boton = crearBotonAppConImagen(nombre, nombreRecursoImagen);
        boton.addActionListener(accion);
        return boton;
    }


    public static JPanel crearCampoPassword(JPasswordField campo) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);

        char echoOriginal = campo.getEchoChar() != 0 ? campo.getEchoChar() : '•';
        campo.setEchoChar(echoOriginal);

        JButton btnToggle = new JButton("Ver");
        btnToggle.setFocusable(false);
        btnToggle.setMargin(new Insets(2, 8, 2, 8));
        btnToggle.setFont(btnToggle.getFont().deriveFont(11f));
        btnToggle.setToolTipText("Mostrar contraseña");
        btnToggle.addActionListener(e -> {
            boolean oculta = campo.getEchoChar() != 0;
            if (oculta) {
                campo.setEchoChar((char) 0);
                btnToggle.setText("Ocultar");
                btnToggle.setToolTipText("Ocultar contraseña");
            } else {
                campo.setEchoChar(echoOriginal);
                btnToggle.setText("Ver");
                btnToggle.setToolTipText("Mostrar contraseña");
            }
        });

        panel.add(campo, BorderLayout.CENTER);
        panel.add(btnToggle, BorderLayout.EAST);
        return panel;
    }

    public static JButton crearBotonPrimario(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color relleno = getModel().isPressed() ? ACCENT_OSCURO
                        : (getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.setColor(relleno);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorderPainted(false);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

   
    public static JButton crearBotonCircular(String texto, int diametro, Color colorFondo, Color colorTexto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color relleno = getModel().isPressed()
                        ? colorFondo.darker()
                        : (getModel().isRollover() ? brillar(colorFondo) : colorFondo);
                g2.setColor(relleno);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setPreferredSize(new Dimension(diametro, diametro));
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setForeground(colorTexto);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, diametro * 0.4f));
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private static Color brillar(Color c) {
        int r = Math.min(255, c.getRed() + 25);
        int gg = Math.min(255, c.getGreen() + 25);
        int b = Math.min(255, c.getBlue() + 25);
        return new Color(r, gg, b, c.getAlpha());
    }

  
    public static JPanel crearFondoDegradado(Color inicio, Color fin) {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, inicio, getWidth(), getHeight(), fin);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        return panel;
    }

  
    public static JTextField crearCampoTexto(String placeholder, int columnas) {
        return new CampoTextoRedondeado(placeholder, columnas);
    }

    public static JPasswordField crearCampoPasswordEstilizado(String placeholder, int columnas) {
        return new CampoPasswordRedondeado(placeholder, columnas);
    }

    private static void dibujarFondoRedondeado(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
        if (c.hasFocus()) {
            g2.setColor(ACCENT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, c.getWidth() - 2, c.getHeight() - 2, 8, 8);
        }
        g2.dispose();
    }

    private static void dibujarPlaceholder(Graphics g, JTextField campo, String placeholder, int longitudTexto) {
        if (longitudTexto != 0 || campo.hasFocus() || placeholder == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(TEXTO_SUAVE);
        g2.setFont(campo.getFont());
        FontMetrics fm = g2.getFontMetrics();
        int ty = (campo.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(placeholder, campo.getInsets().left, ty);
        g2.dispose();
    }

    private static final class CampoTextoRedondeado extends JTextField {
        private final String placeholder;

        CampoTextoRedondeado(String placeholder, int columnas) {
            super(columnas);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setForeground(TEXTO);
            setCaretColor(TEXTO);
            setSelectionColor(ACCENT_CLARO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            dibujarFondoRedondeado(g, this);
            super.paintComponent(g);
            dibujarPlaceholder(g, this, placeholder, getText().length());
        }
    }

    private static final class CampoPasswordRedondeado extends JPasswordField {
        private final String placeholder;

        CampoPasswordRedondeado(String placeholder, int columnas) {
            super(columnas);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            setFont(new Font("Segoe UI", Font.PLAIN, 16));
            setForeground(TEXTO);
            setCaretColor(TEXTO);
            setSelectionColor(ACCENT_CLARO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            dibujarFondoRedondeado(g, this);
            super.paintComponent(g);
            dibujarPlaceholder(g, this, placeholder, getPassword().length);
        }
    }
}