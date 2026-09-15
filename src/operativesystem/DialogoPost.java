package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class DialogoPost extends JDialog {

    private final InstaControlador controlador;
    private final Post post;
    private final Runnable alCambiar;

    private JButton btnLike;
    private JLabel lblLikes;
    private JPanel panelComentarios;

    public DialogoPost(Frame owner, InstaControlador controlador, Post post, Runnable alCambiar) {
        super(owner, "Publicación de @" + post.getUsernameAutor(), true);
        this.controlador = controlador;
        this.post = post;
        this.alCambiar = alCambiar;

        setLayout(new BorderLayout());
        setSize(460, 640);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(TemaUI.FONDO);

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 14, 8, 14));

        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(post.getUsernameAutor(), 32));
        JLabel lblUsername = new JLabel("@" + post.getUsernameAutor());
        lblUsername.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUsername.setForeground(TemaUI.TEXTO);

        panel.add(lblAvatar, BorderLayout.WEST);
        panel.add(lblUsername, BorderLayout.CENTER);

        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual != null && actual.getUsername().equals(post.getUsernameAutor())) {
            JButton btnEliminar = new JButton("🗑 Eliminar");
            btnEliminar.setContentAreaFilled(false);
            btnEliminar.setBorderPainted(false);
            btnEliminar.setFocusPainted(false);
            btnEliminar.setForeground(new Color(190, 40, 40));
            btnEliminar.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnEliminar.addActionListener(e -> eliminarPost());
            panel.add(btnEliminar, BorderLayout.EAST);
        }

        return panel;
    }

    private void eliminarPost() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar esta publicación? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            GestorPosts.eliminarPost(post.getUsernameAutor(), post.getId());
            dispose();
            if (alCambiar != null) {
                alCambiar.run();
            }
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar la publicación: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JScrollPane construirCentro() {
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(0, 14, 14, 14));

        JLabel lblImagen;
        String ruta = post.getRutaImagen();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            ImageIcon icono = new ImageIcon(new ImageIcon(ruta).getImage()
                    .getScaledInstance(400, 320, Image.SCALE_SMOOTH));
            lblImagen = new JLabel(icono);
        } else {
            lblImagen = new JLabel("🖼", SwingConstants.CENTER);
            lblImagen.setFont(lblImagen.getFont().deriveFont(64f));
            lblImagen.setPreferredSize(new Dimension(400, 260));
            lblImagen.setMaximumSize(new Dimension(400, 260));
            lblImagen.setOpaque(true);
            lblImagen.setBackground(TemaUI.SUPERFICIE);
        }
        lblImagen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblFecha = new JLabel(post.getFechaTexto());
        lblFecha.setForeground(TemaUI.TEXTO_SUAVE);
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFecha.setBorder(new EmptyBorder(8, 0, 4, 0));

        JPanel panelLike = construirPanelLike();
        panelLike.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtCaption = new JTextArea(post.getTexto());
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);
        txtCaption.setEditable(false);
        txtCaption.setOpaque(false);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtCaption.setForeground(TemaUI.TEXTO);
        txtCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        ListaEnlazada<String> menciones = post.getMenciones();
        JPanel panelMenciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelMenciones.setOpaque(false);
        panelMenciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!menciones.estaVacia()) {
            JLabel lblEtiquetaMenciones = new JLabel("Menciona a:");
            lblEtiquetaMenciones.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblEtiquetaMenciones.setForeground(TemaUI.TEXTO_SUAVE);
            panelMenciones.add(lblEtiquetaMenciones);
            for (String username : menciones) {
                JButton btnMencion = new JButton("@" + username);
                btnMencion.setContentAreaFilled(false);
                btnMencion.setBorderPainted(false);
                btnMencion.setFocusPainted(false);
                btnMencion.setForeground(TemaUI.ACCENT_OSCURO);
                btnMencion.setFont(new Font("SansSerif", Font.BOLD, 11));
                btnMencion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnMencion.addActionListener(e -> {
                    dispose();
                    controlador.abrirPerfilAjeno(username, "FEED");
                });
                panelMenciones.add(btnMencion);
            }
        }

        JLabel lblComentariosTitulo = new JLabel("Comentarios");
        lblComentariosTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblComentariosTitulo.setForeground(TemaUI.TEXTO);
        lblComentariosTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblComentariosTitulo.setBorder(new EmptyBorder(10, 0, 6, 0));

        panelComentarios = new JPanel();
        panelComentarios.setOpaque(false);
        panelComentarios.setLayout(new BoxLayout(panelComentarios, BoxLayout.Y_AXIS));
        panelComentarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        refrescarComentarios();

        JPanel panelNuevoComentario = construirPanelNuevoComentario();
        panelNuevoComentario.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNuevoComentario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        contenido.add(lblImagen);
        contenido.add(lblFecha);
        contenido.add(panelLike);
        contenido.add(txtCaption);
        if (!menciones.estaVacia()) {
            contenido.add(panelMenciones);
        }
        contenido.add(lblComentariosTitulo);
        contenido.add(panelComentarios);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(panelNuevoComentario);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private JPanel construirPanelLike() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        UsuarioInsta actual = controlador.getUsuarioActual();
        boolean leDiLike = actual != null && post.estaLikeadoPor(actual.getUsername());

        btnLike = new JButton(leDiLike ? "❤" : "🤍");
        btnLike.setFont(btnLike.getFont().deriveFont(18f));
        btnLike.setContentAreaFilled(false);
        btnLike.setBorderPainted(false);
        btnLike.setFocusPainted(false);
        btnLike.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLike.addActionListener(e -> alternarLike());

        lblLikes = new JLabel(post.getLikes() + " likes");
        lblLikes.setForeground(TemaUI.TEXTO_SUAVE);
        lblLikes.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panel.add(btnLike);
        panel.add(lblLikes);
        return panel;
    }

    private void alternarLike() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        try {
            GestorPosts.alternarLike(post.getUsernameAutor(), post.getId(), actual.getUsername());
            Post actualizado = GestorPosts.obtenerPostPorId(post.getUsernameAutor(), post.getId());
            if (actualizado != null) {
                btnLike.setText(actualizado.estaLikeadoPor(actual.getUsername()) ? "❤" : "🤍");
                lblLikes.setText(actualizado.getLikes() + " likes");
            }
            if (alCambiar != null) {
                alCambiar.run();
            }
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el like: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarComentarios() {
        panelComentarios.removeAll();
        List<Comentario> comentarios = post.getComentarios();
        if (comentarios.isEmpty()) {
            JLabel lblVacio = new JLabel("Sé el primero en comentar.");
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            lblVacio.setFont(new Font("SansSerif", Font.PLAIN, 12));
            panelComentarios.add(lblVacio);
        } else {
            for (Comentario c : comentarios) {
                panelComentarios.add(crearFilaComentario(c));
                panelComentarios.add(Box.createVerticalStrut(4));
            }
        }
        panelComentarios.revalidate();
        panelComentarios.repaint();
    }

    private JPanel crearFilaComentario(Comentario c) {
        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(c.getUsernameAutor(), 22));
        JLabel lblTexto = new JLabel("<html><b>@" + c.getUsernameAutor() + "</b>&nbsp;&nbsp;" + c.getTexto() + "</html>");
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTexto.setForeground(TemaUI.TEXTO);

        fila.add(lblAvatar, BorderLayout.WEST);
        fila.add(lblTexto, BorderLayout.CENTER);
        return fila;
    }

    private JPanel construirPanelNuevoComentario() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);

        JTextField txtComentario = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        Runnable enviar = () -> {
            UsuarioInsta actual = controlador.getUsuarioActual();
            String texto = txtComentario.getText().trim();
            if (actual == null || texto.isEmpty()) {
                return;
            }
            try {
                Comentario nuevo = GestorPosts.agregarComentario(
                        post.getUsernameAutor(), post.getId(), actual.getUsername(), texto);
                post.agregarComentario(nuevo);
                txtComentario.setText("");
                refrescarComentarios();
                if (alCambiar != null) {
                    alCambiar.run();
                }
            } catch (ArchivoCorruptoException | IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo enviar el comentario: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        btnEnviar.addActionListener(e -> enviar.run());
        txtComentario.addActionListener(e -> enviar.run());

        panel.add(txtComentario, BorderLayout.CENTER);
        panel.add(btnEnviar, BorderLayout.EAST);
        return panel;
    }
}