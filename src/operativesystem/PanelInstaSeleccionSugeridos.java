package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PanelInstaSeleccionSugeridos extends JPanel {

    private static final int MINIMO_A_SEGUIR = 4;

    private final InstaControlador controlador;
    private final JPanel panelLista;
    private final List<JCheckBox> checks = new ArrayList<>();
    private UsuarioInsta usuarioPendiente;

    public PanelInstaSeleccionSugeridos(InstaControlador controlador) {
        super(new BorderLayout(0, 14));
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel lblTitulo = new JLabel("Elige cuentas para seguir", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);

        JLabel lblSubtitulo = new JLabel(
                "Selecciona al menos " + MINIMO_A_SEGUIR + " cuentas para continuar",
                SwingConstants.CENTER);
        lblSubtitulo.setForeground(TemaUI.TEXTO_SUAVE);

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.add(lblTitulo);
        panelEncabezado.add(Box.createVerticalStrut(6));
        panelEncabezado.add(lblSubtitulo);
        panelEncabezado.setBorder(new EmptyBorder(0, 0, 14, 0));

        panelLista = new PanelDesplazable(new BorderLayout());
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JButton btnContinuar = TemaUI.crearBotonPrimario("Continuar");
        btnContinuar.addActionListener(e -> continuar());
        JPanel panelBoton = new JPanel();
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(14, 0, 0, 0));
        panelBoton.add(btnContinuar);

        add(panelEncabezado, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

 
    public void mostrarPara(UsuarioInsta usuario) {
        this.usuarioPendiente = usuario;
        checks.clear();
        panelLista.removeAll();

        for (String[] datos : GestorInstaPlus.getCuentasPorDefecto()) {
            String nombre = datos[0];
            String username = datos[1];
            panelLista.add(crearFila(nombre, username));
            panelLista.add(Box.createVerticalStrut(6));
        }

        panelLista.revalidate();
        panelLista.repaint();
    }

    private JPanel crearFila(String nombre, String username) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        fila.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblAvatar = new JLabel(TemaUI.crearIconoCircular(
                username.substring(0, 1).toUpperCase(), TemaUI.colorApp(username.hashCode()), 36));
        fila.add(lblAvatar, BorderLayout.WEST);

        JCheckBox chk = new JCheckBox("@" + username + "   —   " + nombre);
        chk.setOpaque(false);
        chk.setForeground(TemaUI.TEXTO);
        chk.putClientProperty("username", username);
        checks.add(chk);
        fila.add(chk, BorderLayout.CENTER);

        return fila;
    }

    private void continuar() {
        List<String> seleccionadas = new ArrayList<>();
        for (JCheckBox chk : checks) {
            if (chk.isSelected()) {
                seleccionadas.add((String) chk.getClientProperty("username"));
            }
        }

        if (seleccionadas.size() < MINIMO_A_SEGUIR) {
            JOptionPane.showMessageDialog(this,
                    "Debes elegir al menos " + MINIMO_A_SEGUIR + " cuentas para continuar.",
                    "Selección insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            GestorInstaPlus.seguirVarias(usuarioPendiente.getUsername(), seleccionadas);
            UsuarioInsta usuario = usuarioPendiente;
            usuarioPendiente = null;
            controlador.ingresarAlApp(usuario);
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron guardar las cuentas seguidas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
