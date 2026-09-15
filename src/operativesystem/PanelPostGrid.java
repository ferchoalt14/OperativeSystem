package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Cuadrícula de publicaciones (perfil propio / perfil ajeno). */
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
            JLabel lblVacio = new JLabel("<html><center><font size='6'>📷</font><br><br>"
                    + EstiloInsta.escaparHtml(mensajeVacio) + "</center></html>", SwingConstants.CENTER);
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
        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(TAMANO_MINIATURA, TAMANO_MINIATURA));
        lbl.setOpaque(true);
        lbl.setBackground(EstiloInsta.mezclar(TemaUI.FONDO, TemaUI.TEXTO, 0.05));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        lbl.setToolTipText(String.format("♥ %,d   💬 %d", post.getLikes(), post.getComentarios().size()));

        ImageIcon mini = EstiloInsta.miniaturaCuadrada(post.getRutaImagen(), TAMANO_MINIATURA);
        if (mini != null) {
            lbl.setIcon(mini);
        } else {
            String texto = post.getTexto() == null ? "" : post.getTexto();
            if (texto.isBlank()) {
                lbl.setText("🖼");
                lbl.setFont(lbl.getFont().deriveFont(36f));
            } else {
                lbl.setText("<html><center>" + EstiloInsta.escaparHtml(EstiloInsta.recortar(texto, 60)) + "</center></html>");
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TemaUI.BORDE), new EmptyBorder(8, 8, 8, 8)));
            }
            lbl.setForeground(TemaUI.TEXTO_SUAVE);
        }

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controlador.abrirPost(post, alCambiar);
            }
        });
        return lbl;
    }
}