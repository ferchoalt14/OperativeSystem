package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PanelInstaPerfil extends JPanel {

    private final InstaControlador controlador;

    private final JLabel lblAvatar;
    private final JLabel lblNombreCompleto;
    private final JLabel lblUsername;
    private final JLabel lblPosts, lblFollowers, lblFollowing;
    private final JLabel lblDatos;
    private final JPanel panelSiguiendoLista;

    public PanelInstaPerfil(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(26, 28, 20, 28));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));

        lblAvatar = new JLabel();
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblNombreCompleto = new JLabel("", SwingConstants.CENTER);
        lblNombreCompleto.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNombreCompleto.setForeground(TemaUI.TEXTO);
        lblNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsername = new JLabel("", SwingConstants.CENTER);
        lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 13));
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

        lblDatos = new JLabel("", SwingConstants.CENTER);
        lblDatos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDatos.setForeground(TemaUI.TEXTO_SUAVE);
        lblDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnElegirFoto = new JButton("Elegir foto de perfil");
        btnElegirFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnElegirFoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnElegirFoto.addActionListener(e -> elegirFotoPerfil());

        panelEncabezado.add(lblAvatar);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblNombreCompleto);
        panelEncabezado.add(lblUsername);
        panelEncabezado.add(Box.createVerticalStrut(14));
        panelEncabezado.add(panelStats);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblDatos);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(btnElegirFoto);
        panelEncabezado.add(Box.createVerticalStrut(20));

        JLabel lblSiguiendoTitulo = new JLabel("Cuentas que sigues");
        lblSiguiendoTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSiguiendoTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        lblSiguiendoTitulo.setBorder(new EmptyBorder(0, 2, 8, 0));

        panelSiguiendoLista = new JPanel();
        panelSiguiendoLista.setOpaque(false);
        panelSiguiendoLista.setLayout(new BoxLayout(panelSiguiendoLista, BoxLayout.Y_AXIS));

        JPanel panelSiguiendoContenedor = new JPanel(new BorderLayout());
        panelSiguiendoContenedor.setOpaque(false);
        panelSiguiendoContenedor.add(lblSiguiendoTitulo, BorderLayout.NORTH);
        panelSiguiendoContenedor.add(panelSiguiendoLista, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(panelSiguiendoContenedor);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        add(panelEncabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
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

    public void refrescar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        lblNombreCompleto.setText(actual.getNombreCompleto());
        lblUsername.setText("@" + actual.getUsername());

        String ruta = actual.getRutaFotoPerfil();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            lblAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
        } else {
            String nombre = actual.getNombreCompleto();
            String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
            lblAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(1), 96));
        }

        List<String> following;
        int followersParaMostrar;
        try {
            following = GestorInstaPlus.obtenerFollowing(actual.getUsername());
            followersParaMostrar = GestorInstaPlus.contarFollowersParaMostrar(actual.getUsername());
        } catch (ArchivoCorruptoException ex) {
            following = new ArrayList<>();
            followersParaMostrar = 0;
        }
        int publicaciones = GestorPosts.contarPosts(actual.getUsername());

        lblPosts.setText(String.valueOf(publicaciones));
        lblFollowers.setText(String.valueOf(followersParaMostrar));
        lblFollowing.setText(String.valueOf(following.size()));

        lblDatos.setText("<html>" + actual.getEdad() + " años &nbsp;•&nbsp; "
                + (actual.getGenero() == 'F' ? "Femenino" : "Masculino")
                + " &nbsp;•&nbsp; Se unió el " + actual.getFechaRegistroTexto() + "</html>");

        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String u : following) {
            lista.agregar(u);
        }

        panelSiguiendoLista.removeAll();
        if (lista.estaVacia()) {
            JLabel lblVacio = new JLabel("Aún no sigues a nadie.");
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            panelSiguiendoLista.add(lblVacio);
        } else {
            for (String u : lista) {
                panelSiguiendoLista.add(crearFilaSiguiendo(u));
                panelSiguiendoLista.add(Box.createVerticalStrut(6));
            }
        }
        panelSiguiendoLista.revalidate();
        panelSiguiendoLista.repaint();
    }

    private JPanel crearFilaSiguiendo(String username) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lblAvatarFila = new JLabel(TemaUI.crearIconoCircular(
                username.substring(0, 1).toUpperCase(), TemaUI.colorApp(username.hashCode()), 32));
        fila.add(lblAvatarFila, BorderLayout.WEST);

        JButton btnUsername = new JButton("@" + username);
        btnUsername.setContentAreaFilled(false);
        btnUsername.setBorderPainted(false);
        btnUsername.setFocusPainted(false);
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setForeground(TemaUI.TEXTO);
        btnUsername.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUsername.addActionListener(e -> controlador.abrirPerfilAjeno(username, "PERFIL"));
        fila.add(btnUsername, BorderLayout.CENTER);

        return fila;
    }

    private void elegirFotoPerfil() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            actual.setRutaFotoPerfil(archivo.getAbsolutePath());
            try {
                GestorInstaPlus.actualizarUsuario(actual);
                refrescar();
            } catch (ArchivoCorruptoException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo guardar la foto de perfil: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}