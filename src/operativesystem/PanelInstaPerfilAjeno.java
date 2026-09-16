package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PanelInstaPerfilAjeno extends JPanel {

    private final InstaControlador controlador;

    private String usernameVisitado;
    private String seccionOrigen = "PERFIL";

    private final JLabel lblAvatar, lblNombreCompleto, lblUsername;
    private final JLabel lblDescripcion;
    private final JLabel lblPosts, lblFollowers, lblFollowing;
    private final JButton btnSeguir;

    private final PanelPostGrid panelPublicaciones;
    private final JPanel panelSeguidoresLista;
    private final JPanel panelSiguiendoLista;

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

        // Descripción (bio) del usuario visitado
        lblDescripcion = new JLabel("", SwingConstants.CENTER);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblDescripcion.setForeground(TemaUI.TEXTO);
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDescripcion.setVisible(false);

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

        JButton btnMensaje = new JButton("✉ Enviar mensaje");
        btnMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMensaje.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMensaje.addActionListener(e -> {
            if (usernameVisitado != null) {
                controlador.abrirChatConUsuario(usernameVisitado);
            }
        });

        JPanel panelBotonesPerfil = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panelBotonesPerfil.setOpaque(false);
        panelBotonesPerfil.add(btnSeguir);
        panelBotonesPerfil.add(btnMensaje);
        panelBotonesPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelEncabezado.add(lblAvatar);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblNombreCompleto);
        panelEncabezado.add(lblUsername);
        panelEncabezado.add(Box.createVerticalStrut(6));
        panelEncabezado.add(lblDescripcion);
        panelEncabezado.add(Box.createVerticalStrut(14));
        panelEncabezado.add(panelStats);
        panelEncabezado.add(Box.createVerticalStrut(16));
        panelEncabezado.add(panelBotonesPerfil);
        panelEncabezado.add(Box.createVerticalStrut(16));

        panelPublicaciones = new PanelPostGrid(controlador, () -> refrescarPublicaciones(usernameVisitado));

        panelSeguidoresLista = new PanelDesplazable(new BorderLayout());
        panelSeguidoresLista.setOpaque(false);
        panelSeguidoresLista.setLayout(new BoxLayout(panelSeguidoresLista, BoxLayout.Y_AXIS));

        panelSiguiendoLista = new PanelDesplazable(new BorderLayout());
        panelSiguiendoLista.setOpaque(false);
        panelSiguiendoLista.setLayout(new BoxLayout(panelSiguiendoLista, BoxLayout.Y_AXIS));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.addTab("Publicaciones", envolverEnScroll(panelPublicaciones));
        tabs.addTab("Seguidores", envolverEnScroll(panelSeguidoresLista));
        tabs.addTab("Siguiendo", envolverEnScroll(panelSiguiendoLista));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(panelEncabezado, BorderLayout.NORTH);
        centro.add(tabs, BorderLayout.CENTER);

        add(btnVolver, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private JScrollPane envolverEnScroll(JPanel contenido) {
        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
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
            if (perfil == null || !perfil.isActiva()) {
                // Una cuenta desactivada se comporta como si no existiera.
                JOptionPane.showMessageDialog(this, "No se encontró la cuenta @" + username,
                        "Cuenta no disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            lblNombreCompleto.setText(perfil.getNombreCompleto());
            lblUsername.setText("@" + perfil.getUsername());

            // Mostrar la descripción que la otra persona guardó en su perfil
            String descripcion = perfil.getDescripcion();
            if (descripcion == null || descripcion.isBlank()) {
                lblDescripcion.setText("");
                lblDescripcion.setVisible(false);
            } else {
                String html = EstiloInsta.escaparHtml(descripcion.trim()).replace("\n", "<br>");
                lblDescripcion.setText("<html><div style='text-align:center; width:320px;'>" + html + "</div></html>");
                lblDescripcion.setVisible(true);
            }

            String ruta = perfil.getRutaFotoPerfil();
            if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
                lblAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
            } else {
                String nombre = perfil.getNombreCompleto();
                String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
                lblAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(username.hashCode()), 96));
            }

            ListaEnlazada<String> followers = GestorInstaPlus.obtenerFollowers(username);
            ListaEnlazada<String> following = GestorInstaPlus.obtenerFollowing(username);
            int followersParaMostrar = GestorInstaPlus.contarFollowersParaMostrar(username);
            int posts = GestorPosts.contarPosts(username);

            lblPosts.setText(String.valueOf(posts));
            lblFollowers.setText(String.valueOf(followersParaMostrar));
            lblFollowing.setText(String.valueOf(following.tamano()));

            boolean loSigo = followers.contiene(actual.getUsername());
            btnSeguir.setText(loSigo ? "Dejar de seguir" : "Seguir");

            refrescarListaCuentas(panelSeguidoresLista, followers, "Todavía no tiene seguidores.");
            refrescarListaCuentas(panelSiguiendoLista, following, "Todavía no sigue a nadie.");
            refrescarPublicaciones(username);

            controlador.mostrarSeccion("PERFIL_AJENO");

        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el perfil de @" + username,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarListaCuentas(JPanel contenedor, ListaEnlazada<String> usuarios, String mensajeVacio) {
        contenedor.removeAll();
        if (usuarios.estaVacia()) {
            JLabel lblVacio = new JLabel(mensajeVacio);
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            contenedor.add(lblVacio);
        } else {
            for (String u : usuarios) {
                contenedor.add(crearFilaCuenta(u));
                contenedor.add(Box.createVerticalStrut(6));
            }
        }
        contenedor.revalidate();
        contenedor.repaint();
    }

    private void refrescarPublicaciones(String username) {
        List<Post> posts;
        try {
            posts = GestorPosts.cargarPostsDeUsuario(username);
        } catch (ArchivoCorruptoException ex) {
            posts = new ArrayList<>();
        }
        panelPublicaciones.mostrarPosts(posts, "Aún no hay publicaciones.");
    }

    private JPanel crearFilaCuenta(String username) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lblAvatarFila = new JLabel(AvatarHelper.avatarPara(username, 32));
        fila.add(lblAvatarFila, BorderLayout.WEST);

        JButton btnUsername = new JButton("@" + username);
        btnUsername.setContentAreaFilled(false);
        btnUsername.setBorderPainted(false);
        btnUsername.setFocusPainted(false);
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setForeground(TemaUI.TEXTO);
        btnUsername.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUsername.addActionListener(e -> controlador.abrirPerfilAjeno(username, "PERFIL_AJENO"));
        fila.add(btnUsername, BorderLayout.CENTER);

        return fila;
    }

    private void alternarSeguimiento() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (usernameVisitado == null || actual == null) {
            return;
        }
        try {
            ListaEnlazada<String> followers = GestorInstaPlus.obtenerFollowers(usernameVisitado);
            boolean loSigo = followers.contiene(actual.getUsername());

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