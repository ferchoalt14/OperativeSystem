package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class PantallaInstaPlus extends JPanel implements InstaControlador {

    private final CardLayout cardLayoutRaiz = new CardLayout();
    private final JPanel panelRaiz = new JPanel(cardLayoutRaiz);

    private final CardLayout cardLayoutApp = new CardLayout();
    private final JPanel panelSecciones = new JPanel(cardLayoutApp);

    /** Usuario del sistema operativo dueño de esta ventana (la sesión de INSTA+ se guarda por él). */
    private final String usuarioSO;
    private UsuarioInsta usuarioActual;
    private String seccionActual = "FEED";

    private PanelInstaLogin panelLogin;
    private PanelInstaRegistro panelRegistro;
    private PanelInstaSeleccionSugeridos panelSugeridos;
    private PanelInstaFeed panelFeed;
    private PanelInstaBuscar panelBuscar;
    private PanelInstaCrearPost panelCrearPost;
    private PanelInstaPerfil panelPerfil;
    private PanelDescripcionPerfil panelDescripcionPerfil;
    private PanelInstaEditarPerfil panelEditarPerfil;
    private PanelInstaPerfilAjeno panelPerfilAjeno;
    private PanelInstaMensajes panelMensajes;
    private PanelInstaChat panelChat;
    private PanelInstaDetallePost panelDetallePost;

    private JButton btnInicioNav;
    private JButton btnBuscarNav;
    private JButton btnCrearNav;
    private JButton btnMensajesNav;
    private JLabel lblConexion;
    private final AvisoFlotanteInsta aviso = new AvisoFlotanteInsta();

    private ClienteInsta cliente;
    private volatile boolean hiloInboxActivo;
    private Thread hiloRevisionInbox;

    public PantallaInstaPlus(String usuarioSO) {
        super(new BorderLayout());
        this.usuarioSO = usuarioSO;
        GestorInstaPlus.establecerUsuarioSistema(usuarioSO);
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

        UsuarioInsta sesion = GestorInstaPlus.obtenerSesion(usuarioSO);
        if (sesion != null) {
            entrar(sesion);
        } else {
            cardLayoutRaiz.show(panelRaiz, "LOGIN");
        }
    }

    private JComponent construirPanelApp() {
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
        panelDetallePost = new PanelInstaDetallePost(this);

        // Perfil propio = franja con descripción + botón "Editar perfil" arriba del perfil.
        panelDescripcionPerfil = new PanelDescripcionPerfil(this);
        JPanel cabeceraPerfil = new JPanel(new BorderLayout());
        cabeceraPerfil.setOpaque(false);
        cabeceraPerfil.setBorder(new EmptyBorder(16, 26, 0, 26));
        cabeceraPerfil.add(panelDescripcionPerfil, BorderLayout.CENTER);

        JPanel seccionPerfil = new JPanel(new BorderLayout());
        seccionPerfil.setOpaque(false);
        seccionPerfil.add(cabeceraPerfil, BorderLayout.NORTH);
        seccionPerfil.add(panelPerfil, BorderLayout.CENTER);

        panelSecciones.setOpaque(false);
        panelSecciones.add(panelFeed, "FEED");
        panelSecciones.add(panelBuscar, "BUSCAR");
        panelSecciones.add(panelCrearPost, "CREAR");
        panelSecciones.add(panelMensajes, "MENSAJES");
        panelSecciones.add(panelChat, "CHAT");
        panelSecciones.add(seccionPerfil, "PERFIL");
        panelSecciones.add(panelPerfilAjeno, "PERFIL_AJENO");
        panelSecciones.add(panelEditarPerfil, "EDITAR_PERFIL");
        panelSecciones.add(panelDetallePost, "POST");

        panel.add(new AvisoFlotanteInsta.Capa(panelSecciones, aviso), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirBarraLateral() {
        JPanel barra = new JPanel();
        barra.setOpaque(true);
        barra.setBackground(TemaUI.SUPERFICIE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, TemaUI.BORDE));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setPreferredSize(new Dimension(170, 0));

        JLabel lblLogo = new JLabel("INSTA+");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblLogo.setForeground(TemaUI.ACCENT_OSCURO);
        lblLogo.setBorder(new EmptyBorder(22, 18, 24, 18));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnInicioNav = crearBotonNav("🏠", "Inicio", e -> {
            refrescarFeed();
            mostrarSeccion("FEED");
        });
        btnBuscarNav = crearBotonNav("🔍", "Buscar", e -> mostrarSeccion("BUSCAR"));
        btnCrearNav = crearBotonNav("➕", "Crear", e -> mostrarSeccion("CREAR"));
        btnMensajesNav = crearBotonNav("✉", "Mensajes", e -> {
            refrescarMensajes();
            mostrarSeccion("MENSAJES");
        });
        JButton btnPerfil = crearBotonNav("👤", "Perfil", e -> {
            refrescarPerfilPropio();
            mostrarSeccion("PERFIL");
        });

        barra.add(lblLogo);
        barra.add(btnInicioNav);
        barra.add(btnBuscarNav);
        barra.add(btnCrearNav);
        barra.add(btnMensajesNav);
        barra.add(btnPerfil);
        barra.add(Box.createVerticalGlue());

        lblConexion = new JLabel("● Conectando…");
        lblConexion.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblConexion.setForeground(EstiloInsta.AMBAR);
        lblConexion.setBorder(new EmptyBorder(0, 20, 4, 18));
        lblConexion.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.add(lblConexion);

        JButton btnSalir = new JButton("🚪  Cerrar sesión");
        btnSalir.setHorizontalAlignment(SwingConstants.LEFT);
        btnSalir.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnSalir.setForeground(EstiloInsta.ROJO);
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

    // ------------------------------------------------------------------------------------------
    //  Sesión y sockets
    // ------------------------------------------------------------------------------------------

    private void entrar(UsuarioInsta usuario) {
        this.usuarioActual = usuario;
        GestorInstaPlus.guardarSesion(usuarioSO, usuario);
        conectarCliente();
        iniciarRevisionNotificacionesInbox();
        refrescarPerfilPropio();
        refrescarFeed();
        refrescarMensajes();
        cardLayoutRaiz.show(panelRaiz, "APP");
        aplicarEstadoCuenta();
        if (cuentaActiva()) {
            mostrarSeccion("FEED");
        } else {
            panelEditarPerfil.cargarDatos();
            mostrarSeccion("EDITAR_PERFIL");
            aviso.mostrar(null, "Tu cuenta está desactivada",
                    "Solo puedes reactivarla desde Editar perfil. Mientras tanto nadie ve tu contenido.", null);
        }
    }

    // ------------------------------------------------------------------------------------------
    //  Cuenta desactivada: solo se permite ver el perfil propio, editarlo y reactivarlo.
    // ------------------------------------------------------------------------------------------

    private boolean cuentaActiva() {
        return usuarioActual != null && usuarioActual.isActiva();
    }

    /** Secciones permitidas mientras la cuenta está desactivada. */
    private static boolean seccionPermitidaSinCuentaActiva(String nombre) {
        return "PERFIL".equals(nombre) || "EDITAR_PERFIL".equals(nombre);
    }

    /** Habilita o bloquea los botones del menú según el estado de la cuenta. */
    private void aplicarEstadoCuenta() {
        boolean activa = cuentaActiva();
        btnInicioNav.setEnabled(activa);
        btnBuscarNav.setEnabled(activa);
        btnCrearNav.setEnabled(activa);
        btnMensajesNav.setEnabled(activa);
        if (!activa && !seccionPermitidaSinCuentaActiva(seccionActual)) {
            panelEditarPerfil.cargarDatos();
            mostrarSeccion("EDITAR_PERFIL");
        }
    }

    private void conectarCliente() {
        if (cliente != null) {
            cliente.desconectar();
        }
        cliente = new ClienteInsta(usuarioActual.getUsername(), new ClienteInsta.Oyente() {
            @Override
            public void alRecibirEvento(PaqueteInsta evento) {
                manejarEvento(evento);
            }

            @Override
            public void alCambiarConexion(boolean conectado) {
                if (conectado) {
                    lblConexion.setText("● En línea");
                    lblConexion.setForeground(EstiloInsta.VERDE);
                } else {
                    lblConexion.setText("● Reconectando…");
                    lblConexion.setForeground(EstiloInsta.AMBAR);
                }
            }
        });
        cliente.conectar();
    }

    /** Revisa periódicamente las notificaciones binarias sin bloquear el hilo de Swing. */
    private void iniciarRevisionNotificacionesInbox() {
        detenerRevisionNotificacionesInbox();
        hiloInboxActivo = true;
        hiloRevisionInbox = new Thread(() -> {
            int anterior = -1;
            while (hiloInboxActivo && usuarioActual != null) {
                try {
                    int actual = GestorNotificaciones.contarNoLeidas(usuarioActual.getUsername());
                    if (actual != anterior) {
                        anterior = actual;
                        int total = actual;
                        SwingUtilities.invokeLater(() -> actualizarNotificacionesInbox(total));
                    }
                    Thread.sleep(2500L);
                } catch (ArchivoCorruptoException ex) {
                    // La interfaz no se bloquea si un archivo está temporalmente ocupado/corrupto.
                    try {
                        Thread.sleep(2500L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "hilo-revision-notificaciones-inbox");
        hiloRevisionInbox.setDaemon(true);
        hiloRevisionInbox.start();
    }

    private void detenerRevisionNotificacionesInbox() {
        hiloInboxActivo = false;
        if (hiloRevisionInbox != null) {
            hiloRevisionInbox.interrupt();
            hiloRevisionInbox = null;
        }
    }

    /** Llamar cuando se cierra la ventana de INSTA+ (la sesión queda guardada para este usuario del SO). */
    public void liberarRecursos() {
        detenerRevisionNotificacionesInbox();
        if (cliente != null) {
            cliente.desconectar();
            cliente = null;
        }
        aviso.ocultar();
    }

    private boolean soyYo(String username) {
        return usuarioActual != null && username != null && usuarioActual.getUsername().equalsIgnoreCase(username);
    }

    private boolean viendoChatCon(String username) {
        return "CHAT".equals(seccionActual) && panelChat.isShowing()
                && username != null && username.equalsIgnoreCase(panelChat.getUsuarioActivo());
    }

    private boolean viendoPost(String autor, String postId) {
        return "POST".equals(seccionActual) && panelDetallePost.muestra(autor, postId);
    }

    /** Todo lo que empuja el servidor llega aquí (ya en el hilo de Swing). */
    private void manejarEvento(PaqueteInsta e) {
        if (usuarioActual == null || !usuarioActual.isActiva()) {
            return;
        }
        String de = e.getOVacio(PaqueteInsta.DE);
        String para = e.getOVacio(PaqueteInsta.PARA);
        String autor = e.getOVacio(PaqueteInsta.AUTOR_POST);
        String postId = e.getOVacio(PaqueteInsta.POST_ID);

        switch (e.getTipo()) {
            case MENSAJE_NUEVO: {
                String otro = soyYo(de) ? para : de;
                if (viendoChatCon(otro)) {
                    panelChat.refrescar();
                } else if (!soyYo(de)) {
                    aviso.mostrar(AvatarHelper.avatarPara(de, 34), "Nuevo mensaje de @" + de,
                            EstiloInsta.recortar(e.get(PaqueteInsta.TEXTO), 80), () -> abrirChatConUsuario(de));
                }
                refrescarMensajes();
                break;
            }
            case MENSAJES_LEIDOS: {
                String otro = soyYo(de) ? para : de;
                if (!soyYo(de) && viendoChatCon(otro)) {
                    panelChat.refrescar();
                }
                refrescarMensajes();
                break;
            }
            case CHAT_ELIMINADO: {
                String otro = soyYo(de) ? para : de;
                if (viendoChatCon(otro)) {
                    mostrarSeccion("MENSAJES");
                }
                if (!soyYo(de)) {
                    aviso.mostrar(AvatarHelper.avatarPara(de, 34), "Conversación eliminada",
                            "@" + de + " eliminó el chat que tenían.", null);
                }
                refrescarMensajes();
                break;
            }
            case COMENTARIO_NUEVO: {
                if (viendoPost(autor, postId)) {
                    panelDetallePost.recargar();
                }
                panelFeed.actualizarPost(autor, postId);
                if (soyYo(autor) && !soyYo(de)) {
                    aviso.mostrar(AvatarHelper.avatarPara(de, 34), "@" + de + " comentó tu publicación",
                            EstiloInsta.recortar(e.get(PaqueteInsta.TEXTO), 80), () -> abrirPostPorId(autor, postId));
                }
                break;
            }
            case LIKE_ACTUALIZADO: {
                if (viendoPost(autor, postId) && !soyYo(de)) {
                    panelDetallePost.recargar();
                }
                panelFeed.actualizarPost(autor, postId);
                if (soyYo(autor)) {
                    panelPerfil.refrescar();
                    if (!soyYo(de) && e.getBool(PaqueteInsta.LIKEADO)) {
                        aviso.mostrar(AvatarHelper.avatarPara(de, 34), "Nuevo me gusta",
                                "A @" + de + " le gustó tu publicación.", () -> abrirPostPorId(autor, postId));
                    }
                }
                break;
            }
            case POST_NUEVO: {
                refrescarFeed();
                if (soyYo(de)) {
                    refrescarPerfilPropio();
                } else {
                    aviso.mostrar(AvatarHelper.avatarPara(de, 34), "Nueva publicación",
                            "@" + de + " publicó algo nuevo.", () -> abrirPostPorId(autor, postId));
                }
                break;
            }
            case POST_ELIMINADO: {
                if (viendoPost(autor, postId)) {
                    panelDetallePost.volver();
                }
                refrescarFeed();
                if (soyYo(autor)) {
                    refrescarPerfilPropio();
                }
                break;
            }
            case MENCION: {
                aviso.mostrar(AvatarHelper.avatarPara(de, 34), "Te mencionaron",
                        "@" + de + " te mencionó en una publicación.", () -> abrirPostPorId(autor, postId));
                break;
            }
            default:
                break;
        }
    }

    private void abrirPostPorId(String autor, String postId) {
        try {
            Post post = GestorPosts.obtenerPostPorId(autor, postId);
            if (post == null) {
                mostrarAviso("Publicación no disponible", "Esa publicación ya fue eliminada.");
                return;
            }
            abrirPost(post, null);
        } catch (ArchivoCorruptoException ex) {
            mostrarAviso("No se pudo abrir la publicación", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------
    //  Implementación de InstaControlador
    // ------------------------------------------------------------------------------------------

    @Override
    public void mostrarPantallaRaiz(String nombre) {
        cardLayoutRaiz.show(panelRaiz, nombre);
    }

    @Override
    public void mostrarSeccion(String nombre) {
        if (usuarioActual != null && !usuarioActual.isActiva() && !seccionPermitidaSinCuentaActiva(nombre)) {
            // Con la cuenta desactivada no se puede navegar, publicar, comentar ni mandar mensajes.
            nombre = "EDITAR_PERFIL";
            panelEditarPerfil.cargarDatos();
            aviso.mostrar(null, "Cuenta desactivada", "Reactiva tu cuenta para usar esta opción.", null);
        }
        seccionActual = nombre;
        cardLayoutApp.show(panelSecciones, nombre);
    }

    @Override
    public void ingresarAlApp(UsuarioInsta usuario) {
        entrar(usuario);
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
        panelDescripcionPerfil.refrescar(usuarioActual);
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
        if (soyYo(username)) {
            refrescarPerfilPropio();
            mostrarSeccion("PERFIL");
            return;
        }
        panelPerfilAjeno.abrir(username, seccionOrigen);
    }

    @Override
    public void abrirChatConUsuario(String username) {
        String origen = "PERFIL_AJENO".equals(seccionActual) ? "PERFIL_AJENO" : "MENSAJES";
        panelChat.abrirCon(username, origen);
    }

    @Override
    public void refrescarMensajes() {
        if (usuarioActual == null) {
            return;
        }
        panelMensajes.refrescar();
        try {
            int total = GestorMensajes.contarNoLeidosTotal(usuarioActual.getUsername());
            btnMensajesNav.setText("✉   Mensajes" + (total > 0 ? "  (" + total + ")" : ""));
            btnMensajesNav.setFont(new Font("SansSerif", total > 0 ? Font.BOLD : Font.PLAIN, 14));
        } catch (ArchivoCorruptoException ex) {
            // el contador se actualizará con el próximo evento
        }
    }

    @Override
    public void abrirPost(Post post, Runnable alCambiar) {
        if (post == null) {
            return;
        }
        if (!soyYo(post.getUsernameAutor()) && !GestorInstaPlus.estaActiva(post.getUsernameAutor())) {
            mostrarAviso("Publicación no disponible", "Esta publicación ya no está disponible.");
            return;
        }
        String origen = seccionActual;
        mostrarSeccion("POST");
        panelDetallePost.abrir(post, origen, alCambiar);
    }

    @Override
    public void abrirEditarPerfil() {
        panelEditarPerfil.cargarDatos();
        mostrarSeccion("EDITAR_PERFIL");
    }

    @Override
    public void usuarioActualizado(String usernameAnterior) {
        if (usuarioActual == null) {
            return;
        }
        GestorInstaPlus.guardarSesion(usuarioSO, usuarioActual);
        aplicarEstadoCuenta();
        if (usernameAnterior != null && !usernameAnterior.equals(usuarioActual.getUsername())) {
            conectarCliente(); // el socket se registra con el username nuevo
            refrescarMensajes();
            refrescarFeed();
        }
        refrescarPerfilPropio();
    }

    @Override
    public void enviarAlServidor(PaqueteInsta paquete, Consumer<PaqueteInsta> alResponder) {
        if (cliente == null) {
            if (alResponder != null) {
                alResponder.accept(PaqueteInsta.error(0, "No hay una sesión activa."));
            }
            return;
        }
        cliente.enviar(paquete, alResponder);
    }

    @Override
    public void mostrarAviso(String titulo, String texto) {
        aviso.mostrar(null, titulo, texto, null);
    }

    @Override
    public void actualizarNotificacionesInbox(int noLeidas) {
        if (panelMensajes != null) {
            panelMensajes.actualizarNotificaciones(noLeidas);
        }
    }

    @Override
    public void cerrarSesion() {
        liberarRecursos();
        usuarioActual = null;
        GestorInstaPlus.cerrarSesionGuardada(usuarioSO);
        panelLogin.limpiarUsername();
        panelLogin.limpiarPassword();
        lblConexion.setText("● Conectando…");
        lblConexion.setForeground(EstiloInsta.AMBAR);
        cardLayoutRaiz.show(panelRaiz, "LOGIN");
    }
}