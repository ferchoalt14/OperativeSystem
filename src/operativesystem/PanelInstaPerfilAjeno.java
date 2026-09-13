package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class PanelInstaPerfilAjeno extends JPanel {

    private final InstaControlador controlador;

    private String usernameVisitado;
    private String seccionOrigen = "PERFIL";

    private final JLabel lblAvatar, lblNombreCompleto, lblUsername;
    private final JLabel lblPosts, lblFollowers, lblFollowing;
    private final JButton btnSeguir;

    public PanelInstaPerfilAjeno(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(18, 26, 24, 26));

        JButton btnVolver = new JButton("←  Volver");
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setForeground(TemaUI.ACCENT_OSCURO);
        btnVolver.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> controlador.mostrarSeccion(seccionOrigen));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.setBorder(new EmptyBorder(16, 0, 0, 0));

        lblAvatar = new JLabel();
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblNombreCompleto = new JLabel("", SwingConstants.CENTER);
        lblNombreCompleto.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNombreCompleto.setForeground(TemaUI.TEXTO);
        lblNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsername = new JLabel("", SwingConstants.CENTER);
        lblUsername.setForeground(TemaUI.TEXTO_SUAVE);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPosts = crearEtiquetaStat();
        lblFollowers = crearEtiquetaStat();
        lblFollowing = crearEtiquetaStat();

        JPanel panelStats = new JPanel(new GridLayout(1, 3, 20, 0));
        panelStats.setOpaque(false);
        panelStats.add(envolverStat(lblPosts, "Publicaciones"));
        panelStats.add(envolverStat(lblFollowers, "Seguidores"));
        panelStats.add(envolverStat(lblFollowing, "Siguiendo"));
        panelStats.setMaximumSize(new Dimension(340, 60));
        panelStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnSeguir = TemaUI.crearBotonPrimario("Seguir");
        btnSeguir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSeguir.addActionListener(e -> alternarSeguimiento());

        JLabel lblSinPosts = new JLabel("<html><center>📷<br><br>Aún no hay publicaciones.</center></html>",
                SwingConstants.CENTER);
        lblSinPosts.setForeground(TemaUI.TEXTO_SUAVE);
        lblSinPosts.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSinPosts.setBorder(new EmptyBorder(30, 0, 0, 0));

        panelEncabezado.add(lblAvatar);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblNombreCompleto);
        panelEncabezado.add(lblUsername);
        panelEncabezado.add(Box.createVerticalStrut(14));
        panelEncabezado.add(panelStats);
        panelEncabezado.add(Box.createVerticalStrut(16));
        panelEncabezado.add(btnSeguir);
        panelEncabezado.add(lblSinPosts);

        add(btnVolver, BorderLayout.NORTH);
        add(panelEncabezado, BorderLayout.CENTER);
    }

    private JLabel crearEtiquetaStat() {
        JLabel lbl = new JLabel("0", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        lbl.setForeground(TemaUI.TEXTO);
        return lbl;
    }

    private JPanel envolverStat(JLabel valor, String etiqueta) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lblEtiqueta = new JLabel(etiqueta, SwingConstants.CENTER);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEtiqueta.setForeground(TemaUI.TEXTO_SUAVE);
        valor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(valor);
        panel.add(lblEtiqueta);
        return panel;
    }

    public void abrir(String username, String seccionOrigen) {
        this.usernameVisitado = username;
        this.seccionOrigen = seccionOrigen;
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        try {
            UsuarioInsta perfil = GestorInstaPlus.buscarPorUsername(username);
            if (perfil == null) {
                JOptionPane.showMessageDialog(this, "No se encontró la cuenta @" + username,
                        "Cuenta no disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            lblNombreCompleto.setText(perfil.getNombreCompleto());
            lblUsername.setText("@" + perfil.getUsername());

            String ruta = perfil.getRutaFotoPerfil();
            if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
                lblAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
            } else {
                String nombre = perfil.getNombreCompleto();
                String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
                lblAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(username.hashCode()), 96));
            }

            List<String> followers = GestorInstaPlus.obtenerFollowers(username);
            List<String> following = GestorInstaPlus.obtenerFollowing(username);
            int followersParaMostrar = GestorInstaPlus.contarFollowersParaMostrar(username);
            int posts = GestorPosts.contarPosts(username);

            lblPosts.setText(String.valueOf(posts));
            lblFollowers.setText(String.valueOf(followersParaMostrar));
            lblFollowing.setText(String.valueOf(following.size()));

            boolean loSigo = followers.contains(actual.getUsername());
            btnSeguir.setText(loSigo ? "Dejar de seguir" : "Seguir");

            controlador.mostrarSeccion("PERFIL_AJENO");

        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el perfil de @" + username,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarSeguimiento() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (usernameVisitado == null || actual == null) {
            return;
        }
        try {
            List<String> followers = GestorInstaPlus.obtenerFollowers(usernameVisitado);
            boolean loSigo = followers.contains(actual.getUsername());

            if (loSigo) {
                GestorInstaPlus.dejarDeSeguir(actual.getUsername(), usernameVisitado);
            } else {
                GestorInstaPlus.seguirCuenta(actual.getUsername(), usernameVisitado);
            }

            abrir(usernameVisitado, seccionOrigen);
            controlador.refrescarPerfilPropio();
            controlador.refrescarFeed();
            controlador.refrescarBusqueda();

        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el seguimiento: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}