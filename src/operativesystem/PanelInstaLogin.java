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

        JLabel lblIcono = new JLabel(cargarLogo(96));
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

    /**
     * Carga el logo de INSTA+ desde el paquete "images" (images/insta.png) y lo escala
     * sin deformarlo. Si no se encuentra, muestra el círculo "IG" de siempre.
     */
    private static Icon cargarLogo(int tamanoMaximo) {
        Image imagen = null;

        // 1) Dentro del classpath (Source Packages / images / insta.png)
        java.net.URL url = PanelInstaLogin.class.getResource("/images/insta.png");
        if (url != null) {
            imagen = new ImageIcon(url).getImage();
        } else {
            // 2) Respaldo: buscar el archivo en la carpeta del proyecto
            String[] rutas = {"images/insta.png", "src/images/insta.png"};
            for (String ruta : rutas) {
                java.io.File archivo = new java.io.File(ruta);
                if (archivo.isFile()) {
                    imagen = new ImageIcon(archivo.getAbsolutePath()).getImage();
                    break;
                }
            }
        }

        if (imagen == null) {
            System.err.println("[INSTA+] No se encontró images/insta.png, se usa el ícono por defecto.");
            return TemaUI.crearIconoCircular("IG", TemaUI.colorApp(1), 64);
        }

        ImageIcon original = new ImageIcon(imagen);
        int ancho = original.getIconWidth();
        int alto = original.getIconHeight();
        if (ancho <= 0 || alto <= 0) {
            return TemaUI.crearIconoCircular("IG", TemaUI.colorApp(1), 64);
        }
        double escala = Math.min((double) tamanoMaximo / ancho, (double) tamanoMaximo / alto);
        int nuevoAncho = Math.max(1, (int) Math.round(ancho * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * escala));
        return new ImageIcon(imagen.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH));
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
            entrarConCuentaDesactivada(username, password, ex.getMessage());
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

    /**
     * La cuenta existe pero está desactivada: si la contraseña es correcta, se deja entrar
     * únicamente para reactivarla (PantallaInstaPlus bloquea todo lo demás).
     */
    private void entrarConCuentaDesactivada(String username, String password, String mensajeExcepcion) {
        try {
            UsuarioInsta usuario = GestorInstaPlus.verificarCredenciales(username, password);
            if (usuario == null) {
                preguntarQueHacerTrasError(mensajeExcepcion);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    mensajeExcepcion + "\nSolo podrás entrar para reactivarla desde \"Editar perfil\".",
                    "Cuenta desactivada", JOptionPane.INFORMATION_MESSAGE);
            GestorInstaPlus.crearArchivosPersonales(usuario.getUsername());
            txtPassword.setText("");
            controlador.ingresarAlApp(usuario);
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer la base de usuarios de INSTA+: " + ex.getMessage(),
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