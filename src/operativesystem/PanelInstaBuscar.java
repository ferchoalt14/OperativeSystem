package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelInstaBuscar extends JPanel {

    private final InstaControlador controlador;
    private final JTextField txtBuscar;
    private final JPanel panelResultados;
    private final JTextField txtBuscarHashtag;
    private final JPanel panelResultadosHashtag;

    public PanelInstaBuscar(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // --- Pestaña: Buscar personas ---
        JPanel panelPersonas = new JPanel(new BorderLayout(0, 12));
        panelPersonas.setOpaque(false);

        JLabel lblTitulo = new JLabel("Buscar personas");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtBuscar = TemaUI.crearCampoTexto("Buscar por username...", 18);
        txtBuscar.addActionListener(e -> ejecutarBusqueda());

        JButton btnBuscar = TemaUI.crearBotonPrimario("Buscar");
        btnBuscar.addActionListener(e -> ejecutarBusqueda());

        JPanel panelCampo = new JPanel(new BorderLayout(8, 0));
        panelCampo.setOpaque(false);
        panelCampo.add(txtBuscar, BorderLayout.CENTER);
        panelCampo.add(btnBuscar, BorderLayout.EAST);
        panelCampo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.add(lblTitulo);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(panelCampo);

        panelResultados = new PanelDesplazable(new BorderLayout());
        panelResultados.setOpaque(false);
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        panelPersonas.add(panelEncabezado, BorderLayout.NORTH);
        panelPersonas.add(scroll, BorderLayout.CENTER);

        // --- Pestaña: Buscar hashtag ---
        JPanel panelHashtag = new JPanel(new BorderLayout(0, 12));
        panelHashtag.setOpaque(false);

        JLabel lblTituloHashtag = new JLabel("Buscar hashtag");
        lblTituloHashtag.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloHashtag.setForeground(TemaUI.ACCENT_OSCURO);

        txtBuscarHashtag = TemaUI.crearCampoTexto("Buscar por #hashtag...", 18);
        txtBuscarHashtag.addActionListener(e -> ejecutarBusquedaHashtag());

        JButton btnBuscarHashtag = TemaUI.crearBotonPrimario("Buscar");
        btnBuscarHashtag.addActionListener(e -> ejecutarBusquedaHashtag());

        JPanel panelCampoHashtag = new JPanel(new BorderLayout(8, 0));
        panelCampoHashtag.setOpaque(false);
        panelCampoHashtag.add(txtBuscarHashtag, BorderLayout.CENTER);
        panelCampoHashtag.add(btnBuscarHashtag, BorderLayout.EAST);

        JPanel panelEncabezadoHashtag = new JPanel();
        panelEncabezadoHashtag.setOpaque(false);
        panelEncabezadoHashtag.setLayout(new BoxLayout(panelEncabezadoHashtag, BoxLayout.Y_AXIS));
        panelEncabezadoHashtag.add(lblTituloHashtag);
        panelEncabezadoHashtag.add(Box.createVerticalStrut(10));
        panelEncabezadoHashtag.add(panelCampoHashtag);

        panelResultadosHashtag = new PanelDesplazable(new BorderLayout());
        panelResultadosHashtag.setOpaque(false);
        panelResultadosHashtag.setLayout(new BoxLayout(panelResultadosHashtag, BoxLayout.Y_AXIS));

        JScrollPane scrollHashtag = new JScrollPane(panelResultadosHashtag);
        scrollHashtag.setOpaque(false);
        scrollHashtag.getViewport().setOpaque(false);
        scrollHashtag.setBorder(null);
        scrollHashtag.getVerticalScrollBar().setUnitIncrement(14);

        panelHashtag.add(panelEncabezadoHashtag, BorderLayout.NORTH);
        panelHashtag.add(scrollHashtag, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.addTab("Personas", panelPersonas);
        tabs.addTab("Hashtags", panelHashtag);

        add(tabs, BorderLayout.CENTER);
    }

    private void ejecutarBusquedaHashtag() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        String texto = txtBuscarHashtag.getText().trim();
        if (texto.startsWith("#")) {
            texto = texto.substring(1);
        }
        panelResultadosHashtag.removeAll();

        if (!texto.isEmpty()) {
            try {
                ListaEnlazada<Post> resultados = GestorPosts.buscarPorHashtag(actual.getUsername(), texto);
                if (resultados.estaVacia()) {
                    JLabel lblVacio = new JLabel("No se encontraron publicaciones con #" + texto);
                    lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
                    lblVacio.setBorder(new EmptyBorder(10, 4, 10, 4));
                    panelResultadosHashtag.add(lblVacio);
                } else {
                    for (Post p : resultados) {
                        panelResultadosHashtag.add(crearFilaPost(p));
                        panelResultadosHashtag.add(Box.createVerticalStrut(6));
                    }
                }
            } catch (ArchivoCorruptoException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo buscar el hashtag.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        panelResultadosHashtag.revalidate();
        panelResultadosHashtag.repaint();
    }

    private JPanel crearFilaPost(Post post) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        fila.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(post.getUsernameAutor(), 36));
        fila.add(lblAvatar, BorderLayout.WEST);

        String resumen = post.getTexto().length() > 60 ? post.getTexto().substring(0, 60) + "…" : post.getTexto();
        JButton btnPost = new JButton("@" + post.getUsernameAutor() + "   —   " + resumen);
        btnPost.setContentAreaFilled(false);
        btnPost.setBorderPainted(false);
        btnPost.setFocusPainted(false);
        btnPost.setHorizontalAlignment(SwingConstants.LEFT);
        btnPost.setForeground(TemaUI.TEXTO);
        btnPost.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPost.addActionListener(e -> controlador.abrirPerfilAjeno(post.getUsernameAutor(), "BUSCAR"));
        fila.add(btnPost, BorderLayout.CENTER);

        return fila;
    }

    public void ejecutarBusqueda() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        String texto = txtBuscar.getText().trim().toLowerCase();
        panelResultados.removeAll();

        if (!texto.isEmpty()) {
            ListaEnlazada<String> coincidencias = new ListaEnlazada<>();
            try {
                List<UsuarioInsta> usuarios = GestorInstaPlus.cargarUsuarios();
                for (UsuarioInsta u : usuarios) {
                    if (u.isActiva()
                            && !u.getUsername().equalsIgnoreCase(actual.getUsername())
                            && u.getUsername().toLowerCase().contains(texto)) {
                        coincidencias.agregar(u.getUsername());
                    }
                }
            } catch (ArchivoCorruptoException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer la base de usuarios de INSTA+.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (coincidencias.estaVacia()) {
                JLabel lblVacio = new JLabel("No se encontraron usuarios con ese nombre.");
                lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
                lblVacio.setBorder(new EmptyBorder(10, 4, 10, 4));
                panelResultados.add(lblVacio);
            } else {
                ListaEnlazada<String> misSeguidos;
                try {
                    misSeguidos = GestorInstaPlus.obtenerFollowing(actual.getUsername());
                } catch (ArchivoCorruptoException ex) {
                    misSeguidos = new ListaEnlazada<>();
                }
                for (String username : coincidencias) {
                    boolean loSigo = misSeguidos.contiene(username);
                    panelResultados.add(crearFilaResultado(username, loSigo));
                    panelResultados.add(Box.createVerticalStrut(6));
                }
            }
        }

        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private JPanel crearFilaResultado(String username, boolean loSigo) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        fila.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(username, 36));
        fila.add(lblAvatar, BorderLayout.WEST);

        JButton btnUsername = new JButton("@" + username + (loSigo ? "   —   Lo sigues" : "   —   No lo sigues"));
        btnUsername.setContentAreaFilled(false);
        btnUsername.setBorderPainted(false);
        btnUsername.setFocusPainted(false);
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setForeground(TemaUI.TEXTO);
        btnUsername.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUsername.addActionListener(e -> controlador.abrirPerfilAjeno(username, "BUSCAR"));
        fila.add(btnUsername, BorderLayout.CENTER);

        return fila;
    }
}