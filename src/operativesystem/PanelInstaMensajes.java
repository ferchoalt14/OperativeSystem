package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Bandeja de entrada: lista de conversaciones (DMs) del usuario actual. */
public class PanelInstaMensajes extends JPanel {

    private final InstaControlador controlador;
    private final JPanel panelLista;
    private final JTextField txtNuevoDM;
    private final JLabel lblNotificaciones;

    public PanelInstaMensajes(InstaControlador controlador) {
        super(new BorderLayout(0, 12));
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(20, 26, 20, 26));

        JLabel lblTitulo = new JLabel("Mensajes");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);

        lblNotificaciones = new JLabel("Notificaciones sin leer: 0");
        lblNotificaciones.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblNotificaciones.setForeground(TemaUI.TEXTO_SUAVE);

        txtNuevoDM = TemaUI.crearCampoTexto("Escribir a un username nuevo...", 16);
        JButton btnIniciar = TemaUI.crearBotonPrimario("Iniciar chat");
        btnIniciar.addActionListener(e -> iniciarChatNuevo());

        JPanel panelNuevo = new JPanel(new BorderLayout(8, 0));
        panelNuevo.setOpaque(false);
        panelNuevo.add(txtNuevoDM, BorderLayout.CENTER);
        panelNuevo.add(btnIniciar, BorderLayout.EAST);

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.add(lblTitulo);
        panelEncabezado.add(lblNotificaciones);
        panelEncabezado.add(Box.createVerticalStrut(8));
        panelEncabezado.add(panelNuevo);

        panelLista = new PanelDesplazable(new BorderLayout());
        panelLista.setOpaque(false);
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        add(panelEncabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Actualiza el indicador de notificaciones leído por el hilo de Inbox. */
    public void actualizarNotificaciones(int noLeidas) {
        lblNotificaciones.setText("Notificaciones sin leer: " + Math.max(0, noLeidas));
        lblNotificaciones.setForeground(noLeidas > 0 ? TemaUI.ACCENT_OSCURO : TemaUI.TEXTO_SUAVE);
    }

    private void iniciarChatNuevo() {
        String username = txtNuevoDM.getText().trim();
        if (username.isEmpty()) {
            return;
        }
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual != null && username.equalsIgnoreCase(actual.getUsername())) {
            JOptionPane.showMessageDialog(this, "No puedes enviarte un mensaje a ti mismo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (GestorInstaPlus.buscarPorUsername(username) == null) {
                JOptionPane.showMessageDialog(this, "No existe la cuenta @" + username,
                        "Usuario no encontrado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo verificar el usuario.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        txtNuevoDM.setText("");
        controlador.abrirChatConUsuario(username);
    }

    /** Vuelve a cargar la lista de conversaciones desde disco. */
    public void refrescar() {
        panelLista.removeAll();
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            panelLista.revalidate();
            panelLista.repaint();
            return;
        }

        try {
            ListaEnlazada<String> conversaciones = GestorMensajes.obtenerConversaciones(actual.getUsername());
            if (conversaciones.estaVacia()) {
                JLabel lblVacio = new JLabel("<html><center>✉<br><br>Aún no tienes conversaciones.<br>"
                        + "Escribe un username arriba o entra al perfil de alguien y dale \"Enviar mensaje\".</center></html>",
                        SwingConstants.CENTER);
                lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
                lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblVacio.setBorder(new EmptyBorder(40, 0, 0, 0));
                panelLista.add(lblVacio);
            } else {
                for (String otro : conversaciones) {
                    panelLista.add(crearFilaConversacion(otro, actual.getUsername()));
                    panelLista.add(Box.createVerticalStrut(6));
                }
            }
        } catch (ArchivoCorruptoException ex) {
            panelLista.add(new JLabel("No se pudieron cargar tus conversaciones."));
        }

        panelLista.revalidate();
        panelLista.repaint();
    }

    private JPanel crearFilaConversacion(String otro, String usernameActual) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(true);
        fila.setBackground(TemaUI.SUPERFICIE);
        fila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUI.BORDE, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblAvatar = new JLabel(AvatarHelper.avatarPara(otro, 38));

        int noLeidos = 0;
        String ultimoTexto = "";
        try {
            noLeidos = GestorMensajes.contarNoLeidosDe(usernameActual, otro);
            List<Mensaje> mensajes = GestorMensajes.cargarConversacion(usernameActual, otro);
            if (!mensajes.isEmpty()) {
                ultimoTexto = mensajes.get(mensajes.size() - 1).getTexto();
                if (ultimoTexto.length() > 42) {
                    ultimoTexto = ultimoTexto.substring(0, 42) + "…";
                }
            }
        } catch (ArchivoCorruptoException ex) {
            
        }

        JLabel lblNombre = new JLabel("@" + otro + (noLeidos > 0 ? "  •  " + noLeidos + " nuevo" + (noLeidos > 1 ? "s" : "") : ""));
        lblNombre.setFont(new Font("SansSerif", noLeidos > 0 ? Font.BOLD : Font.PLAIN, 13));
        lblNombre.setForeground(noLeidos > 0 ? TemaUI.ACCENT_OSCURO : TemaUI.TEXTO);

        JLabel lblUltimo = new JLabel(ultimoTexto);
        lblUltimo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblUltimo.setForeground(TemaUI.TEXTO_SUAVE);

        JPanel panelTextos = new JPanel(new GridLayout(2, 1));
        panelTextos.setOpaque(false);
        panelTextos.add(lblNombre);
        panelTextos.add(lblUltimo);

        fila.add(lblAvatar, BorderLayout.WEST);
        fila.add(panelTextos, BorderLayout.CENTER);

        JButton btnEliminar = new JButton("🗑");
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setForeground(new Color(190, 40, 40));
        btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEliminar.setToolTipText("Eliminar conversación");
        btnEliminar.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar la conversación con @" + otro + "? Esta acción no se puede deshacer.",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmacion == JOptionPane.YES_OPTION) {
                GestorMensajes.eliminarConversacion(usernameActual, otro);
                refrescar();
                controlador.refrescarMensajes();
            }
        });
        fila.add(btnEliminar, BorderLayout.EAST);

        fila.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                controlador.abrirChatConUsuario(otro);
            }
        });

        return fila;
    }
}