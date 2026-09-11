package operativesystem;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PantallaLogin extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    private JLabel lblReloj;
    private JLabel lblFecha;
    private Timer timerReloj;

    private JTextField txtRegNombre;
    private JComboBox<String> cmbRegGenero;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JSpinner spnRegEdad;
    private JLabel lblRegFoto;
    private String rutaFotoSeleccionada = null;
    private JComboBox<String> cmbRegRol;
    private JLabel lblEstadoRegistro;

    public PantallaLogin() {
        super("Mini-Windows");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int ancho = Math.max(1366, (int) (pantalla.width * 0.85));
        int alto = Math.max(768, (int) (pantalla.height * 0.85));
        setMinimumSize(new Dimension(1024, 650));
        setSize(ancho, alto);
        setLocationRelativeTo(null);

        panelContenedor.add(construirPanelLogin(), "LOGIN");
        panelContenedor.add(construirPanelRegistro(), "REGISTRO");
        add(panelContenedor);

        cardLayout.show(panelContenedor, "LOGIN");

      
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

   

    private JPanel construirPanelLogin() {
        JPanel fondo = TemaUI.crearFondoDegradado(TemaUI.LOGIN_GRAD_INICIO, TemaUI.LOGIN_GRAD_FIN);
        fondo.setLayout(new BorderLayout());

       
        JPanel bloqueReloj = new JPanel();
        bloqueReloj.setOpaque(false);
        bloqueReloj.setLayout(new BoxLayout(bloqueReloj, BoxLayout.Y_AXIS));
        bloqueReloj.setBorder(BorderFactory.createEmptyBorder(26, 0, 0, 36));

        lblReloj = new JLabel();
        lblReloj.setFont(new Font("Segoe UI Light", Font.PLAIN, 46));
        lblReloj.setForeground(Color.WHITE);
        lblReloj.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblFecha = new JLabel();
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblFecha.setForeground(new Color(255, 255, 255, 225));
        lblFecha.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bloqueReloj.add(lblReloj);
        bloqueReloj.add(lblFecha);

        JPanel envoltorioReloj = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        envoltorioReloj.setOpaque(false);
        envoltorioReloj.add(bloqueReloj);
        fondo.add(envoltorioReloj, BorderLayout.NORTH);

        actualizarReloj();
        timerReloj = new Timer(1000, e -> actualizarReloj());
        timerReloj.start();

        
        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel lblAvatar = new JLabel(TemaUI.crearIconoPersonaGenerica(120));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(lblAvatar);
        centro.add(Box.createVerticalStrut(14));

        JLabel lblOtroUsuario = new JLabel("Usuario");
        lblOtroUsuario.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        lblOtroUsuario.setForeground(Color.WHITE);
        lblOtroUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(lblOtroUsuario);
        centro.add(Box.createVerticalStrut(22));

        txtLoginUsername = TemaUI.crearCampoTexto("Nombre de usuario", 18);
        txtLoginUsername.setPreferredSize(new Dimension(340, 50));
        txtLoginUsername.setMaximumSize(new Dimension(340, 50));
        txtLoginUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(txtLoginUsername);
        centro.add(Box.createVerticalStrut(10));

        txtLoginPassword = TemaUI.crearCampoPasswordEstilizado("Contraseña", 18);
        txtLoginPassword.addActionListener(this::onLogin);


        JPanel panelPassword = TemaUI.crearCampoPassword(txtLoginPassword);
        panelPassword.setPreferredSize(new Dimension(340, 50));
        panelPassword.setMaximumSize(new Dimension(340, 50));
        panelPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(panelPassword);

        centro.add(Box.createVerticalStrut(16));

       
        JButton btnIniciarSesion = TemaUI.crearBotonPrimario("Iniciar sesión");
        btnIniciarSesion.setPreferredSize(new Dimension(340, 46));
        btnIniciarSesion.setMaximumSize(new Dimension(340, 46));
        btnIniciarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciarSesion.addActionListener(this::onLogin);
        centro.add(btnIniciarSesion);

        centro.add(Box.createVerticalStrut(24));

        JButton btnCrearCuenta = new JButton("¿No tienes cuenta? Crea una");
        estilizarEnlaceClaro(btnCrearCuenta);
        btnCrearCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrearCuenta.addActionListener(e -> cardLayout.show(panelContenedor, "REGISTRO"));
        centro.add(btnCrearCuenta);

        JPanel envoltorioCentro = new JPanel(new GridBagLayout());
        envoltorioCentro.setOpaque(false);
        envoltorioCentro.add(centro);
        fondo.add(envoltorioCentro, BorderLayout.CENTER);

        // --- Barra inferior con iconos, como la pantalla de bloqueo de Windows ---
        JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 18));
        barraInferior.setOpaque(false);
        barraInferior.add(crearBotonIconoInferior("\uD83C\uDF10", "Red"));
        barraInferior.add(crearBotonIconoInferior("\u267F", "Accesibilidad"));
        JButton btnApagar = crearBotonIconoInferior("\u23FB", "Apagar");
        btnApagar.addActionListener(e -> confirmarSalida());
        barraInferior.add(btnApagar);
        fondo.add(barraInferior, BorderLayout.SOUTH);

        return fondo;
    }

    private void actualizarReloj() {
        Date ahora = new Date();
        lblReloj.setText(new SimpleDateFormat("HH:mm").format(ahora));
        String fechaTexto = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES")).format(ahora);
        if (!fechaTexto.isEmpty()) {
            fechaTexto = Character.toUpperCase(fechaTexto.charAt(0)) + fechaTexto.substring(1);
        }
        lblFecha.setText(fechaTexto);
    }

    private JButton crearBotonIconoInferior(String simbolo, String tooltip) {
        JButton b = new JButton(simbolo);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        b.setForeground(Color.WHITE);
        b.setToolTipText(tooltip);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void estilizarEnlaceClaro(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void estilizarEnlaceOscuro(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(TemaUI.ACCENT_OSCURO);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void confirmarSalida() {
        int seleccion = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar Mini-Windows?", "Apagar equipo",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (seleccion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
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

            if (timerReloj != null) {
                timerReloj.stop();
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
        JPanel fondo = TemaUI.crearFondoDegradado(TemaUI.LOGIN_GRAD_INICIO, TemaUI.LOGIN_GRAD_FIN);

        JPanel tarjeta = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TemaUI.SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(new Color(0, 0, 0, 25));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(30, 46, 24, 46));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;

        JLabel lblIcono = new JLabel(TemaUI.crearIconoPersonaGenerica(56));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        tarjeta.add(lblIcono, gbc);
        fila++;

        gbc.gridy = fila;
        JLabel lblTitulo = new JLabel("Crear una cuenta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        lblTitulo.setForeground(TemaUI.TEXTO);
        tarjeta.add(lblTitulo, gbc);
        fila++;

        gbc.gridy = fila;
        JLabel lblSubtitulo = new JLabel("Completa tus datos para usar Mini-Windows", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TemaUI.TEXTO_SUAVE);
        tarjeta.add(lblSubtitulo, gbc);
        fila++;

        gbc.gridwidth = 1;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Nombre completo:"), gbc);
        gbc.gridx = 1;
        txtRegNombre = new JTextField(15);
        estilizarCampo(txtRegNombre);
        tarjeta.add(txtRegNombre, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Género:"), gbc);
        gbc.gridx = 1;
        cmbRegGenero = new JComboBox<>(new String[]{"M", "F"});
        estilizarCampo(cmbRegGenero);
        tarjeta.add(cmbRegGenero, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Username:"), gbc);
        gbc.gridx = 1;
        txtRegUsername = new JTextField(15);
        estilizarCampo(txtRegUsername);
        tarjeta.add(txtRegUsername, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Password:"), gbc);
        gbc.gridx = 1;
        txtRegPassword = new JPasswordField(15);
        estilizarCampo(txtRegPassword);
        JPanel panelRegPassword = TemaUI.crearCampoPassword(txtRegPassword);
        panelRegPassword.setPreferredSize(new Dimension(260, 36));
        tarjeta.add(panelRegPassword, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 1;
        JLabel lblRequisitos = new JLabel("<html>" + TemaUI.REQUISITOS_PASSWORD + "</html>");
        lblRequisitos.setFont(lblRequisitos.getFont().deriveFont(Font.PLAIN, 10f));
        lblRequisitos.setForeground(TemaUI.TEXTO_SUAVE);
        tarjeta.add(lblRequisitos, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Tipo de cuenta:"), gbc);
        gbc.gridx = 1;
        cmbRegRol = new JComboBox<>(new String[]{"Estándar", "Administrador"});
        estilizarCampo(cmbRegRol);
        tarjeta.add(cmbRegRol, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Edad:"), gbc);
        gbc.gridx = 1;
        spnRegEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        estilizarCampo(spnRegEdad);
        tarjeta.add(spnRegEdad, gbc);
        fila++;

        gbc.gridy = fila; gbc.gridx = 0;
        tarjeta.add(crearEtiqueta("Foto de perfil:"), gbc);
        gbc.gridx = 1;
        JPanel panelFoto = new JPanel(new BorderLayout(5, 0));
        panelFoto.setOpaque(false);
        lblRegFoto = new JLabel("(ninguna seleccionada)");
        lblRegFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRegFoto.setForeground(TemaUI.TEXTO_SUAVE);
        JButton btnElegirFoto = new JButton("Elegir...");
        btnElegirFoto.addActionListener(e -> elegirFoto());
        panelFoto.add(lblRegFoto, BorderLayout.CENTER);
        panelFoto.add(btnElegirFoto, BorderLayout.EAST);
        tarjeta.add(panelFoto, gbc);
        fila++;

        lblEstadoRegistro = new JLabel(" ", SwingConstants.CENTER);
        lblEstadoRegistro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadoRegistro.setForeground(new Color(197, 15, 31));
        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 6, 0, 6);
        tarjeta.add(lblEstadoRegistro, gbc);
        fila++;
        gbc.insets = new Insets(6, 6, 6, 6);

        JButton btnRegistrar = TemaUI.crearBotonPrimario("Registrar");
        btnRegistrar.addActionListener(this::onRegistrar);
        gbc.gridy = fila; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 6, 6, 6);
        tarjeta.add(btnRegistrar, gbc);
        fila++;

        JButton btnVolver = new JButton("Volver al inicio de sesión");
        estilizarEnlaceOscuro(btnVolver);
        btnVolver.addActionListener(e -> {
            limpiarFormularioRegistro();
            cardLayout.show(panelContenedor, "LOGIN");
        });
        gbc.gridy = fila;
        gbc.insets = new Insets(2, 6, 6, 6);
        tarjeta.add(btnVolver, gbc);

        GridBagConstraints gbcFondo = new GridBagConstraints();
        fondo.add(tarjeta, gbcFondo);

        return fondo;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TemaUI.TEXTO);
        return l;
    }

    private void estilizarCampo(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUI.BORDE, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        c.setPreferredSize(new Dimension(260, 36));
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
        lblEstadoRegistro.setText(" ");

        String nombre = txtRegNombre.getText().trim();
        char genero = ((String) cmbRegGenero.getSelectedItem()).charAt(0);
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword());
        int edad = (Integer) spnRegEdad.getValue();

        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mostrarErrorRegistro("Completa todos los campos obligatorios.");
            return;
        }

        if (!TemaUI.PATRON_PASSWORD_SEGURA.matcher(password).matches()) {
            mostrarErrorRegistro("La contraseña no cumple los requisitos mínimos: " + TemaUI.REQUISITOS_PASSWORD);
            return;
        }

        String rolSeleccionado = "Administrador".equals(cmbRegRol.getSelectedItem())
                ? "ADMINISTRADOR" : "ESTANDAR";

        Usuario nuevo = new Usuario(nombre, genero, username, password, edad,
                rutaFotoSeleccionada, false, rolSeleccionado);

        try {
            GestorArchivosBinarios.registrarUsuario(nuevo);


            if (timerReloj != null) {
                timerReloj.stop();
            }
            SwingUtilities.invokeLater(() -> new EscritorioPrincipal(nuevo).setVisible(true));
            dispose();

        } catch (UsernameDuplicadoException ex) {
            mostrarErrorRegistro(ex.getMessage());
        } catch (ArchivoCorruptoException | IOException ex) {
            mostrarErrorRegistro("No se pudo registrar la cuenta: " + ex.getMessage());
        }
    }

    private void mostrarErrorRegistro(String mensaje) {
        lblEstadoRegistro.setText("<html><div style='text-align:center; width:250px;'>" + mensaje + "</div></html>");
    }

    private void limpiarFormularioRegistro() {
        txtRegNombre.setText("");
        txtRegUsername.setText("");
        txtRegPassword.setText("");
        spnRegEdad.setValue(18);
        cmbRegGenero.setSelectedIndex(0);
        lblRegFoto.setText("(ninguna seleccionada)");
        rutaFotoSeleccionada = null;
        cmbRegRol.setSelectedIndex(0);
        lblEstadoRegistro.setText(" ");
    }
}