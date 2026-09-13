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

    public PanelInstaBuscar(InstaControlador controlador) {
        super(new BorderLayout(0, 12));
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 24, 28));

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

        panelResultados = new JPanel();
        panelResultados.setOpaque(false);
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        add(panelEncabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
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
                List<String> misSeguidos;
                try {
                    misSeguidos = GestorInstaPlus.obtenerFollowing(actual.getUsername());
                } catch (ArchivoCorruptoException ex) {
                    misSeguidos = new ArrayList<>();
                }
                for (String username : coincidencias) {
                    boolean loSigo = misSeguidos.contains(username);
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

        JLabel lblAvatar = new JLabel(TemaUI.crearIconoCircular(
                username.substring(0, 1).toUpperCase(), TemaUI.colorApp(username.hashCode()), 36));
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