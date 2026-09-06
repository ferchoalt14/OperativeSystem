package operativesystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Vista inicial del módulo INSTA+.
 *
 * Se monta como el contenido de un JInternalFrame dentro de EscritorioPrincipal
 * y usa un CardLayout interno para alternar entre:
 *  - "LOGIN":      inicio de sesión de INSTA+.
 *  - "REGISTRO":   creación de una cuenta nueva (sin foto de perfil todavía).
 *  - "BIENVENIDA": pantalla post-login, donde recién ahí se puede elegir foto.
 *
 * IMPORTANTE: las cuentas de INSTA+ son EXCLUSIVAS de este módulo. Se manejan
 * con la clase UsuarioInsta y se guardan mediante GestorInstaPlus, en un
 * espacio de archivos binarios totalmente separado del sistema operativo
 * Mini-Windows (que usa Usuario / GestorArchivosBinarios). Registrarte en
 * INSTA+ no crea una cuenta del sistema operativo, y no aparece en
 * "Administrar usuarios".
 */
public class PantallaInstaPlus extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    // --- Campos de la pantalla de Login ---
    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    // --- Campos de la pantalla de Registro ---
    private JTextField txtRegNombre;
    private JComboBox<String> cmbRegGenero;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JSpinner spnRegEdad;

    // --- Pantalla de bienvenida (post-login) ---
    private JLabel lblBienvenida;
    private JLabel lblAvatar;

    // Usuario de INSTA+ actualmente logueado (null si no hay sesión iniciada).
    private UsuarioInsta usuarioInstaActual;

    public PantallaInstaPlus() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(TemaUI.FONDO);

        panelContenedor.setOpaque(false);
        panelContenedor.add(construirPanelLogin(), "LOGIN");
        panelContenedor.add(construirPanelRegistro(), "REGISTRO");
        panelContenedor.add(construirPanelBienvenida(), "BIENVENIDA");

        add(panelContenedor, BorderLayout.CENTER);
        cardLayout.show(panelContenedor, "LOGIN");
    }

    // ============================================================
    //  PANTALLA: LOGIN
    // ============================================================

    private JPanel construirPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIcono = new JLabel(TemaUI.crearIconoCircular("IG", TemaUI.colorApp(1), 64));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblIcono, gbc);

        JLabel lblTitulo = new JLabel("INSTA+", SwingConstants.CENTER);
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
            // Autenticación contra users_insta.ins (EXCLUSIVO de INSTA+, no el del SO).
            UsuarioInsta usuario = GestorInstaPlus.autenticar(username, password);

            if (usuario == null) {
                preguntarQueHacerTrasError("Usuario o contraseña incorrectos.");
                return;
            }

            // Por si la cuenta es de antes de que existiera este flujo, nos aseguramos
            // de que su espacio personal (following.ins, insta.ins, etc.) ya exista.
            GestorInstaPlus.crearArchivosPersonales(usuario.getUsername());

            mostrarBienvenida(usuario);
            txtLoginPassword.setText("");

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
            cardLayout.show(panelContenedor, "REGISTRO");
        } else {
            txtLoginPassword.setText("");
        }
    }

    // ============================================================
    //  PANTALLA: REGISTRO  (sin foto de perfil: eso se elige tras el login)
    // ============================================================

    private JPanel construirPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Crear cuenta en INSTA+", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
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

        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblAviso = new JLabel(
                "<html><i>Podrás elegir tu foto de perfil una vez inicies sesión.</i></html>");
        lblAviso.setFont(lblAviso.getFont().deriveFont(10f));
        lblAviso.setForeground(TemaUI.TEXTO_SUAVE);
        panel.add(lblAviso, gbc);
        gbc.gridwidth = 1;
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

    private void onRegistrar(ActionEvent e) {
        String nombre = txtRegNombre.getText().trim();
        char genero = ((String) cmbRegGenero.getSelectedItem()).charAt(0);
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword());
        int edad = (Integer) spnRegEdad.getValue();

        // Validación: campos obligatorios vacíos.
        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos obligatorios.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación: requisitos de contraseña segura (definidos en TemaUI).
        if (!TemaUI.PATRON_PASSWORD_SEGURA.matcher(password).matches()) {
            JOptionPane.showMessageDialog(this,
                    "La contraseña no cumple los requisitos mínimos:\n" + TemaUI.REQUISITOS_PASSWORD,
                    "Contraseña insegura", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UsuarioInsta nuevo = new UsuarioInsta(nombre, genero, username, password, edad);

        try {
            // La unicidad del username (dentro del universo de INSTA+, no del SO)
            // se valida dentro de registrarUsuario, que lanza UsernameDuplicadoException.
            GestorInstaPlus.registrarUsuario(nuevo);

            JOptionPane.showMessageDialog(this,
                    "Cuenta de INSTA+ creada correctamente. Ya puedes iniciar sesión.",
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
    }

    // ============================================================
    //  PANTALLA: BIENVENIDA (post-login) — aquí sí se elige la foto de perfil
    // ============================================================

    private JPanel construirPanelBienvenida() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);

        lblBienvenida = new JLabel("", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBienvenida.setForeground(TemaUI.ACCENT_OSCURO);

        JPanel panelSuperior = new JPanel();
        panelSuperior.setOpaque(false);
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(lblAvatar);
        panelSuperior.add(Box.createVerticalStrut(10));
        panelSuperior.add(lblBienvenida);

        JButton btnElegirFoto = new JButton("Elegir foto de perfil");
        btnElegirFoto.addActionListener(e -> elegirFotoPerfil());

        JLabel lblPendiente = new JLabel(
                "<html><center>El feed, timeline, inbox y demás secciones<br>" +
                        "de INSTA+ se integrarán aquí en esta misma<br>" +
                        "pantalla (menú único).</center></html>",
                SwingConstants.CENTER);
        lblPendiente.setForeground(TemaUI.TEXTO_SUAVE);

        JPanel panelCentro = new JPanel();
        panelCentro.setOpaque(false);
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        btnElegirFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPendiente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelCentro.add(btnElegirFoto);
        panelCentro.add(Box.createVerticalStrut(16));
        panelCentro.add(lblPendiente);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setForeground(new Color(190, 40, 40));
        btnCerrarSesion.addActionListener(e -> {
            usuarioInstaActual = null;
            txtLoginUsername.setText("");
            cardLayout.show(panelContenedor, "LOGIN");
        });

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelCentro, BorderLayout.CENTER);
        panel.add(btnCerrarSesion, BorderLayout.SOUTH);

        return panel;
    }

    private void mostrarBienvenida(UsuarioInsta usuario) {
        this.usuarioInstaActual = usuario;
        lblBienvenida.setText("¡Bienvenido a INSTA+, @" + usuario.getUsername() + "!");
        actualizarAvatar();
        cardLayout.show(panelContenedor, "BIENVENIDA");
    }

    private void actualizarAvatar() {
        if (usuarioInstaActual == null) return;

        String ruta = usuarioInstaActual.getRutaFotoPerfil();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            lblAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
        } else {
            String nombre = usuarioInstaActual.getNombreCompleto();
            String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
            lblAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(1), 96));
        }
    }

    /**
     * Permite elegir la foto de perfil UNA VEZ que la sesión de INSTA+ ya está
     * iniciada (nunca durante el registro), tal como se pidió: el placeholder
     * de foto se quitó del formulario de creación de cuenta.
     */
    private void elegirFotoPerfil() {
        if (usuarioInstaActual == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            usuarioInstaActual.setRutaFotoPerfil(archivo.getAbsolutePath());
            try {
                GestorInstaPlus.actualizarUsuario(usuarioInstaActual);
                actualizarAvatar();
            } catch (ArchivoCorruptoException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo guardar la foto de perfil: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}