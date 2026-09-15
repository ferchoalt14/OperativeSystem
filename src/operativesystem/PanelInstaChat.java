package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Vista de una conversación individual (DMs). Los mensajes se envían y reciben por sockets:
 * ya no hay un Timer consultando el disco; el panel se actualiza cuando llega un evento.
 */
public class PanelInstaChat extends JPanel {

    private static final int ANCHO_MAX_TEXTO = 260;

    private final InstaControlador controlador;
    private String otroUsuario;
    private String seccionOrigen = "MENSAJES";

    private final JLabel lblAvatar = new JLabel();
    private final JLabel lblTitulo = new JLabel("");
    private final ConfirmacionEnLinea confirmacion = new ConfirmacionEnLinea();
    private final JPanel panelMensajes;
    private final JScrollPane scroll;
    private final JTextField txtMensaje;
    private final JButton btnEnviar;
    private final MensajeEnLinea mensajeEstado = new MensajeEnLinea();

    public PanelInstaChat(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);

        // --- Encabezado ---
        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(true);
        encabezado.setBackground(TemaUI.SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaUI.BORDE),
                new EmptyBorder(10, 12, 10, 14)));

        JButton btnVolver = EstiloInsta.botonTexto("←", TemaUI.ACCENT_OSCURO, 18, true);
        btnVolver.addActionListener(e -> {
            confirmacion.ocultar();
            controlador.mostrarSeccion(seccionOrigen);
        });

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(TemaUI.TEXTO);
        lblTitulo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblTitulo.setToolTipText("Ver perfil");
        lblTitulo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (otroUsuario != null) {
                    controlador.abrirPerfilAjeno(otroUsuario, "CHAT");
                }
            }
        });

        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelIzquierda.setOpaque(false);
        panelIzquierda.add(btnVolver);
        panelIzquierda.add(lblAvatar);
        panelIzquierda.add(lblTitulo);

        JButton btnEliminarChat = EstiloInsta.botonTexto("🗑 Eliminar chat", EstiloInsta.ROJO, 12, false);
        btnEliminarChat.addActionListener(e -> {
            if (otroUsuario != null) {
                confirmacion.preguntar("¿Eliminar toda la conversación con @" + otroUsuario
                        + "? Se borrará para los dos y no se puede deshacer.", "Eliminar chat", this::eliminarChatActual);
            }
        });

        encabezado.add(panelIzquierda, BorderLayout.WEST);
        encabezado.add(btnEliminarChat, BorderLayout.EAST);

        JPanel zonaConfirmacion = new JPanel(new BorderLayout());
        zonaConfirmacion.setOpaque(false);
        zonaConfirmacion.setBorder(new EmptyBorder(6, 14, 0, 14));
        zonaConfirmacion.add(confirmacion, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(encabezado, BorderLayout.NORTH);
        norte.add(zonaConfirmacion, BorderLayout.CENTER);

        // --- Mensajes ---
        panelMensajes = new JPanel();
        panelMensajes.setOpaque(false);
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel envoltorio = new PanelDesplazable(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(panelMensajes, BorderLayout.NORTH);

        scroll = new JScrollPane(envoltorio);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // --- Barra de envío ---
        txtMensaje = EstiloInsta.campoTexto(20);
        txtMensaje.addActionListener(e -> onEnviar());

        btnEnviar = TemaUI.crearBotonPrimario("Enviar");
        btnEnviar.addActionListener(e -> onEnviar());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(SelectorEmojis.crearBoton(txtMensaje));
        panelBotones.add(btnEnviar);

        JPanel filaEnvio = new JPanel(new BorderLayout(8, 0));
        filaEnvio.setOpaque(false);
        filaEnvio.add(txtMensaje, BorderLayout.CENTER);
        filaEnvio.add(panelBotones, BorderLayout.EAST);

        JPanel panelEnvio = new JPanel(new BorderLayout(0, 6));
        panelEnvio.setOpaque(true);
        panelEnvio.setBackground(TemaUI.SUPERFICIE);
        panelEnvio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaUI.BORDE),
                new EmptyBorder(8, 10, 10, 10)));
        panelEnvio.add(mensajeEstado, BorderLayout.NORTH);
        panelEnvio.add(filaEnvio, BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelEnvio, BorderLayout.SOUTH);
    }

    /** Abre (o crea) la conversación con "username". */
    public void abrirCon(String username, String seccionOrigen) {
        this.otroUsuario = username;
        this.seccionOrigen = (seccionOrigen == null || "CHAT".equals(seccionOrigen)) ? "MENSAJES" : seccionOrigen;
        lblTitulo.setText("@" + username);
        lblAvatar.setIcon(AvatarHelper.avatarPara(username, 30));
        confirmacion.ocultar();
        mensajeEstado.ocultar();
        txtMensaje.setText("");
        controlador.mostrarSeccion("CHAT");
        refrescar();
        SwingUtilities.invokeLater(txtMensaje::requestFocusInWindow);
    }

    /** Devuelve con quién es la conversación actualmente abierta (o null si no hay ninguna). */
    public String getUsuarioActivo() {
        return otroUsuario;
    }

    /** Redibuja la conversación desde disco y avisa al servidor si hay mensajes por marcar como leídos. */
    public void refrescar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null || otroUsuario == null) {
            return;
        }
        panelMensajes.removeAll();
        boolean hayNoLeidos = false;
        try {
            List<Mensaje> mensajes = GestorMensajes.cargarConversacion(actual.getUsername(), otroUsuario);
            if (mensajes.isEmpty()) {
                panelMensajes.add(crearEstadoVacio());
            } else {
                String diaAnterior = null;
                for (Mensaje m : mensajes) {
                    String dia = m.getDiaTexto();
                    if (!dia.equals(diaAnterior)) {
                        panelMensajes.add(crearSeparadorDia(dia));
                        diaAnterior = dia;
                    }
                    boolean esMio = m.getRemitente().equalsIgnoreCase(actual.getUsername());
                    if (!esMio && !m.isLeido()) {
                        hayNoLeidos = true;
                    }
                    panelMensajes.add(crearBurbuja(m, esMio));
                    panelMensajes.add(Box.createVerticalStrut(6));
                }
            }
        } catch (ArchivoCorruptoException ex) {
            JLabel error = EstiloInsta.etiqueta("No se pudo cargar la conversación.", 12, false, EstiloInsta.ROJO);
            error.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelMensajes.add(error);
        }
        panelMensajes.revalidate();
        panelMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar barra = scroll.getVerticalScrollBar();
            barra.setValue(barra.getMaximum());
        });

        // Solo marcamos como leído si de verdad se está viendo el chat.
        if (hayNoLeidos && isShowing()) {
            controlador.enviarAlServidor(PaqueteInsta.marcarLeidos(otroUsuario), null);
        }
    }

    private void eliminarChatActual() {
        if (controlador.getUsuarioActual() == null || otroUsuario == null) {
            return;
        }
        controlador.enviarAlServidor(PaqueteInsta.eliminarChat(otroUsuario), r -> {
            if (!r.esOk()) {
                mensajeEstado.error("No se pudo eliminar el chat: " + r.getError());
                return;
            }
            controlador.refrescarMensajes();
            controlador.mostrarSeccion(seccionOrigen);
        });
    }

    private void onEnviar() {
        if (controlador.getUsuarioActual() == null || otroUsuario == null) {
            return;
        }
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        if (texto.length() > 1000) {
            mensajeEstado.error("El mensaje es demasiado largo (máximo 1000 caracteres).");
            return;
        }
        mensajeEstado.ocultar();
        btnEnviar.setEnabled(false);
        String destino = otroUsuario;
        controlador.enviarAlServidor(PaqueteInsta.mensaje(destino, texto), r -> {
            btnEnviar.setEnabled(true);
            txtMensaje.requestFocusInWindow();
            if (!r.esOk()) {
                mensajeEstado.error("No se pudo enviar el mensaje: " + r.getError());
                return;
            }
            if (destino.equals(otroUsuario)) {
                txtMensaje.setText("");
            }
            // El redibujado llega con el evento MENSAJE_NUEVO que empuja el servidor.
        });
    }

    // ------------------------------------------------------------------------------------------

    private JComponent crearEstadoVacio() {
        JPanel p = EstiloInsta.filaAjustada(new GridBagLayout());
        p.setBorder(new EmptyBorder(40, 0, 0, 0));
        JLabel lbl = new JLabel("<html><center><font size='6'>👋</font><br><br>Aún no hay mensajes.<br>"
                + "¡Envía el primero!</center></html>", SwingConstants.CENTER);
        lbl.setForeground(TemaUI.TEXTO_SUAVE);
        p.add(lbl);
        return p;
    }

    private JComponent crearSeparadorDia(String dia) {
        JPanel fila = EstiloInsta.filaAjustada(new FlowLayout(FlowLayout.CENTER, 0, 6));
        EstiloInsta.PanelRedondeado chip = new EstiloInsta.PanelRedondeado(new BorderLayout(),
                EstiloInsta.mezclar(TemaUI.FONDO, TemaUI.TEXTO, 0.07), null, 14);
        chip.setBorder(new EmptyBorder(3, 10, 3, 10));
        chip.add(EstiloInsta.etiqueta(dia, 10, false, TemaUI.TEXTO_SUAVE));
        fila.add(chip);
        return fila;
    }

    private JPanel crearBurbuja(Mensaje m, boolean esMio) {
        JPanel fila = EstiloInsta.filaAjustada(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));

        Color fondo = esMio ? TemaUI.ACCENT_OSCURO : EstiloInsta.mezclar(TemaUI.SUPERFICIE, TemaUI.TEXTO, 0.08);
        Color colorTexto = esMio ? Color.WHITE : TemaUI.TEXTO;

        EstiloInsta.PanelRedondeado burbuja = new EstiloInsta.PanelRedondeado(null, fondo, null, 18);
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBorder(new EmptyBorder(8, 12, 6, 12));

        Font fuente = new Font("SansSerif", Font.PLAIN, 13);
        String html = EstiloInsta.escaparHtml(m.getTexto());
        int ancho = getFontMetrics(fuente).stringWidth(m.getTexto());
        JLabel lblTexto = new JLabel(ancho > ANCHO_MAX_TEXTO
                ? "<html><body style='width:" + ANCHO_MAX_TEXTO + "px'>" + html + "</body></html>"
                : "<html>" + html + "</html>");
        lblTexto.setFont(fuente);
        lblTexto.setForeground(colorTexto);
        lblTexto.setAlignmentX(esMio ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        String estado = m.getHoraTexto();
        if (esMio) {
            estado += m.isLeido() ? "  ✓✓ Visto" : "  ✓ Enviado";
        }
        JLabel lblEstado = new JLabel(estado);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblEstado.setForeground(esMio ? new Color(255, 255, 255, 190) : TemaUI.TEXTO_SUAVE);
        lblEstado.setAlignmentX(esMio ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        lblEstado.setBorder(new EmptyBorder(3, 0, 0, 0));

        burbuja.add(lblTexto);
        burbuja.add(lblEstado);
        fila.add(burbuja);
        return fila;
    }
}