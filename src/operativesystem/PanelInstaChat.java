package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

/** Vista de una conversación individual (mensajería directa / DMs). */
public class PanelInstaChat extends JPanel {

    private final InstaControlador controlador;
    private String otroUsuario;
    private String seccionOrigen = "MENSAJES";

    private final JLabel lblAvatar;    private final JLabel lblTitulo;
    private final JPanel panelMensajes;
    private final JScrollPane scroll;
    private final JTextField txtMensaje;

    private final Timer timerActualizacion;

    public PanelInstaChat(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // --- Encabezado ---
        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(true);
        encabezado.setBackground(TemaUI.SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaUI.BORDE),
                new EmptyBorder(10, 14, 10, 14)));

        JButton btnVolver = new JButton("←");
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setForeground(TemaUI.ACCENT_OSCURO);
        btnVolver.setFont(btnVolver.getFont().deriveFont(18f));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> controlador.mostrarSeccion(seccionOrigen));

        lblAvatar = new JLabel();
        lblTitulo = new JLabel("");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(TemaUI.TEXTO);

        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelIzquierda.setOpaque(false);
        panelIzquierda.add(btnVolver);
        panelIzquierda.add(lblAvatar);
        panelIzquierda.add(lblTitulo);

        encabezado.add(panelIzquierda, BorderLayout.WEST);

        // --- Mensajes ---
        panelMensajes = new PanelDesplazable(new BorderLayout());
        panelMensajes.setOpaque(false);
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBorder(new EmptyBorder(12, 14, 12, 14));

        scroll = new JScrollPane(panelMensajes);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // --- Barra de envío ---
        JPanel panelEnvio = new JPanel(new BorderLayout(8, 0));
        panelEnvio.setOpaque(true);
        panelEnvio.setBackground(TemaUI.SUPERFICIE);
        panelEnvio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaUI.BORDE),
                new EmptyBorder(8, 10, 8, 10)));

        txtMensaje = new JTextField();
        txtMensaje.addActionListener(this::onEnviar);

        JButton btnEmoji = SelectorEmojis.crearBoton(txtMensaje);
        JButton btnEnviar = TemaUI.crearBotonPrimario("Enviar");
        btnEnviar.addActionListener(this::onEnviar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnEmoji);
        panelBotones.add(btnEnviar);

        panelEnvio.add(txtMensaje, BorderLayout.CENTER);
        panelEnvio.add(panelBotones, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelEnvio, BorderLayout.SOUTH);

        // Refresca cada 4s mientras el chat esté visible (llega mensaje del otro usuario / se marca leído).
        timerActualizacion = new Timer(4000, e -> {
            if (isShowing() && otroUsuario != null) {
                try {
                    refrescarMensajes();
                } catch (IOException ex) {
                    System.getLogger(PanelInstaChat.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        });
        timerActualizacion.start();
    }

    /** Abre (o crea) la conversación con "username". */
    public void abrirCon(String username, String seccionOrigen) throws IOException {
        this.otroUsuario = username;
        this.seccionOrigen = seccionOrigen;
        lblTitulo.setText("@" + username);
        lblAvatar.setIcon(AvatarHelper.avatarPara(username, 28));

        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual != null) {
            try {
                GestorMensajes.marcarComoLeidos(actual.getUsername(), username);
            } catch (ArchivoCorruptoException | IOException ex) {
                
            }
        }
        refrescarMensajes();
        controlador.mostrarSeccion("CHAT");
    }

    /** Devuelve con quién es la conversación actualmente abierta (o null si no hay ninguna). */
    public String getUsuarioActivo() {
        return otroUsuario;
    }

    private void onEnviar(ActionEvent e) {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null || otroUsuario == null) {
            return;
        }
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        try {
            GestorMensajes.enviarMensaje(actual.getUsername(), otroUsuario, texto);
            txtMensaje.setText("");
            refrescarMensajes();
            controlador.refrescarMensajes();
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo enviar el mensaje: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarMensajes() throws IOException {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null || otroUsuario == null) {
            return;
        }
        panelMensajes.removeAll();
        try {
            List<Mensaje> mensajes = GestorMensajes.cargarConversacion(actual.getUsername(), otroUsuario);
            // Marcar como leídos los que me llegaron mientras tenía el chat abierto.
            GestorMensajes.marcarComoLeidos(actual.getUsername(), otroUsuario);

            if (mensajes.isEmpty()) {
                JLabel lblVacio = new JLabel("Aún no hay mensajes. ¡Envía el primero! 👋");
                lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
                lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelMensajes.add(lblVacio);
            } else {
                for (Mensaje m : mensajes) {
                    boolean esMio = m.getRemitente().equalsIgnoreCase(actual.getUsername());
                    panelMensajes.add(crearBurbuja(m, esMio));
                    panelMensajes.add(Box.createVerticalStrut(6));
                }
            }
        } catch (ArchivoCorruptoException ex) {
            panelMensajes.add(new JLabel("No se pudo cargar la conversación."));
        }
        panelMensajes.revalidate();
        panelMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar barra = scroll.getVerticalScrollBar();
            barra.setValue(barra.getMaximum());
        });
    }

    private JPanel crearBurbuja(Mensaje m, boolean esMio) {
        JPanel fila = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 2));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel burbuja = new JPanel();
        burbuja.setLayout(new BoxLayout(burbuja, BoxLayout.Y_AXIS));
        burbuja.setBackground(esMio ? TemaUI.ACCENT_OSCURO : TemaUI.FONDO);
        burbuja.setBorder(new EmptyBorder(8, 12, 6, 12));
        burbuja.setMaximumSize(new Dimension(280, 200));

        JLabel lblTexto = new JLabel("<html><body style='width: 220px'>" + m.getTexto() + "</body></html>");
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblTexto.setForeground(esMio ? Color.WHITE : TemaUI.TEXTO);
        lblTexto.setAlignmentX(Component.LEFT_ALIGNMENT);

        String estado = m.getHoraTexto();
        if (esMio) {
            estado += m.isLeido() ? "  ✓✓ Leído" : "  ✓ Enviado";
        }
        JLabel lblEstado = new JLabel(estado);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblEstado.setForeground(esMio ? new Color(230, 230, 230) : TemaUI.TEXTO_SUAVE);
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        burbuja.add(lblTexto);
        burbuja.add(lblEstado);

        fila.add(burbuja);
        return fila;
    }
}