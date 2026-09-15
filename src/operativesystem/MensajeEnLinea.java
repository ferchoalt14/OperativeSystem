package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Mensaje que aparece dentro de la misma pantalla (error / éxito / info), en lugar de un JOptionPane.
 * Uso: mensaje.error("..."), mensaje.exito("..."), mensaje.info("..."), mensaje.ocultar().
 */
public class MensajeEnLinea extends EstiloInsta.PanelRedondeado {

    private final JLabel lblIcono = new JLabel();
    private final JLabel lblTexto = new JLabel();
    private final Timer timerOcultar;

    public MensajeEnLinea() {
        super(new BorderLayout(8, 0), EstiloInsta.ROJO_SUAVE, null, 12);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        lblIcono.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblIcono.setVerticalAlignment(SwingConstants.TOP);
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(lblIcono, BorderLayout.WEST);
        add(lblTexto, BorderLayout.CENTER);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setVisible(false);

        timerOcultar = new Timer(4500, e -> ocultar());
        timerOcultar.setRepeats(false);
    }

    public void error(String texto) {
        mostrar("⚠", texto, EstiloInsta.ROJO, EstiloInsta.ROJO_SUAVE, false);
    }

    public void exito(String texto) {
        mostrar("✓", texto, EstiloInsta.VERDE, EstiloInsta.VERDE_SUAVE, true);
    }

    public void info(String texto) {
        mostrar("ℹ", texto, EstiloInsta.AZUL, EstiloInsta.AZUL_SUAVE, false);
    }

    public void ocultar() {
        timerOcultar.stop();
        if (isVisible()) {
            setVisible(false);
            refrescarContenedor();
        }
    }

    private void mostrar(String icono, String texto, Color color, Color fondo, boolean autoOcultar) {
        timerOcultar.stop();
        lblIcono.setText(icono);
        lblIcono.setForeground(color);
        lblTexto.setForeground(color.darker());
        lblTexto.setText("<html>" + EstiloInsta.escaparHtml(texto) + "</html>");
        setColores(fondo, null);
        setVisible(true);
        refrescarContenedor();
        if (autoOcultar) {
            timerOcultar.start();
        }
    }

    private void refrescarContenedor() {
        Container padre = getParent();
        if (padre != null) {
            padre.revalidate();
            padre.repaint();
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}