package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PantallaInstaPlus extends JPanel implements InstaControlador {

    private final CardLayout cardLayoutRaiz = new CardLayout();
    private final JPanel panelRaiz = new JPanel(cardLayoutRaiz);

    private final CardLayout cardLayoutApp = new CardLayout();
    private final JPanel panelSecciones = new JPanel(cardLayoutApp);

    private UsuarioInsta usuarioActual;

    private PanelInstaLogin panelLogin;
    private PanelInstaRegistro panelRegistro;
    private PanelInstaSeleccionSugeridos panelSugeridos;
    private PanelInstaFeed panelFeed;
    private PanelInstaBuscar panelBuscar;
    private PanelInstaCrearPost panelCrearPost;
    private PanelInstaPerfil panelPerfil;
    private PanelInstaPerfilAjeno panelPerfilAjeno;
    private PanelInstaMensajes panelMensajes;
    private PanelInstaChat panelChat;
    private JButton btnMensajesNav;

    private final Map<String, Integer> noLeidosPorConversacion = new HashMap<>();
    private Timer timerNotificaciones;

    public PantallaInstaPlus() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(TemaUI.FONDO);

        panelRaiz.setOpaque(false);
        panelLogin = new PanelInstaLogin(this);
        panelRegistro = new PanelInstaRegistro(this);
        panelSugeridos = new PanelInstaSeleccionSugeridos(this);
        panelRaiz.add(panelLogin, "LOGIN");
        panelRaiz.add(panelRegistro, "REGISTRO");
        panelRaiz.add(panelSugeridos, "SUGERIDOS");
        panelRaiz.add(construirPanelApp(), "APP");

        add(panelRaiz, BorderLayout.CENTER);

        UsuarioInsta sesion = GestorInstaPlus.obtenerSesion();
        if (sesion != null) {
            this.usuarioActual = sesion;
            refrescarPerfilPropio();
            refrescarFeed();
            refrescarMensajes();
            cardLayoutRaiz.show(panelRaiz, "APP");
            mostrarSeccion("FEED");
            iniciarNotificaciones();
        } else {
            cardLayoutRaiz.show(panelRaiz, "LOGIN");
        }
    }
    private PanelInstaEditarPerfil panelEditarPerfil;

    private JPanel construirPanelApp() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(construirBarraLateral(), BorderLayout.WEST);

        panelFeed = new PanelInstaFeed(this);
        panelBuscar = new PanelInstaBuscar(this);
        panelCrearPost = new PanelInstaCrearPost(this);
        panelPerfil = new PanelInstaPerfil(this);
        panelEditarPerfil = new PanelInstaEditarPerfil(this);
        panelPerfilAjeno = new PanelInstaPerfilAjeno(this);
        panelMensajes = new PanelInstaMensajes(this);
        panelChat = new PanelInstaChat(this);

        panelSecciones.setOpaque(false);
        panelSecciones.add(panelFeed, "FEED");
        panelSecciones.add(panelBuscar, "BUSCAR");
        panelSecciones.add(panelCrearPost, "CREAR");
        panelSecciones.add(panelMensajes, "MENSAJES");
        panelSecciones.add(panelChat, "CHAT");
        panelSecciones.add(panelPerfil, "PERFIL");
        panelSecciones.add(panelPerfilAjeno, "PERFIL_AJENO");
        panelSecciones.add(panelEditarPerfil, "EDITAR_PERFIL");
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

        JButton btnInicio = crearBotonNav("🏠", "Inicio", e -> {
            refrescarFeed();
            mostrarSeccion("FEED");
        });
        JButton btnBuscar = crearBotonNav("🔍", "Buscar", e -> mostrarSeccion("BUSCAR"));
        JButton btnCrear = crearBotonNav("➕", "Crear", e -> mostrarSeccion("CREAR"));
        btnMensajesNav = crearBotonNav("✉", "Mensajes", e -> {
            refrescarMensajes();
            mostrarSeccion("MENSAJES");
        });
        JButton btnPerfil = crearBotonNav("👤", "Perfil", e -> {
            refrescarPerfilPropio();
            mostrarSeccion("PERFIL");
        });
        JButton btnEditarPerfil = crearBotonNav("✏", "Editar perfil", e -> {
            panelEditarPerfil.cargarDatos();
            mostrarSeccion("EDITAR_PERFIL");
        });
        barra.add(btnEditarPerfil);

        barra.add(lblLogo);
        barra.add(btnInicio);
        barra.add(btnBuscar);
        barra.add(btnCrear);
        barra.add(btnMensajesNav);
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
        GestorInstaPlus.guardarSesion(usuario);
        refrescarPerfilPropio();
        refrescarFeed();
        refrescarMensajes();
        cardLayoutRaiz.show(panelRaiz, "APP");
        mostrarSeccion("FEED");
        iniciarNotificaciones();
    }

    @Override
    public void mostrarSeleccionSugeridos(UsuarioInsta usuario) {
        panelSugeridos.mostrarPara(usuario);
        cardLayoutRaiz.show(panelRaiz, "SUGERIDOS");
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
    public void abrirChatConUsuario(String username) {
        panelChat.abrirCon(username, "MENSAJES");
        noLeidosPorConversacion.put(username, 0);
    }

    @Override
    public void refrescarMensajes() {
        if (usuarioActual == null) {
            return;
        }
        panelMensajes.refrescar();
        try {
            int total = GestorMensajes.contarNoLeidosTotal(usuarioActual.getUsername());
            btnMensajesNav.setText("✉   Mensajes" + (total > 0 ? " (" + total + ")" : ""));
        } catch (ArchivoCorruptoException ex) {
            
        }
    }

    private void iniciarNotificaciones() {
        if (timerNotificaciones != null) {
            timerNotificaciones.stop();
        }
        noLeidosPorConversacion.clear();
        timerNotificaciones = new Timer(6000, e -> revisarMensajesNuevos());
        timerNotificaciones.start();
    }

    private void revisarMensajesNuevos() {
        if (usuarioActual == null) {
            return;
        }
        try {
            ListaEnlazada<String> conversaciones = GestorMensajes.obtenerConversaciones(usuarioActual.getUsername());
            boolean huboCambios = false;
            for (String otro : conversaciones) {
                int noLeidos = GestorMensajes.contarNoLeidosDe(usuarioActual.getUsername(), otro);
                Integer anterior = noLeidosPorConversacion.getOrDefault(otro, 0);
                if (noLeidos > anterior) {
                    huboCambios = true;
                    boolean estaViendoEseChat = panelChat.isShowing() && otro.equalsIgnoreCase(panelChat.getUsuarioActivo());
                    if (!estaViendoEseChat) {
                        ToastNotificacion.mostrar("Nuevo mensaje", "@" + otro + " te escribió",
                                () -> abrirChatConUsuario(otro));
                    }
                }
                noLeidosPorConversacion.put(otro, noLeidos);
            }
            if (huboCambios) {
                refrescarMensajes();
            }
        } catch (ArchivoCorruptoException ex) {
            
        }
    }

    @Override
    public void cerrarSesion() {
        if (timerNotificaciones != null) {
            timerNotificaciones.stop();
        }
        usuarioActual = null;
        GestorInstaPlus.cerrarSesionGuardada();
        panelLogin.limpiarUsername();
        panelLogin.limpiarPassword();
        cardLayoutRaiz.show(panelRaiz, "LOGIN");
    }
}