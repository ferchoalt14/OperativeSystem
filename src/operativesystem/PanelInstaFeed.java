package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;


public class PanelInstaFeed extends JPanel {

    private final InstaControlador controlador;
    private final JPanel panelPosts;

    public PanelInstaFeed(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(20, 26, 20, 26));

        JLabel lblTitulo = new JLabel("Inicio");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        lblTitulo.setBorder(new EmptyBorder(0, 4, 14, 0));

        panelPosts = new PanelDesplazable(new BorderLayout());
        panelPosts.setOpaque(false);
        panelPosts.setLayout(new BoxLayout(panelPosts, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelPosts);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Vuelve a cargar el feed desde disco y redibuja las tarjetas de posts. */
    public void refrescar() {
        panelPosts.removeAll();
        UsuarioInsta actual = controlador.getUsuarioActual();

        if (actual == null) {
            panelPosts.revalidate();
            panelPosts.repaint();
            return;
        }

        ListaEnlazada<Post> feed;
        try {
            feed = GestorPosts.obtenerFeed(actual.getUsername());
        } catch (ArchivoCorruptoException ex) {
            panelPosts.add(new JLabel("No se pudo cargar el feed."));
            panelPosts.revalidate();
            panelPosts.repaint();
            return;
        }

        if (feed.estaVacia()) {
            JLabel lblVacio = new JLabel("<html><center>📷<br><br>Aún no hay publicaciones en tu feed.<br>"
                    + "Sigue cuentas para empezar a verlas aquí.</center></html>",
                    SwingConstants.CENTER);
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblVacio.setBorder(new EmptyBorder(40, 0, 0, 0));
            panelPosts.add(lblVacio);
        } else {
            for (Post post : feed) {
                panelPosts.add(crearTarjetaPost(post));
                panelPosts.add(Box.createVerticalStrut(14));
            }
        }

        panelPosts.revalidate();
        panelPosts.repaint();
    }

    private JPanel crearTarjetaPost(Post post) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(TemaUI.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUI.BORDE, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(false);
        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(post.getUsernameAutor(), 34));
        JButton btnUsername = new JButton("@" + post.getUsernameAutor());
        btnUsername.setContentAreaFilled(false);
        btnUsername.setBorderPainted(false);
        btnUsername.setFocusPainted(false);
        btnUsername.setForeground(TemaUI.TEXTO);
        btnUsername.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUsername.addActionListener(e -> controlador.abrirPerfilAjeno(post.getUsernameAutor(), "FEED"));
        JLabel lblFecha = new JLabel(post.getFechaTexto());
        lblFecha.setForeground(TemaUI.TEXTO_SUAVE);
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JPanel panelNombreFecha = new JPanel(new GridLayout(2, 1));
        panelNombreFecha.setOpaque(false);
        panelNombreFecha.add(btnUsername);
        panelNombreFecha.add(lblFecha);

        encabezado.add(lblAvatar, BorderLayout.WEST);
        encabezado.add(panelNombreFecha, BorderLayout.CENTER);

        JLabel lblImagen;
        String ruta = post.getRutaImagen();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            ImageIcon icono = new ImageIcon(new ImageIcon(ruta).getImage()
                    .getScaledInstance(360, 280, Image.SCALE_SMOOTH));
            lblImagen = new JLabel(icono);
        } else {
            lblImagen = new JLabel("🖼", SwingConstants.CENTER);
            lblImagen.setFont(lblImagen.getFont().deriveFont(48f));
            lblImagen.setPreferredSize(new Dimension(360, 200));
            lblImagen.setOpaque(true);
            lblImagen.setBackground(TemaUI.FONDO);
        }
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea txtCaption = new JTextArea(post.getTexto());
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);
        txtCaption.setEditable(false);
        txtCaption.setOpaque(false);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtCaption.setForeground(TemaUI.TEXTO);

        JLabel lblLikes = new JLabel("❤ " + post.getLikes() + " likes");
        lblLikes.setForeground(TemaUI.TEXTO_SUAVE);
        lblLikes.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        panelInferior.add(txtCaption, BorderLayout.CENTER);
        panelInferior.add(lblLikes, BorderLayout.SOUTH);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(lblImagen, BorderLayout.CENTER);
        tarjeta.add(panelInferior, BorderLayout.SOUTH);

        return tarjeta;
    }
}