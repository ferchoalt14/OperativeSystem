package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Detalle de una publicación mostrado DENTRO de INSTA+ (reemplaza al antiguo DialogoPost).
 * Likes, comentarios y eliminación viajan por sockets.
 */
public class PanelInstaDetallePost extends JPanel {

    private final InstaControlador controlador;

    private Post post;
    private String seccionOrigen = "FEED";
    private Runnable alCambiar;

    private final JLabel lblAvatar = new JLabel();
    private final JButton btnAutor;
    private final JButton btnEliminar;
    private final ConfirmacionEnLinea confirmacion = new ConfirmacionEnLinea();
    private final MensajeEnLinea mensajeSuperior = new MensajeEnLinea();

    private final JPanel contenido;
    private final JScrollPane scroll;

    private final JTextField txtComentario;
    private final JButton btnEnviar;
    private final MensajeEnLinea mensajeComentario = new MensajeEnLinea();

    public PanelInstaDetallePost(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);

        // ---------------- Encabezado ----------------
        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(true);
        encabezado.setBackground(TemaUI.SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaUI.BORDE),
                new EmptyBorder(10, 12, 10, 14)));

        JButton btnVolver = EstiloInsta.botonTexto("←", TemaUI.ACCENT_OSCURO, 18, true);
        btnVolver.setToolTipText("Volver");
        btnVolver.addActionListener(e -> volver());

        btnAutor = EstiloInsta.botonTexto("", TemaUI.TEXTO, 14, true);
        btnAutor.addActionListener(e -> {
            if (post != null) {
                controlador.abrirPerfilAjeno(post.getUsernameAutor(), seccionOrigen);
            }
        });

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izquierda.setOpaque(false);
        izquierda.add(btnVolver);
        izquierda.add(lblAvatar);
        izquierda.add(btnAutor);

        btnEliminar = EstiloInsta.botonTexto("🗑 Eliminar", EstiloInsta.ROJO, 12, false);
        btnEliminar.addActionListener(e -> confirmacion.preguntar(
                "¿Seguro que quieres eliminar esta publicación? Esta acción no se puede deshacer.",
                "Eliminar", this::eliminarPost));

        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(btnEliminar, BorderLayout.EAST);

        JPanel avisosSuperiores = new JPanel();
        avisosSuperiores.setOpaque(false);
        avisosSuperiores.setLayout(new BoxLayout(avisosSuperiores, BoxLayout.Y_AXIS));
        avisosSuperiores.setBorder(new EmptyBorder(0, 16, 0, 16));
        avisosSuperiores.add(Box.createVerticalStrut(6));
        avisosSuperiores.add(confirmacion);
        avisosSuperiores.add(mensajeSuperior);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(encabezado, BorderLayout.NORTH);
        norte.add(avisosSuperiores, BorderLayout.CENTER);

        // ---------------- Contenido ----------------
        contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(12, 22, 16, 22));

        JPanel envoltorio = new PanelDesplazable(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(contenido, BorderLayout.NORTH);

        scroll = new JScrollPane(envoltorio);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // ---------------- Barra para comentar ----------------
        txtComentario = EstiloInsta.campoTexto(20);
        txtComentario.addActionListener(e -> enviarComentario());
        btnEnviar = TemaUI.crearBotonPrimario("Publicar");
        btnEnviar.addActionListener(e -> enviarComentario());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        botones.setOpaque(false);
        botones.add(SelectorEmojis.crearBoton(txtComentario));
        botones.add(btnEnviar);

        JPanel filaComentar = new JPanel(new BorderLayout(8, 0));
        filaComentar.setOpaque(false);
        filaComentar.add(txtComentario, BorderLayout.CENTER);
        filaComentar.add(botones, BorderLayout.EAST);

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.setOpaque(true);
        sur.setBackground(TemaUI.SUPERFICIE);
        sur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaUI.BORDE),
                new EmptyBorder(8, 12, 10, 12)));
        sur.add(mensajeComentario, BorderLayout.NORTH);
        sur.add(filaComentar, BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------------------------------

    public void abrir(Post post, String seccionOrigen, Runnable alCambiar) {
        this.post = post;
        this.seccionOrigen = (seccionOrigen == null || "POST".equals(seccionOrigen)) ? "FEED" : seccionOrigen;
        this.alCambiar = alCambiar;
        confirmacion.ocultar();
        mensajeSuperior.ocultar();
        mensajeComentario.ocultar();
        txtComentario.setText("");
        recargar();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    /** ¿Está mostrando este post? */
    public boolean muestra(String autor, String postId) {
        return post != null && post.getId().equals(postId) && post.getUsernameAutor().equalsIgnoreCase(autor);
    }

    public String getSeccionOrigen() {
        return seccionOrigen;
    }

    /** Vuelve a leer el post del disco y redibuja (se llama al llegar eventos del socket). */
    public void recargar() {
        if (post == null) {
            return;
        }
        try {
            Post actualizado = GestorPosts.obtenerPostPorId(post.getUsernameAutor(), post.getId());
            if (actualizado == null) {
                volver();
                controlador.mostrarAviso("Publicación eliminada", "Esta publicación ya no está disponible.");
                return;
            }
            post = actualizado;
        } catch (ArchivoCorruptoException ex) {
            mensajeSuperior.error("No se pudo recargar la publicación: " + ex.getMessage());
        }
        int posicion = scroll.getVerticalScrollBar().getValue();
        construirContenido();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(posicion));
    }

    public void volver() {
        confirmacion.ocultar();
        controlador.mostrarSeccion(seccionOrigen);
    }

    // ------------------------------------------------------------------------------------------

    private void construirContenido() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        String autor = post.getUsernameAutor();

        lblAvatar.setIcon(AvatarHelper.avatarPara(autor, 30));
        btnAutor.setText("@" + autor);
        btnEliminar.setVisible(actual != null && actual.getUsername().equalsIgnoreCase(autor));

        contenido.removeAll();

        // Imagen
        ImageIcon imagen = EstiloInsta.imagenAjustada(post.getRutaImagen(), 460, 420);
        JPanel marcoImagen = EstiloInsta.filaAjustada(new FlowLayout(FlowLayout.CENTER, 0, 0));
        if (imagen != null) {
            marcoImagen.add(new JLabel(imagen));
        } else {
            EstiloInsta.PanelRedondeado placeholder = new EstiloInsta.PanelRedondeado(
                    new GridBagLayout(), EstiloInsta.mezclar(TemaUI.FONDO, TemaUI.TEXTO, 0.05), null, 18);
            placeholder.setPreferredSize(new Dimension(420, 200));
            JLabel lbl = new JLabel("<html><center><font size='6'>🖼</font><br>Publicación de solo texto</center></html>");
            lbl.setForeground(TemaUI.TEXTO_SUAVE);
            placeholder.add(lbl);
            marcoImagen.add(placeholder);
        }
        contenido.add(marcoImagen);
        contenido.add(Box.createVerticalStrut(10));

        // Acciones: like + contador + fecha
        boolean leDiLike = actual != null && post.estaLikeadoPor(actual.getUsername());
        JButton btnLike = EstiloInsta.botonTexto(leDiLike ? "♥" : "♡",
                leDiLike ? EstiloInsta.LIKE_ACTIVO : EstiloInsta.LIKE_INACTIVO, 24, true);
        btnLike.setToolTipText(leDiLike ? "Quitar me gusta" : "Me gusta");
        JLabel lblLikes = EstiloInsta.etiqueta(formatear(post.getLikes()) + " me gusta", 13, true, TemaUI.TEXTO);
        btnLike.addActionListener(e -> alternarLike(btnLike, lblLikes));

        List<Comentario> comentarios = post.getComentarios();
        JLabel lblNumComentarios = EstiloInsta.etiqueta("💬 " + comentarios.size(), 13, false, TemaUI.TEXTO_SUAVE);

        JPanel filaAcciones = EstiloInsta.filaAjustada(new BorderLayout());
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        izquierda.setOpaque(false);
        izquierda.add(btnLike);
        izquierda.add(lblLikes);
        izquierda.add(Box.createHorizontalStrut(12));
        izquierda.add(lblNumComentarios);
        filaAcciones.add(izquierda, BorderLayout.WEST);
        filaAcciones.add(EstiloInsta.etiqueta(post.getFechaTexto(), 11, false, TemaUI.TEXTO_SUAVE), BorderLayout.EAST);
        contenido.add(filaAcciones);

        // Caption
        if (post.getTexto() != null && !post.getTexto().isBlank()) {
            contenido.add(Box.createVerticalStrut(6));
            JPanel caption = EstiloInsta.filaAjustada(new BorderLayout(0, 2));
            caption.add(EstiloInsta.etiqueta("@" + autor, 13, true, TemaUI.TEXTO), BorderLayout.NORTH);
            caption.add(EstiloInsta.textoAjustable(post.getTexto(), 13, TemaUI.TEXTO), BorderLayout.CENTER);
            contenido.add(caption);
        }

        // Menciones
        ListaEnlazada<String> menciones = post.getMenciones();
        if (!menciones.estaVacia()) {
            JPanel panelMenciones = EstiloInsta.filaAjustada(new FlowLayout(FlowLayout.LEFT, 2, 0));
            panelMenciones.add(EstiloInsta.etiqueta("Menciona a:", 11, false, TemaUI.TEXTO_SUAVE));
            for (String username : menciones) {
                JButton btnMencion = EstiloInsta.botonTexto("@" + username, TemaUI.ACCENT_OSCURO, 11, true);
                btnMencion.addActionListener(e -> controlador.abrirPerfilAjeno(username, "POST"));
                panelMenciones.add(btnMencion);
            }
            contenido.add(Box.createVerticalStrut(4));
            contenido.add(panelMenciones);
        }

        // Comentarios
        contenido.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(TemaUI.BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(sep);
        contenido.add(Box.createVerticalStrut(10));

        JLabel lblTitulo = EstiloInsta.etiqueta("Comentarios" + (comentarios.isEmpty() ? "" : " (" + comentarios.size() + ")"),
                14, true, TemaUI.TEXTO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(8));

        if (comentarios.isEmpty()) {
            JLabel vacio = EstiloInsta.etiqueta("Aún no hay comentarios. ¡Sé el primero en comentar!", 12, false, TemaUI.TEXTO_SUAVE);
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(vacio);
        } else {
            for (Comentario c : comentarios) {
                contenido.add(crearFilaComentario(c));
                contenido.add(Box.createVerticalStrut(10));
            }
        }

        contenido.revalidate();
        contenido.repaint();
    }

    private JPanel crearFilaComentario(Comentario c) {
        JPanel fila = EstiloInsta.filaAjustada(new BorderLayout(10, 0));

        JPanel colAvatar = new JPanel(new BorderLayout());
        colAvatar.setOpaque(false);
        colAvatar.add(new JLabel(AvatarHelper.avatarPara(c.getUsernameAutor(), 28)), BorderLayout.NORTH);

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cabecera.setOpaque(false);
        JButton btnUser = EstiloInsta.botonTexto("@" + c.getUsernameAutor(), TemaUI.TEXTO, 12, true);
        btnUser.setBorder(new EmptyBorder(0, 0, 0, 6));
        btnUser.addActionListener(e -> controlador.abrirPerfilAjeno(c.getUsernameAutor(), "POST"));
        cabecera.add(btnUser);
        cabecera.add(EstiloInsta.etiqueta(c.getFechaTexto(), 10, false, TemaUI.TEXTO_SUAVE));

        JPanel cuerpo = new JPanel(new BorderLayout(0, 2));
        cuerpo.setOpaque(false);
        cuerpo.add(cabecera, BorderLayout.NORTH);
        cuerpo.add(EstiloInsta.textoAjustable(c.getTexto(), 13, TemaUI.TEXTO), BorderLayout.CENTER);

        fila.add(colAvatar, BorderLayout.WEST);
        fila.add(cuerpo, BorderLayout.CENTER);
        return fila;
    }

    // ------------------------------------------------------------------------------------------

    private void alternarLike(JButton btnLike, JLabel lblLikes) {
        if (post == null || controlador.getUsuarioActual() == null) {
            return;
        }
        btnLike.setEnabled(false);
        controlador.enviarAlServidor(PaqueteInsta.like(post.getUsernameAutor(), post.getId()), r -> {
            btnLike.setEnabled(true);
            if (!r.esOk()) {
                mensajeSuperior.error("No se pudo actualizar el me gusta: " + r.getError());
                return;
            }
            boolean ahora = r.getBool(PaqueteInsta.LIKEADO);
            btnLike.setText(ahora ? "♥" : "♡");
            btnLike.setForeground(ahora ? EstiloInsta.LIKE_ACTIVO : EstiloInsta.LIKE_INACTIVO);
            lblLikes.setText(formatear(r.getInt(PaqueteInsta.LIKES)) + " me gusta");
            if (alCambiar != null) {
                alCambiar.run();
            }
        });
    }

    private void enviarComentario() {
        if (post == null || controlador.getUsuarioActual() == null) {
            return;
        }
        String texto = txtComentario.getText().trim();
        if (texto.isEmpty()) {
            mensajeComentario.error("Escribe algo antes de publicar el comentario.");
            return;
        }
        if (texto.length() > 300) {
            mensajeComentario.error("El comentario es demasiado largo (máximo 300 caracteres).");
            return;
        }
        mensajeComentario.ocultar();
        btnEnviar.setEnabled(false);
        txtComentario.setEnabled(false);
        controlador.enviarAlServidor(PaqueteInsta.comentar(post.getUsernameAutor(), post.getId(), texto), r -> {
            btnEnviar.setEnabled(true);
            txtComentario.setEnabled(true);
            txtComentario.requestFocusInWindow();
            if (!r.esOk()) {
                mensajeComentario.error(r.getError());
                return;
            }
            txtComentario.setText("");
            recargar();
            SwingUtilities.invokeLater(() -> {
                JScrollBar barra = scroll.getVerticalScrollBar();
                barra.setValue(barra.getMaximum());
            });
            if (alCambiar != null) {
                alCambiar.run();
            }
        });
    }

    private void eliminarPost() {
        if (post == null) {
            return;
        }
        btnEliminar.setEnabled(false);
        controlador.enviarAlServidor(PaqueteInsta.eliminarPost(post.getId()), r -> {
            btnEliminar.setEnabled(true);
            if (!r.esOk()) {
                mensajeSuperior.error("No se pudo eliminar la publicación: " + r.getError());
                return;
            }
            Runnable cambio = alCambiar;
            post = null;
            controlador.mostrarSeccion(seccionOrigen);
            controlador.mostrarAviso("Publicación eliminada", "Tu publicación se eliminó correctamente.");
            if (cambio != null) {
                cambio.run();
            }
        });
    }

    private static String formatear(int n) {
        return String.format("%,d", n);
    }
}