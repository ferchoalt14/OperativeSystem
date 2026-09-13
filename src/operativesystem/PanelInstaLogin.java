package operativesystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/** Pantalla de "Log In" de INSTA+. */
public class PanelInstaLogin extends JPanel {

    private final InstaControlador controlador;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;

    public PanelInstaLogin(InstaControlador controlador) {
        super(new GridBagLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIcono = new JLabel(TemaUI.crearIconoCircular("IG", TemaUI.colorApp(1), 64));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblIcono, gbc);

        JLabel lblTitulo = new JLabel("INSTA+", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        gbc.gridy = 1;
        add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        add(txtUsername, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        add(TemaUI.crearCampoPassword(txtPassword), gbc);

        JButton btnLogin = TemaUI.crearBotonPrimario("Log In");
        btnLogin.addActionListener(this::onLogin);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnLogin, gbc);

        JButton btnIrRegistro = new JButton("Crear cuenta");
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setForeground(TemaUI.ACCENT_OSCURO);
        btnIrRegistro.addActionListener(e -> controlador.mostrarPantallaRaiz("REGISTRO"));
        gbc.gridy = 5;
        add(btnIrRegistro, gbc);
    }

    public void limpiarPassword() {
        txtPassword.setText("");
    }

    public void limpiarUsername() {
        txtUsername.setText("");
    }

    private void onLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa usuario y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UsuarioInsta usuario = GestorInstaPlus.autenticar(username, password);

            if (usuario == null) {
                preguntarQueHacerTrasError("Usuario o contraseña incorrectos.");
                return;
            }

            GestorInstaPlus.crearArchivosPersonales(usuario.getUsername());
            txtPassword.setText("");
            controlador.ingresarAlApp(usuario);

        } catch (CuentaDesactivadaException ex) {
            preguntarQueHacerTrasError(ex.getMessage());
        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer la base de usuarios de INSTA+: " + ex.getMessage(),
                    "Error de archivo", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo preparar el espacio de INSTA+: " + ex.getMessage(),
                    "Error de archivo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preguntarQueHacerTrasError(String mensaje) {
        Object[] opciones = {"Reintentar login", "Crear cuenta nueva"};
        int seleccion = JOptionPane.showOptionDialog(this, mensaje, "Error de inicio de sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == 1) {
            controlador.mostrarPantallaRaiz("REGISTRO");
        } else {
            txtPassword.setText("");
        }
    }
}