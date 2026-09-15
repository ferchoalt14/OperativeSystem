package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Notificación flotante ("toast") que aparece abajo a la derecha de la pantalla,
 * por encima de cualquier otra ventana/aplicación, y se cierra sola tras unos segundos.
 */
public class ToastNotificacion {

    private static final int DURACION_MS = 5000;
    private static final int ANCHO = 320;
    private static final int ALTO = 72;
    private static int desplazamientoActivo = 0;

    public static void mostrar(String titulo, String mensaje, Runnable alHacerClic) {
        SwingUtilities.invokeLater(() -> {
            JWindow ventana = new JWindow();
            ventana.setAlwaysOnTop(true);
            ventana.setFocusableWindowState(false);

            JPanel contenido = new JPanel(new BorderLayout(10, 0));
            contenido.setBackground(TemaUI.SUPERFICIE);
            contenido.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TemaUI.ACCENT_OSCURO, 1, true),
                    new EmptyBorder(10, 12, 10, 12)));

            JLabel lblIcono = new JLabel("✉");
            lblIcono.setFont(lblIcono.getFont().deriveFont(22f));

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblTitulo.setForeground(TemaUI.TEXTO);

            JLabel lblMensaje = new JLabel("<html><body style='width: 210px'>" + mensaje + "</body></html>");
            lblMensaje.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblMensaje.setForeground(TemaUI.TEXTO_SUAVE);

            JPanel panelTextos = new JPanel();
            panelTextos.setOpaque(false);
            panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
            panelTextos.add(lblTitulo);
            panelTextos.add(lblMensaje);

            contenido.add(lblIcono, BorderLayout.WEST);
            contenido.add(panelTextos, BorderLayout.CENTER);
            contenido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            ventana.add(contenido);
            ventana.setSize(ANCHO, ALTO);

            Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            int y = pantalla.y + pantalla.height - ALTO - 20 - desplazamientoActivo;
            int x = pantalla.x + pantalla.width - ANCHO - 20;
            ventana.setLocation(x, y);
            desplazamientoActivo += ALTO + 10;

            contenido.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    ventana.dispose();
                    desplazamientoActivo = Math.max(0, desplazamientoActivo - (ALTO + 10));
                    if (alHacerClic != null) {
                        alHacerClic.run();
                    }
                }
            });

            ventana.setVisible(true);

            Timer temporizador = new Timer(DURACION_MS, e -> {
                ventana.dispose();
                desplazamientoActivo = Math.max(0, desplazamientoActivo - (ALTO + 10));
            });
            temporizador.setRepeats(false);
            temporizador.start();
        });
    }
}