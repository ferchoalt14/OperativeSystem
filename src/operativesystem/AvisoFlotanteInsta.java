package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Notificación que aparece DENTRO de la ventana de INSTA+ (esquina superior derecha) y se oculta sola.
 * No es una ventana aparte: se dibuja encima de las secciones.
 */
public class AvisoFlotanteInsta extends EstiloInsta.PanelRedondeado {

    private static final int ANCHO = 330;

    private final JLabel lblIcono = new JLabel();
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblTexto = new JLabel();
    private final Timer timerOcultar;
    private Runnable alHacerClic;

    public AvisoFlotanteInsta() {
        super(new BorderLayout(10, 0), TemaUI.SUPERFICIE, TemaUI.BORDE, 16);
        setBorder(new EmptyBorder(10, 12, 10, 8));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblIcono.setVerticalAlignment(SwingConstants.TOP);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitulo.setForeground(TemaUI.TEXTO);
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTexto.setForeground(TemaUI.TEXTO_SUAVE);

        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(lblTitulo, BorderLayout.NORTH);
        textos.add(lblTexto, BorderLayout.CENTER);

        JButton btnCerrar = EstiloInsta.botonTexto("✕", TemaUI.TEXTO_SUAVE, 12, false);
        btnCerrar.setVerticalAlignment(SwingConstants.TOP);
        btnCerrar.addActionListener(e -> ocultar());

        add(lblIcono, BorderLayout.WEST);
        add(textos, BorderLayout.CENTER);
        add(btnCerrar, BorderLayout.EAST);

        MouseAdapter clic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Runnable accion = alHacerClic;
                ocultar();
                if (accion != null) {
                    accion.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                timerOcultar.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isVisible()) {
                    timerOcultar.restart();
                }
            }
        };
        addMouseListener(clic);
        textos.addMouseListener(clic);
        lblTexto.addMouseListener(clic);
        lblTitulo.addMouseListener(clic);

        timerOcultar = new Timer(5000, e -> ocultar());
        timerOcultar.setRepeats(false);
        setVisible(false);
    }

    public void mostrar(Icon icono, String titulo, String texto, Runnable alHacerClic) {
        this.alHacerClic = alHacerClic;
        lblIcono.setIcon(icono);
        lblIcono.setText(icono == null ? "🔔" : null);
        lblTitulo.setText(titulo);
        lblTexto.setText("<html><body style='width:220px'>" + EstiloInsta.escaparHtml(texto)
                + (alHacerClic != null ? "<br><font color='#8a8a8a'>Toca para ver</font>" : "") + "</body></html>");
        setVisible(true);
        Container padre = getParent();
        if (padre != null) {
            padre.revalidate();
            padre.repaint();
        }
        timerOcultar.restart();
    }

    public void ocultar() {
        timerOcultar.stop();
        alHacerClic = null;
        if (isVisible()) {
            setVisible(false);
            Container padre = getParent();
            if (padre != null) {
                padre.repaint();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(ANCHO, d.height);
    }

    /** Contenedor que dibuja el aviso flotando encima del contenido. */
    public static class Capa extends JPanel {
        private final JComponent contenido;
        private final AvisoFlotanteInsta aviso;

        public Capa(JComponent contenido, AvisoFlotanteInsta aviso) {
            super(null);
            this.contenido = contenido;
            this.aviso = aviso;
            setOpaque(false);
            add(aviso);     // índice 0 = se pinta encima
            add(contenido);
        }

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }

        @Override
        public void doLayout() {
            contenido.setBounds(0, 0, getWidth(), getHeight());
            Dimension d = aviso.getPreferredSize();
            int w = Math.min(d.width, Math.max(120, getWidth() - 28));
            aviso.setBounds(getWidth() - w - 14, 14, w, d.height);
        }

        @Override
        public Dimension getPreferredSize() {
            return contenido.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return contenido.getMinimumSize();
        }
    }
}
