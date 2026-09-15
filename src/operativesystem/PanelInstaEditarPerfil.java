package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/** Pantalla para editar el username, nombre completo, descripción y estado (activa/inactiva) del perfil propio. */
public class PanelInstaEditarPerfil extends JPanel {

    private final InstaControlador controlador;

    private final JTextField txtNombre;
    private final JTextField txtUsername;
    private final JTextArea txtDescripcion;
    private final JButton btnEstadoCuenta;

    public PanelInstaEditarPerfil(InstaControlador controlador) {
        super(new GridBagLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Editar perfil", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        add(txtNombre, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        add(txtUsername, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        add(scrollDesc, gbc);

        JButton btnGuardar = TemaUI.crearBotonPrimario("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnGuardar, gbc);
        gbc.gridwidth = 1;

        JSeparator separador = new JSeparator();
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        add(separador, gbc);

        JLabel lblEstadoTitulo = new JLabel("Estado de la cuenta");
        lblEstadoTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEstadoTitulo.setForeground(TemaUI.TEXTO);
        gbc.gridy = 6;
        add(lblEstadoTitulo, gbc);

        btnEstadoCuenta = new JButton();
        btnEstadoCuenta.addActionListener(e -> alternarEstadoCuenta());
        gbc.gridy = 7;
        add(btnEstadoCuenta, gbc);
        gbc.gridwidth = 1;
    }

    /** Llamar cada vez que se muestre este panel, para cargar los datos actuales del usuario. */
    public void cargarDatos() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        txtNombre.setText(actual.getNombreCompleto());
        txtUsername.setText(actual.getUsername());
        txtDescripcion.setText(actual.getDescripcion());
        actualizarBotonEstado(actual);
    }

    private void actualizarBotonEstado(UsuarioInsta actual) {
        if (actual.isActiva()) {
            btnEstadoCuenta.setText("Desactivar cuenta");
            btnEstadoCuenta.setForeground(new Color(190, 40, 40));
        } else {
            btnEstadoCuenta.setText("Reactivar cuenta");
            btnEstadoCuenta.setForeground(new Color(40, 140, 60));
        }
    }

    private void guardar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        String nuevoNombre = txtNombre.getText().trim();
        String nuevoUsername = txtUsername.getText().trim();
        String nuevaDescripcion = txtDescripcion.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y el username no pueden estar vacíos.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String usernameOriginal = actual.getUsername();

            if (!nuevoUsername.equalsIgnoreCase(usernameOriginal)) {
                UsuarioInsta existente = GestorInstaPlus.buscarPorUsername(nuevoUsername);
                if (existente != null) {
                    JOptionPane.showMessageDialog(this, "Ese username ya está en uso por otra cuenta.",
                            "Username no disponible", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            actual.setNombreCompleto(nuevoNombre);
            actual.setUsername(nuevoUsername);
            actual.setDescripcion(nuevaDescripcion);

            GestorInstaPlus.actualizarUsuario(actual, usernameOriginal);
            controlador.refrescarPerfilPropio();

            JOptionPane.showMessageDialog(this, "¡Perfil actualizado!",
                    "Listo", JOptionPane.INFORMATION_MESSAGE);

        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar el perfil: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarEstadoCuenta() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        boolean estaActiva = actual.isActiva();
        String mensaje = estaActiva
                ? "¿Seguro que quieres desactivar tu cuenta? No aparecerás en búsquedas ni se mostrarán tus publicaciones mientras esté desactivada."
                : null;

        if (estaActiva) {
            int confirmacion = JOptionPane.showConfirmDialog(this, mensaje,
                    "Confirmar desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            actual.setActiva(!estaActiva);
            GestorInstaPlus.actualizarUsuario(actual, actual.getUsername());
            actualizarBotonEstado(actual);

            JOptionPane.showMessageDialog(this,
                    estaActiva ? "Tu cuenta fue desactivada." : "¡Tu cuenta fue reactivada!",
                    "Listo", JOptionPane.INFORMATION_MESSAGE);

        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el estado de la cuenta: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}