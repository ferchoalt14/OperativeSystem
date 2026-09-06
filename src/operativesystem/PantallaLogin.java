package operativesystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;



public class PantallaLogin extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;


    private JTextField txtRegNombre;
    private JComboBox<String> cmbRegGenero;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JSpinner spnRegEdad;
    private JLabel lblRegFoto;
    private String rutaFotoSeleccionada = null;

    public PantallaLogin() {
        super("Mini-Windows - Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 440);
        setLocationRelativeTo(null);
        setResizable(false);

        panelContenedor.setBackground(TemaUI.FONDO);
        panelContenedor.add(construirPanelLogin(), "LOGIN");
        panelContenedor.add(construirPanelRegistro(), "REGISTRO");
        add(panelContenedor);

        cardLayout.show(panelContenedor, "LOGIN");
    }

    
    private JPanel construirPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(TemaUI.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIcono = new JLabel(TemaUI.crearIconoCircular("MW", TemaUI.ACCENT, 64));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblIcono, gbc);

        JLabel lblTitulo = new JLabel("Mini-Windows", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        gbc.gridy = 1;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtLoginUsername = new JTextField(15);
        panel.add(txtLoginUsername, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtLoginPassword = new JPasswordField(15);
        panel.add(TemaUI.crearCampoPassword(txtLoginPassword), gbc);

        JButton btnLogin = TemaUI.crearBotonPrimario("Log In");
        btnLogin.addActionListener(this::onLogin);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        JButton btnIrRegistro = new JButton("Crear cuenta");
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setForeground(TemaUI.ACCENT_OSCURO);
        btnIrRegistro.addActionListener(e -> cardLayout.show(panelContenedor, "REGISTRO"));
        gbc.gridy = 5;
        panel.add(btnIrRegistro, gbc);

        return panel;
    }

    private void onLogin(ActionEvent e) {
        String username = txtLoginUsername.getText().trim();
        String password = new String(txtLoginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa usuario y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuario = GestorArchivosBinarios.autenticar(username, password);

            if (usuario == null) {
                preguntarQueHacerTrasError("Usuario o contraseña incorrectos.");
                return;
            }

            
            SwingUtilities.invokeLater(() -> {
                new EscritorioPrincipal(usuario).setVisible(true);
            });
            dispose();

        } catch (CuentaDesactivadaException ex) {
            preguntarQueHacerTrasError(ex.getMessage());
        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer la base de usuarios: " + ex.getMessage(),
                    "Error de archivo", JOptionPane.ERROR_MESSAGE);
        }
    }

   
    private void preguntarQueHacerTrasError(String mensaje) {
        Object[] opciones = {"Reintentar login", "Crear cuenta nueva"};
        int seleccion = JOptionPane.showOptionDialog(this, mensaje, "Error de inicio de sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, opciones, opciones[0]);

        if (seleccion == 1) {
            cardLayout.show(panelContenedor, "REGISTRO");
        } else {
            txtLoginPassword.setText("");
        }
    }


    private JPanel construirPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(TemaUI.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Crear cuenta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;

        int fila = 1;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        txtRegNombre = new JTextField(15);
        panel.add(txtRegNombre, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Género:"), gbc);
        gbc.gridx = 1;
        cmbRegGenero = new JComboBox<>(new String[]{"M", "F"});
        panel.add(cmbRegGenero, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtRegUsername = new JTextField(15);
        panel.add(txtRegUsername, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtRegPassword = new JPasswordField(15);
        panel.add(TemaUI.crearCampoPassword(txtRegPassword), gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 1;
        JLabel lblRequisitos = new JLabel("<html>" + TemaUI.REQUISITOS_PASSWORD + "</html>");
        lblRequisitos.setFont(lblRequisitos.getFont().deriveFont(Font.PLAIN, 10f));
        lblRequisitos.setForeground(TemaUI.TEXTO_SUAVE);
        panel.add(lblRequisitos, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Edad:"), gbc);
        gbc.gridx = 1;
        spnRegEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        panel.add(spnRegEdad, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        panel.add(new JLabel("Foto de perfil:"), gbc);
        gbc.gridx = 1;
        JPanel panelFoto = new JPanel(new BorderLayout(5, 0));
        panelFoto.setOpaque(false);
        lblRegFoto = new JLabel("(ninguna seleccionada)");
        JButton btnElegirFoto = new JButton("Elegir...");
        btnElegirFoto.addActionListener(e -> elegirFoto());
        panelFoto.add(lblRegFoto, BorderLayout.CENTER);
        panelFoto.add(btnElegirFoto, BorderLayout.EAST);
        panel.add(panelFoto, gbc);
        fila++;

        JButton btnRegistrar = TemaUI.crearBotonPrimario("Registrar");
        btnRegistrar.addActionListener(this::onRegistrar);
        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnRegistrar, gbc);
        fila++;

        JButton btnVolver = new JButton("Volver al Log In");
        btnVolver.setContentAreaFilled(false);
        btnVolver.setForeground(TemaUI.ACCENT_OSCURO);
        btnVolver.addActionListener(e -> cardLayout.show(panelContenedor, "LOGIN"));
        gbc.gridy = fila;
        panel.add(btnVolver, gbc);

        return panel;
    }

    private void elegirFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            rutaFotoSeleccionada = archivo.getAbsolutePath();
            lblRegFoto.setText(archivo.getName());
        }
    }

    private void onRegistrar(ActionEvent e) {
        String nombre = txtRegNombre.getText().trim();
        char genero = ((String) cmbRegGenero.getSelectedItem()).charAt(0);
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword());
        int edad = (Integer) spnRegEdad.getValue();

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

        Usuario nuevo = new Usuario(nombre, genero, username, password, edad,
                rutaFotoSeleccionada, false);

        try {
            GestorArchivosBinarios.registrarUsuario(nuevo);
            JOptionPane.showMessageDialog(this,
                    "Cuenta creada correctamente. Ya puedes iniciar sesión.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormularioRegistro();
            cardLayout.show(panelContenedor, "LOGIN");

        } catch (UsernameDuplicadoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Username no disponible", JOptionPane.ERROR_MESSAGE);
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar la cuenta: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioRegistro() {
        txtRegNombre.setText("");
        txtRegUsername.setText("");
        txtRegPassword.setText("");
        spnRegEdad.setValue(18);
        cmbRegGenero.setSelectedIndex(0);
        lblRegFoto.setText("(ninguna seleccionada)");
        rutaFotoSeleccionada = null;
    }
}