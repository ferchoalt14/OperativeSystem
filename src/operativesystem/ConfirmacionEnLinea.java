package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Barra de confirmación que se muestra dentro de la pantalla (reemplaza JOptionPane.showConfirmDialog).
 * Uso: confirmacion.preguntar("¿Seguro…?", "Eliminar", () -> { ... });
 */
public class ConfirmacionEnLinea extends EstiloInsta.PanelRedondeado {

    private final JLabel lblPregunta = new JLabel();
    private final JButton btnConfirmar;
    private Runnable accion;

    public ConfirmacionEnLinea() {
        super(new BorderLayout(10, 6), EstiloInsta.AMBAR_SUAVE, new Color(240, 214, 160), 14);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPregunta.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPregunta.setForeground(EstiloInsta.AMBAR.darker());

        btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setBackground(EstiloInsta.ROJO);
        btnConfirmar.setOpaque(true);
        btnConfirmar.setBorderPainted(false);
        btnConfirmar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnConfirmar.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> {
            Runnable a = accion;
            ocultar();
            if (a != null) {
                a.run();
            }
        });

        JButton btnCancelar = EstiloInsta.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> ocultar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnConfirmar);

        add(new JLabel("⚠") {{
            setForeground(EstiloInsta.AMBAR);
            setFont(new Font("SansSerif", Font.BOLD, 16));
        }}, BorderLayout.WEST);
        add(lblPregunta, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setVisible(false);
    }

    public void preguntar(String pregunta, String textoConfirmar, Runnable siConfirma) {
        this.accion = siConfirma;
        lblPregunta.setText("<html>" + EstiloInsta.escaparHtml(pregunta) + "</html>");
        btnConfirmar.setText(textoConfirmar);
        setVisible(true);
        refrescarContenedor();
    }

    /** Variante con botón de confirmación verde (acciones no destructivas). */
    public void preguntarPositivo(String pregunta, String textoConfirmar, Runnable siConfirma) {
        preguntar(pregunta, textoConfirmar, siConfirma);
        btnConfirmar.setBackground(EstiloInsta.VERDE);
    }

    public void ocultar() {
        accion = null;
        btnConfirmar.setBackground(EstiloInsta.ROJO);
        if (isVisible()) {
            setVisible(false);
            refrescarContenedor();
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