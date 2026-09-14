package operativesystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class PanelInstaRegistro extends JPanel {

    private final InstaControlador controlador;

    private final JTextField txtNombre;
    private final JComboBox<String> cmbGenero;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JSpinner spnEdad;

    public PanelInstaRegistro(InstaControlador controlador) {
        super(new GridBagLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Crear cuenta en INSTA+", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);
        gbc.gridwidth = 1;

        int fila = 1;

        gbc.gridy = fila; gbc.gridx = 0;
        add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(15);
        add(txtNombre, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        add(new JLabel("Género:"), gbc);
        gbc.gridx = 1;
        cmbGenero = new JComboBox<>(new String[]{"M", "F"});
        add(cmbGenero, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        add(txtUsername, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        add(TemaUI.crearCampoPassword(txtPassword), gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 1;
        JLabel lblRequisitos = new JLabel("<html>" + TemaUI.REQUISITOS_PASSWORD + "</html>");
        lblRequisitos.setFont(lblRequisitos.getFont().deriveFont(Font.PLAIN, 10f));
        lblRequisitos.setForeground(TemaUI.TEXTO_SUAVE);
        add(lblRequisitos, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        add(new JLabel("Edad:"), gbc);
        gbc.gridx = 1;
        spnEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        add(spnEdad, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblAviso = new JLabel(
                "<html><i>Al registrarte podrás elegir qué cuentas destacadas<br>"
                + "de INSTA+ seguir (mínimo 4, entre fútbol y música).<br>"
                + "Podrás elegir tu foto de perfil una vez dentro.</i></html>");
        lblAviso.setFont(lblAviso.getFont().deriveFont(10f));
        lblAviso.setForeground(TemaUI.TEXTO_SUAVE);
        add(lblAviso, gbc);
        gbc.gridwidth = 1;
        fila++;

        JButton btnRegistrar = TemaUI.crearBotonPrimario("Registrar");
        btnRegistrar.addActionListener(this::onRegistrar);
        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnRegistrar, gbc);
        fila++;

        JButton btnVolver = new JButton("Volver al Log In");
        btnVolver.setContentAreaFilled(false);
        btnVolver.setForeground(TemaUI.ACCENT_OSCURO);
        btnVolver.addActionListener(e -> controlador.mostrarPantallaRaiz("LOGIN"));
        gbc.gridy = fila;
        add(btnVolver, gbc);
    }

    private void onRegistrar(ActionEvent e) {
        String nombre = txtNombre.getText().trim();
        char genero = ((String) cmbGenero.getSelectedItem()).charAt(0);
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        int edad = (Integer) spnEdad.getValue();

        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos obligatorios.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!TemaUI.PATRON_PASSWORD_SEGURA.matcher(password).matches()) {
            JOptionPane.showMessageDialog(this,
                    "La contraseña no cumple los requisitos mínimos:\n" + TemaUI.REQUISITOS_PASSWORD,
                    "Contraseña insegura", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UsuarioInsta nuevo = new UsuarioInsta(nombre, genero, username, password, edad);

        try {

            GestorInstaPlus.registrarUsuario(nuevo);
            limpiarFormulario();
           
            controlador.mostrarSeleccionSugeridos(nuevo);

        } catch (UsernameDuplicadoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Username no disponible", JOptionPane.ERROR_MESSAGE);
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar la cuenta: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        spnEdad.setValue(18);
        cmbGenero.setSelectedIndex(0);
    }
}