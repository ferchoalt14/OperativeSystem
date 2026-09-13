package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Contenedor raíz del módulo INSTA+.
 *
 * Esta clase ya NO contiene toda la lógica de la app: solo arma la navegación
 * (LOGIN / REGISTRO / APP, y dentro de APP: FEED / BUSCAR / CREAR / MENSAJES /
 * PERFIL / PERFIL_AJENO) y actúa como InstaControlador para que cada panel
 * pueda pedirle acciones sin conocer a los demás paneles.
 *
 * Los datos persisten en disco (ver GestorInstaPlus y GestorPosts), así que
 * las cuentas y las publicaciones se mantienen entre sesiones y nunca se borran solas.
 */
public class PantallaInstaPlus extends JPanel implements InstaControlador {

    private final CardLayout cardLayoutRaiz = new CardLayout();
    private final JPanel panelRaiz = new JPanel(cardLayoutRaiz);

    private final CardLayout cardLayoutApp = new CardLayout();
    private final JPanel panelSecciones = new JPanel(cardLayoutApp);

    private UsuarioInsta usuarioActual;

    private PanelInstaLogin panelLogin;
    private PanelInstaRegistro panelRegistro;
    private PanelInstaFeed panelFeed;
    private PanelInstaBuscar panelBuscar;
    private PanelInstaCrearPost panelCrearPost;
    private PanelInstaPerfil panelPerfil;
    private PanelInstaPerfilAjeno panelPerfilAjeno;

    public PantallaInstaPlus() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(TemaUI.FONDO);

        panelRaiz.setOpaque(false);
        panelLogin = new PanelInstaLogin(this);
        panelRegistro = new PanelInstaRegistro(this);
        panelRaiz.add(panelLogin, "LOGIN");
        panelRaiz.add(panelRegistro, "REGISTRO");
        panelRaiz.add(construirPanelApp(), "APP");

        add(panelRaiz, BorderLayout.CENTER);
        cardLayoutRaiz.show(panelRaiz, "LOGIN");
    }

    private JPanel construirPanelApp() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(construirBarraLateral(), BorderLayout.WEST);

        panelFeed = new PanelInstaFeed(this);
        panelBuscar = new PanelInstaBuscar(this);
        panelCrearPost = new PanelInstaCrearPost(this);
        panelPerfil = new PanelInstaPerfil(this);
        panelPerfilAjeno = new PanelInstaPerfilAjeno(this);

        panelSecciones.setOpaque(false);
        panelSecciones.add(panelFeed, "FEED");
        panelSecciones.add(panelBuscar, "BUSCAR");
        panelSecciones.add(panelCrearPost, "CREAR");
        panelSecciones.add(construirPanelMensajePlaceholder("✉",
                "Tu bandeja de entrada está vacía.<br>El Inbox llegará próximamente."), "MENSAJES");
        panelSecciones.add(panelPerfil, "PERFIL");
        panelSecciones.add(panelPerfilAjeno, "PERFIL_AJENO");

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

        JButton btnInicio = crearBotonNav("🏠", "Inicio", e -> { refrescarFeed(); mostrarSeccion("FEED"); });
        JButton btnBuscar = crearBotonNav("🔍", "Buscar", e -> mostrarSeccion("BUSCAR"));
        JButton btnCrear = crearBotonNav("➕", "Crear", e -> mostrarSeccion("CREAR"));
        JButton btnMensajes = crearBotonNav("✉", "Mensajes", e -> mostrarSeccion("MENSAJES"));
        JButton btnPerfil = crearBotonNav("👤", "Perfil", e -> { refrescarPerfilPropio(); mostrarSeccion("PERFIL"); });

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
        btnSalir.addActionListener(e -> cerrarSesion());
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

    // ------------------- Implementación de InstaControlador -------------------

    @Override
    public void mostrarPantallaRaiz(String nombre) {
        cardLayoutRaiz.show(panelRaiz, nombre);
    }

    @Override
    public void mostrarSeccion(String nombre) {
        cardLayoutApp.show(panelSecciones, nombre);
    }

    @Override
    public void ingresarAlApp(UsuarioInsta usuario) {
        this.usuarioActual = usuario;
        refrescarPerfilPropio();
        refrescarFeed();
        cardLayoutRaiz.show(panelRaiz, "APP");
        mostrarSeccion("FEED");
    }

    @Override
    public UsuarioInsta getUsuarioActual() {
        return usuarioActual;
    }

    @Override
    public void refrescarPerfilPropio() {
        panelPerfil.refrescar();
    }

    @Override
    public void refrescarFeed() {
        panelFeed.refrescar();
    }

    @Override
    public void refrescarBusqueda() {
        panelBuscar.ejecutarBusqueda();
    }

    @Override
    public void abrirPerfilAjeno(String username, String seccionOrigen) {
        panelPerfilAjeno.abrir(username, seccionOrigen);
    }

    @Override
    public void cerrarSesion() {
        usuarioActual = null;
        panelLogin.limpiarUsername();
        panelLogin.limpiarPassword();
        cardLayoutRaiz.show(panelRaiz, "LOGIN");
    }
}