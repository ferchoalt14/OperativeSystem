package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PantallaInstaPlus extends JPanel {

    // ---------- Navegación raíz: LOGIN / REGISTRO / APP ----------
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    // Login
    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    // Registro
    private JTextField txtRegNombre;
    private JComboBox<String> cmbRegGenero;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JSpinner spnRegEdad;

    // ---------- App principal (una vez logueado) ----------
    private final CardLayout cardLayoutApp = new CardLayout();
    private final JPanel panelSecciones = new JPanel(cardLayoutApp);
    private UsuarioInsta usuarioInstaActual;
    private String usernamePerfilVisitado;
    private String seccionAnteriorAPerfilAjeno = "PERFIL";

    // Perfil propio
    private JLabel lblPerfilAvatar;
    private JLabel lblPerfilNombreCompleto;
    private JLabel lblPerfilUsername;
    private JLabel lblPerfilPosts, lblPerfilFollowers, lblPerfilFollowing;
    private JLabel lblPerfilDatos;
    private JPanel panelSiguiendoLista;

    // Buscar
    private JTextField txtBuscarUsuario;
    private JPanel panelResultadosBusqueda;

    // Perfil ajeno
    private JLabel lblAjenoAvatar, lblAjenoNombreCompleto, lblAjenoUsername;
    private JLabel lblAjenoPosts, lblAjenoFollowers, lblAjenoFollowing;
    private JButton btnAjenoSeguir;

    public PantallaInstaPlus() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(TemaUI.FONDO);

        panelContenedor.setOpaque(false);
        panelContenedor.add(construirPanelLogin(), "LOGIN");
        panelContenedor.add(construirPanelRegistro(), "REGISTRO");
        panelContenedor.add(construirPanelApp(), "APP");

        add(panelContenedor, BorderLayout.CENTER);
        cardLayout.show(panelContenedor, "LOGIN");
    }


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
            UsuarioInsta usuario = GestorInstaPlus.autenticar(username, password);

            if (usuario == null) {
                preguntarQueHacerTrasError("Usuario o contraseña incorrectos.");
                return;
            }

            GestorInstaPlus.crearArchivosPersonales(usuario.getUsername());
            txtLoginPassword.setText("");
            ingresarAlApp(usuario);

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
                "<html><i>Al registrarte empezarás a seguir automáticamente algunas<br>"
                + "cuentas destacadas de INSTA+. Podrás elegir tu foto de perfil<br>"
                + "una vez dentro.</i></html>");
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
            limpiarFormularioRegistro();
            ingresarAlApp(nuevo); // auto-login: entra directo a la app, sin volver al Log In

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


    private void ingresarAlApp(UsuarioInsta usuario) {
        this.usuarioInstaActual = usuario;
        refrescarPerfilPropio();
        cardLayout.show(panelContenedor, "APP");
        mostrarSeccion("FEED");
    }

 

    private JPanel construirPanelApp() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(construirBarraLateral(), BorderLayout.WEST);

        panelSecciones.setOpaque(false);
        panelSecciones.add(construirSeccionFeed(), "FEED");
        panelSecciones.add(construirSeccionBuscar(), "BUSCAR");
        panelSecciones.add(construirSeccionCrear(), "CREAR");
        panelSecciones.add(construirSeccionMensajes(), "MENSAJES");
        panelSecciones.add(construirSeccionPerfil(), "PERFIL");
        panelSecciones.add(construirSeccionPerfilAjeno(), "PERFIL_AJENO");

        panel.add(panelSecciones, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirBarraLateral() {
        JPanel barra = new JPanel();
        barra.setOpaque(true);
        barra.setBackground(TemaUI.SUPERFICIE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUI.BORDE));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setPreferredSize(new Dimension(160, 0));

        JLabel lblLogo = new JLabel("INSTA+");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblLogo.setForeground(TemaUI.ACCENT_OSCURO);
        lblLogo.setBorder(new EmptyBorder(22, 18, 26, 18));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnInicio = crearBotonNav("🏠", "Inicio", e -> mostrarSeccion("FEED"));
        JButton btnBuscar = crearBotonNav("🔍", "Buscar", e -> mostrarSeccion("BUSCAR"));
        JButton btnCrear = crearBotonNav("➕", "Crear", e -> mostrarSeccion("CREAR"));
        JButton btnMensajes = crearBotonNav("✉", "Mensajes", e -> mostrarSeccion("MENSAJES"));
        JButton btnPerfil = crearBotonNav("👤", "Perfil", e -> {
            refrescarPerfilPropio();
            mostrarSeccion("PERFIL");
        });

        barra.add(lblLogo);
        barra.add(btnInicio);
        barra.add(btnBuscar);
        barra.add(btnCrear);
        barra.add(btnMensajes);
        barra.add(btnPerfil);
        barra.add(Box.createVerticalGlue());

        JButton btnSalir = new JButton("🚪  Cerrar sesión");
        btnSalir.setHorizontalAlignment(SwingConstants.LEFT);
        btnSalir.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnSalir.setForeground(new Color(190, 40, 40));
        btnSalir.setContentAreaFilled(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setOpaque(false);
        btnSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSalir.setBorder(new EmptyBorder(10, 18, 18, 18));
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> {
            usuarioInstaActual = null;
            txtLoginUsername.setText("");
            cardLayout.show(panelContenedor, "LOGIN");
        });
        barra.add(btnSalir);

        return barra;
    }

    private JButton crearBotonNav(String icono, String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(icono + "   " + texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setForeground(TemaUI.TEXTO);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setBorder(new EmptyBorder(9, 18, 9, 18));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addActionListener(accion);
        return boton;
    }

    private void mostrarSeccion(String nombre) {
        cardLayoutApp.show(panelSecciones, nombre);
    }

    private JPanel construirPanelMensajePlaceholder(String icono, String mensaje) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("<html><center>" + icono + "<br><br>" + mensaje + "</center></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(TemaUI.TEXTO_SUAVE);
        panel.add(lbl);
        return panel;
    }

    private JPanel construirSeccionFeed() {
        return construirPanelMensajePlaceholder("📷",
                "Aún no hay publicaciones en tu feed.<br>Empieza a explorar cuentas y comparte tu primer insta.");
    }

    private JPanel construirSeccionCrear() {
        return construirPanelMensajePlaceholder("➕",
                "Publicar una nueva foto estará disponible muy pronto.");
    }

    private JPanel construirSeccionMensajes() {
        return construirPanelMensajePlaceholder("✉",
                "Tu bandeja de entrada está vacía.<br>El Inbox llegará próximamente.");
    }


    private JPanel construirSeccionBuscar() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblTitulo = new JLabel("Buscar personas");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtBuscarUsuario = TemaUI.crearCampoTexto("Buscar por username...", 18);
        txtBuscarUsuario.addActionListener(e -> ejecutarBusqueda());

        JButton btnBuscar = TemaUI.crearBotonPrimario("Buscar");
        btnBuscar.addActionListener(e -> ejecutarBusqueda());

        JPanel panelCampo = new JPanel(new BorderLayout(8, 0));
        panelCampo.setOpaque(false);
        panelCampo.add(txtBuscarUsuario, BorderLayout.CENTER);
        panelCampo.add(btnBuscar, BorderLayout.EAST);
        panelCampo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.add(lblTitulo);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(panelCampo);

        panelResultadosBusqueda = new JPanel();
        panelResultadosBusqueda.setOpaque(false);
        panelResultadosBusqueda.setLayout(new BoxLayout(panelResultadosBusqueda, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelResultadosBusqueda);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(panelEncabezado, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void ejecutarBusqueda() {
        String texto = txtBuscarUsuario.getText().trim().toLowerCase();
        panelResultadosBusqueda.removeAll();

        if (!texto.isEmpty()) {
            // Se usa la lista enlazada propia del proyecto para armar los resultados.
            ListaEnlazada<String> coincidencias = new ListaEnlazada<>(null, null, 0);
            try {
                List<UsuarioInsta> usuarios = GestorInstaPlus.cargarUsuarios();
                for (UsuarioInsta u : usuarios) {
                    if (u.isActiva()
                            && !u.getUsername().equalsIgnoreCase(usuarioInstaActual.getUsername())
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
                panelResultadosBusqueda.add(lblVacio);
            } else {
                List<String> misSeguidos;
                try {
                    misSeguidos = GestorInstaPlus.obtenerFollowing(usuarioInstaActual.getUsername());
                } catch (ArchivoCorruptoException ex) {
                    misSeguidos = new ArrayList<>();
                }
                for (String username : coincidencias) {
                    boolean loSigo = misSeguidos.contains(username);
                    panelResultadosBusqueda.add(crearFilaResultado(username, loSigo));
                    panelResultadosBusqueda.add(Box.createVerticalStrut(6));
                }
            }
        }

        panelResultadosBusqueda.revalidate();
        panelResultadosBusqueda.repaint();
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
        btnUsername.addActionListener(e -> abrirPerfilAjeno(username, "BUSCAR"));
        fila.add(btnUsername, BorderLayout.CENTER);

        return fila;
    }

    // ---------------------------------------------------------------
    //  Perfil propio
    // ---------------------------------------------------------------

    private JPanel construirSeccionPerfil() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(26, 28, 20, 28));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));

        lblPerfilAvatar = new JLabel();
        lblPerfilAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPerfilNombreCompleto = new JLabel("", SwingConstants.CENTER);
        lblPerfilNombreCompleto.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblPerfilNombreCompleto.setForeground(TemaUI.TEXTO);
        lblPerfilNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPerfilUsername = new JLabel("", SwingConstants.CENTER);
        lblPerfilUsername.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPerfilUsername.setForeground(TemaUI.TEXTO_SUAVE);
        lblPerfilUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPerfilPosts = crearEtiquetaStat();
        lblPerfilFollowers = crearEtiquetaStat();
        lblPerfilFollowing = crearEtiquetaStat();

        JPanel panelStats = new JPanel(new GridLayout(1, 3, 20, 0));
        panelStats.setOpaque(false);
        panelStats.add(envolverStat(lblPerfilPosts, "Publicaciones"));
        panelStats.add(envolverStat(lblPerfilFollowers, "Seguidores"));
        panelStats.add(envolverStat(lblPerfilFollowing, "Siguiendo"));
        panelStats.setMaximumSize(new Dimension(340, 60));
        panelStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPerfilDatos = new JLabel("", SwingConstants.CENTER);
        lblPerfilDatos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPerfilDatos.setForeground(TemaUI.TEXTO_SUAVE);
        lblPerfilDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnElegirFoto = new JButton("Elegir foto de perfil");
        btnElegirFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnElegirFoto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnElegirFoto.addActionListener(e -> elegirFotoPerfil());

        panelEncabezado.add(lblPerfilAvatar);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblPerfilNombreCompleto);
        panelEncabezado.add(lblPerfilUsername);
        panelEncabezado.add(Box.createVerticalStrut(14));
        panelEncabezado.add(panelStats);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblPerfilDatos);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(btnElegirFoto);
        panelEncabezado.add(Box.createVerticalStrut(20));

        JLabel lblSiguiendoTitulo = new JLabel("Cuentas que sigues");
        lblSiguiendoTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSiguiendoTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        lblSiguiendoTitulo.setBorder(new EmptyBorder(0, 2, 8, 0));

        panelSiguiendoLista = new JPanel();
        panelSiguiendoLista.setOpaque(false);
        panelSiguiendoLista.setLayout(new BoxLayout(panelSiguiendoLista, BoxLayout.Y_AXIS));

        JPanel panelSiguiendoContenedor = new JPanel(new BorderLayout());
        panelSiguiendoContenedor.setOpaque(false);
        panelSiguiendoContenedor.add(lblSiguiendoTitulo, BorderLayout.NORTH);
        panelSiguiendoContenedor.add(panelSiguiendoLista, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(panelSiguiendoContenedor);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(panelEncabezado, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JLabel crearEtiquetaStat() {
        JLabel lbl = new JLabel("0", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        lbl.setForeground(TemaUI.TEXTO);
        return lbl;
    }

    private JPanel envolverStat(JLabel valor, String etiqueta) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lblEtiqueta = new JLabel(etiqueta, SwingConstants.CENTER);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEtiqueta.setForeground(TemaUI.TEXTO_SUAVE);
        valor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(valor);
        panel.add(lblEtiqueta);
        return panel;
    }

    private void refrescarPerfilPropio() {
        if (usuarioInstaActual == null) {
            return;
        }

        lblPerfilNombreCompleto.setText(usuarioInstaActual.getNombreCompleto());
        lblPerfilUsername.setText("@" + usuarioInstaActual.getUsername());

        String ruta = usuarioInstaActual.getRutaFotoPerfil();
        if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
            lblPerfilAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
        } else {
            String nombre = usuarioInstaActual.getNombreCompleto();
            String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
            lblPerfilAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(1), 96));
        }

        List<String> following;
        List<String> followers;
        int publicaciones;
        try {
            following = GestorInstaPlus.obtenerFollowing(usuarioInstaActual.getUsername());
            followers = GestorInstaPlus.obtenerFollowers(usuarioInstaActual.getUsername());
        } catch (ArchivoCorruptoException ex) {
            following = new ArrayList<>();
            followers = new ArrayList<>();
        }
        publicaciones = GestorInstaPlus.contarPublicaciones(usuarioInstaActual.getUsername());

        lblPerfilPosts.setText(String.valueOf(publicaciones));
        lblPerfilFollowers.setText(String.valueOf(followers.size()));
        lblPerfilFollowing.setText(String.valueOf(following.size()));

        lblPerfilDatos.setText("<html>" + usuarioInstaActual.getEdad() + " años &nbsp;•&nbsp; "
                + (usuarioInstaActual.getGenero() == 'F' ? "Femenino" : "Masculino")
                + " &nbsp;•&nbsp; Se unió el " + usuarioInstaActual.getFechaRegistroTexto() + "</html>");

        // La lista de "siguiendo" se recorre con la lista enlazada propia del proyecto.
        ListaEnlazada<String> lista = new ListaEnlazada<>(null, null, 0);
        for (String u : following) {
            lista.agregar(u);
        }

        panelSiguiendoLista.removeAll();
        if (lista.estaVacia()) {
            JLabel lblVacio = new JLabel("Aún no sigues a nadie.");
            lblVacio.setForeground(TemaUI.TEXTO_SUAVE);
            panelSiguiendoLista.add(lblVacio);
        } else {
            for (String u : lista) {
                panelSiguiendoLista.add(crearFilaSiguiendo(u));
                panelSiguiendoLista.add(Box.createVerticalStrut(6));
            }
        }
        panelSiguiendoLista.revalidate();
        panelSiguiendoLista.repaint();
    }

    private JPanel crearFilaSiguiendo(String username) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lblAvatar = new JLabel(TemaUI.crearIconoCircular(
                username.substring(0, 1).toUpperCase(), TemaUI.colorApp(username.hashCode()), 32));
        fila.add(lblAvatar, BorderLayout.WEST);

        JButton btnUsername = new JButton("@" + username);
        btnUsername.setContentAreaFilled(false);
        btnUsername.setBorderPainted(false);
        btnUsername.setFocusPainted(false);
        btnUsername.setHorizontalAlignment(SwingConstants.LEFT);
        btnUsername.setForeground(TemaUI.TEXTO);
        btnUsername.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUsername.addActionListener(e -> abrirPerfilAjeno(username, "PERFIL"));
        fila.add(btnUsername, BorderLayout.CENTER);

        return fila;
    }

    private void elegirFotoPerfil() {
        if (usuarioInstaActual == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            usuarioInstaActual.setRutaFotoPerfil(archivo.getAbsolutePath());
            try {
                GestorInstaPlus.actualizarUsuario(usuarioInstaActual);
                refrescarPerfilPropio();
            } catch (ArchivoCorruptoException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo guardar la foto de perfil: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    

    private JPanel construirSeccionPerfilAjeno() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 26, 24, 26));

        JButton btnVolver = new JButton("←  Volver");
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setForeground(TemaUI.ACCENT_OSCURO);
        btnVolver.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> mostrarSeccion(seccionAnteriorAPerfilAjeno));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.setBorder(new EmptyBorder(16, 0, 0, 0));

        lblAjenoAvatar = new JLabel();
        lblAjenoAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAjenoNombreCompleto = new JLabel("", SwingConstants.CENTER);
        lblAjenoNombreCompleto.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAjenoNombreCompleto.setForeground(TemaUI.TEXTO);
        lblAjenoNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAjenoUsername = new JLabel("", SwingConstants.CENTER);
        lblAjenoUsername.setForeground(TemaUI.TEXTO_SUAVE);
        lblAjenoUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAjenoPosts = crearEtiquetaStat();
        lblAjenoFollowers = crearEtiquetaStat();
        lblAjenoFollowing = crearEtiquetaStat();

        JPanel panelStats = new JPanel(new GridLayout(1, 3, 20, 0));
        panelStats.setOpaque(false);
        panelStats.add(envolverStat(lblAjenoPosts, "Publicaciones"));
        panelStats.add(envolverStat(lblAjenoFollowers, "Seguidores"));
        panelStats.add(envolverStat(lblAjenoFollowing, "Siguiendo"));
        panelStats.setMaximumSize(new Dimension(340, 60));
        panelStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAjenoSeguir = TemaUI.crearBotonPrimario("Seguir");
        btnAjenoSeguir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAjenoSeguir.addActionListener(e -> alternarSeguimientoAjeno());

        JLabel lblSinPosts = new JLabel("<html><center>📷<br><br>Aún no hay publicaciones.</center></html>",
                SwingConstants.CENTER);
        lblSinPosts.setForeground(TemaUI.TEXTO_SUAVE);
        lblSinPosts.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSinPosts.setBorder(new EmptyBorder(30, 0, 0, 0));

        panelEncabezado.add(lblAjenoAvatar);
        panelEncabezado.add(Box.createVerticalStrut(10));
        panelEncabezado.add(lblAjenoNombreCompleto);
        panelEncabezado.add(lblAjenoUsername);
        panelEncabezado.add(Box.createVerticalStrut(14));
        panelEncabezado.add(panelStats);
        panelEncabezado.add(Box.createVerticalStrut(16));
        panelEncabezado.add(btnAjenoSeguir);
        panelEncabezado.add(lblSinPosts);

        panel.add(btnVolver, BorderLayout.NORTH);
        panel.add(panelEncabezado, BorderLayout.CENTER);
        return panel;
    }

    private void abrirPerfilAjeno(String username, String seccionOrigen) {
        this.usernamePerfilVisitado = username;
        this.seccionAnteriorAPerfilAjeno = seccionOrigen;

        try {
            UsuarioInsta perfil = GestorInstaPlus.buscarPorUsername(username);
            if (perfil == null) {
                JOptionPane.showMessageDialog(this, "No se encontró la cuenta @" + username,
                        "Cuenta no disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            lblAjenoNombreCompleto.setText(perfil.getNombreCompleto());
            lblAjenoUsername.setText("@" + perfil.getUsername());

            String ruta = perfil.getRutaFotoPerfil();
            if (ruta != null && !ruta.isBlank() && new File(ruta).exists()) {
                lblAjenoAvatar.setIcon(TemaUI.crearIconoCircularDeImagen(new File(ruta), 96));
            } else {
                String nombre = perfil.getNombreCompleto();
                String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
                lblAjenoAvatar.setIcon(TemaUI.crearIconoCircular(iniciales, TemaUI.colorApp(username.hashCode()), 96));
            }

            List<String> followers = GestorInstaPlus.obtenerFollowers(username);
            List<String> following = GestorInstaPlus.obtenerFollowing(username);
            int posts = GestorInstaPlus.contarPublicaciones(username);

            lblAjenoPosts.setText(String.valueOf(posts));
            lblAjenoFollowers.setText(String.valueOf(followers.size()));
            lblAjenoFollowing.setText(String.valueOf(following.size()));

            boolean loSigo = followers.contains(usuarioInstaActual.getUsername());
            btnAjenoSeguir.setText(loSigo ? "Dejar de seguir" : "Seguir");

            mostrarSeccion("PERFIL_AJENO");

        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el perfil de @" + username,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarSeguimientoAjeno() {
        if (usernamePerfilVisitado == null || usuarioInstaActual == null) {
            return;
        }
        try {
            List<String> followers = GestorInstaPlus.obtenerFollowers(usernamePerfilVisitado);
            boolean loSigo = followers.contains(usuarioInstaActual.getUsername());

            if (loSigo) {
                GestorInstaPlus.dejarDeSeguir(usuarioInstaActual.getUsername(), usernamePerfilVisitado);
            } else {
                GestorInstaPlus.seguirCuenta(usuarioInstaActual.getUsername(), usernamePerfilVisitado);
            }

            abrirPerfilAjeno(usernamePerfilVisitado, seccionAnteriorAPerfilAjeno);
            refrescarPerfilPropio();
            if ("BUSCAR".equals(seccionAnteriorAPerfilAjeno)) {
                ejecutarBusqueda();
            }

        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el seguimiento: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}