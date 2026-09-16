package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class PanelInstaFeed extends JPanel {

    private final InstaControlador controlador;
    private final JPanel panelPosts;
    private final JScrollPane scroll;


    private final Map<String, Tarjeta> tarjetas = new HashMap<>();

    private static final class Tarjeta {
        JButton btnLike;
        JLabel lblLikes;
        JButton btnComentarios;
    }

    public PanelInstaFeed(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(20, 26, 0, 26));

        JLabel lblTitulo = EstiloInsta.etiqueta("Inicio", 22, true, TemaUI.ACCENT_OSCURO);
        lblTitulo.setBorder(new EmptyBorder(0, 4, 14, 0));

        panelPosts = new JPanel();
        panelPosts.setOpaque(false);
        panelPosts.setLayout(new BoxLayout(panelPosts, BoxLayout.Y_AXIS));
        panelPosts.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel envoltorio = new PanelDesplazable(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(panelPosts, BorderLayout.NORTH);

        scroll = new JScrollPane(envoltorio);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private static String clave(String autor, String postId) {
        return autor.toLowerCase() + "|" + postId;
    }

    
    public void refrescar() {
        int posicion = scroll.getVerticalScrollBar().getValue();
        panelPosts.removeAll();
        tarjetas.clear();
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
            JLabel error = EstiloInsta.etiqueta("No se pudo cargar el feed: " + ex.getMessage(), 12, false, EstiloInsta.ROJO);
            error.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelPosts.add(error);
            panelPosts.revalidate();
            panelPosts.repaint();
            return;
        }

        if (feed.estaVacia()) {
            JPanel vacio = EstiloInsta.filaAjustada(new GridBagLayout());
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            vacio.setBorder(new EmptyBorder(60, 0, 0, 0));
            JLabel lblVacio = new JLabel("<html><center><font size='7'>📷</font><br><br>Aún no hay publicaciones en tu feed.<br>"
                    + "Sigue cuentas para empezar a verlas aquí.</center></html>", SwingConstants.CENTER);
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            vacio.add(lblVacio);
            panelPosts.add(vacio);
        } else {
            for (Post post : feed) {
                panelPosts.add(crearTarjetaPost(post, actual));
                panelPosts.add(Box.createVerticalStrut(16));
            }
        }

        panelPosts.revalidate();
        panelPosts.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(posicion));
    }

    /** Actualiza likes y comentarios de una tarjeta sin redibujar todo el feed. */
    public void actualizarPost(String autor, String postId) {
        Tarjeta t = tarjetas.get(clave(autor, postId));
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (t == null || actual == null) {
            return;
        }
        try {
            Post p = GestorPosts.obtenerPostPorId(autor, postId);
            if (p == null) {
                refrescar();
                return;
            }
            pintarLike(t, p.estaLikeadoPor(actual.getUsername()), p.getLikes());
            t.btnComentarios.setText("💬 " + GestorPosts.comentariosVisibles(p).size());
        } catch (ArchivoCorruptoException ex) {
            // se actualizará en el próximo refresco
        }
    }

    private static void pintarLike(Tarjeta t, boolean likeado, int likes) {
        t.btnLike.setText(likeado ? "♥" : "♡");
        t.btnLike.setForeground(likeado ? EstiloInsta.LIKE_ACTIVO : EstiloInsta.LIKE_INACTIVO);
        t.lblLikes.setText(String.format("%,d me gusta", likes));
    }

    private JPanel crearTarjetaPost(Post post, UsuarioInsta actual) {
        EstiloInsta.PanelRedondeado tarjeta = new EstiloInsta.PanelRedondeado(
                new BorderLayout(0, 10), TemaUI.SUPERFICIE, TemaUI.BORDE, 18) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(560, getPreferredSize().height);
            }
        };
        tarjeta.setBorder(new EmptyBorder(12, 14, 12, 14));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        encabezado.setOpaque(false);
        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(post.getUsernameAutor(), 36));
        JButton btnUsername = EstiloInsta.botonTexto("@" + post.getUsernameAutor(), TemaUI.TEXTO, 13, true);
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnUsername.addActionListener(e -> controlador.abrirPerfilAjeno(post.getUsernameAutor(), "FEED"));
        JLabel lblFecha = EstiloInsta.etiqueta(post.getFechaTexto(), 10, false, TemaUI.TEXTO_SUAVE);

        JPanel panelNombreFecha = new JPanel(new GridLayout(2, 1));
        panelNombreFecha.setOpaque(false);
        JPanel filaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaNombre.setOpaque(false);
        filaNombre.add(btnUsername);
        panelNombreFecha.add(filaNombre);
        panelNombreFecha.add(lblFecha);

        encabezado.add(lblAvatar, BorderLayout.WEST);
        encabezado.add(panelNombreFecha, BorderLayout.CENTER);

        // Imagen
        JComponent vistaImagen;
        ImageIcon imagen = EstiloInsta.imagenAjustada(post.getRutaImagen(), 520, 420);
        if (imagen != null) {
            JLabel lblImagen = new JLabel(imagen, SwingConstants.CENTER);
            vistaImagen = lblImagen;
        } else {
            EstiloInsta.PanelRedondeado placeholder = new EstiloInsta.PanelRedondeado(
                    new GridBagLayout(), EstiloInsta.mezclar(TemaUI.SUPERFICIE, TemaUI.TEXTO, 0.05), null, 14);
            placeholder.setPreferredSize(new Dimension(360, 150));
            JLabel ico = new JLabel("🖼");
            ico.setFont(ico.getFont().deriveFont(40f));
            ico.setForeground(TemaUI.TEXTO_SUAVE);
            placeholder.add(ico);
            vistaImagen = placeholder;
        }
        vistaImagen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        vistaImagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controlador.abrirPost(post, () -> actualizarPost(post.getUsernameAutor(), post.getId()));
            }
        });

        // Acciones
        Tarjeta refs = new Tarjeta();
        refs.btnLike = EstiloInsta.botonTexto("♡", EstiloInsta.LIKE_INACTIVO, 22, true);
        refs.lblLikes = EstiloInsta.etiqueta("", 12, true, TemaUI.TEXTO);
        pintarLike(refs, post.estaLikeadoPor(actual.getUsername()), post.getLikes());
        refs.btnLike.addActionListener(e -> alternarLike(post, refs));

        refs.btnComentarios = EstiloInsta.botonTexto("💬 " + GestorPosts.comentariosVisibles(post).size(), TemaUI.TEXTO_SUAVE, 12, false);
        refs.btnComentarios.setToolTipText("Ver y escribir comentarios");
        refs.btnComentarios.addActionListener(e ->
                controlador.abrirPost(post, () -> actualizarPost(post.getUsernameAutor(), post.getId())));
        tarjetas.put(clave(post.getUsernameAutor(), post.getId()), refs);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panelAcciones.setOpaque(false);
        panelAcciones.add(refs.btnLike);
        panelAcciones.add(refs.lblLikes);
        panelAcciones.add(Box.createHorizontalStrut(14));
        panelAcciones.add(refs.btnComentarios);

        JPanel panelInferior = new JPanel(new BorderLayout(0, 4));
        panelInferior.setOpaque(false);
        panelInferior.add(panelAcciones, BorderLayout.NORTH);
        if (post.getTexto() != null && !post.getTexto().isBlank()) {
            panelInferior.add(EstiloInsta.textoAjustable(post.getTexto(), 13, TemaUI.TEXTO), BorderLayout.CENTER);
        }

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(vistaImagen, BorderLayout.CENTER);
        tarjeta.add(panelInferior, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void alternarLike(Post post, Tarjeta refs) {
        if (controlador.getUsuarioActual() == null) {
            return;
        }
        refs.btnLike.setEnabled(false);
        controlador.enviarAlServidor(PaqueteInsta.like(post.getUsernameAutor(), post.getId()), r -> {
            refs.btnLike.setEnabled(true);
            if (!r.esOk()) {
                controlador.mostrarAviso("No se pudo dar me gusta", r.getError());
                return;
            }
            pintarLike(refs, r.getBool(PaqueteInsta.LIKEADO), r.getInt(PaqueteInsta.LIKES));
        });
    }
}