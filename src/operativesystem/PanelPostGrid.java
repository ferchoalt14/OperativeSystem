package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;


public class PanelPostGrid extends JPanel {

    private static final int COLUMNAS = 3;
    private static final int TAMANO_MINIATURA = 140;

    private final InstaControlador controlador;
    private final Runnable alCambiar;

    public PanelPostGrid(InstaControlador controlador, Runnable alCambiar) {
        this.controlador = controlador;
        this.alCambiar = alCambiar;
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    /** Vuelve a dibujar la cuadrícula con estos posts (ordenados como vengan en la lista). */
    public void mostrarPosts(List<Post> posts, String mensajeVacio) {
        removeAll();
        if (posts == null || posts.isEmpty()) {
            setLayout(new BorderLayout());
            JLabel lblVacio = new JLabel("<html><center>📷<br><br>" + mensajeVacio + "</center></html>",
                    SwingConstants.CENTER);
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            lblVacio.setBorder(new EmptyBorder(30, 0, 0, 0));
            add(lblVacio, BorderLayout.CENTER);
        } else {
            setLayout(new GridLayout(0, COLUMNAS, 6, 6));
            for (Post post : posts) {
                add(crearMiniatura(post));
            }
        }
        revalidate();
        repaint();
    }

    private JLabel crearMiniatura(Post post) {
        JLabel lbl = new JLabel();
        lbl.setPreferredSize(new Dimension(TAMANO_MINIATURA, TAMANO_MINIATURA));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(TemaUI.FONDO);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        lbl.setToolTipText("♥ " + post.getLikes() + " likes");

        String ruta = post.getRutaImagen();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            ImageIcon original = new ImageIcon(ruta);
            Image escalada = original.getImage().getScaledInstance(
                    TAMANO_MINIATURA, TAMANO_MINIATURA, Image.SCALE_SMOOTH);
            lbl.setIcon(new ImageIcon(escalada));
        } else {
            lbl.setText("🖼");
            lbl.setFont(lbl.getFont().deriveFont(36f));
            lbl.setForeground(TemaUI.TEXTO_SUAVE);
        }

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window ventana = SwingUtilities.getWindowAncestor(PanelPostGrid.this);
                Frame marco = (ventana instanceof Frame) ? (Frame) ventana : null;
                DialogoPost dialogo = new DialogoPost(marco, controlador, post, alCambiar);
                dialogo.setVisible(true);
            }
        });

        return lbl;
    }
}