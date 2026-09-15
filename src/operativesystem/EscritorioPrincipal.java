package operativesystem;
 
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.*;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.TreeExpansionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.nio.file.StandardCopyOption;
 
public class EscritorioPrincipal extends JFrame {
 
    private final Usuario usuarioActual;
    private final JDesktopPane escritorio;
    private JPanel panelIconos;
    private JButton btnAvatarSuperior;
    private File archivoCopiado;
    private boolean esCortar = false;
    private final Map<String, JInternalFrame> ventanasAbiertas = new HashMap<>();
 
   
    private JPanel panelVentanasTaskbar;
    private final Map<String, JButton> botonesTaskbar = new LinkedHashMap<>();
    private JLabel lblRelojTaskbar;
    private Timer timerRelojTaskbar;
    private static final Color TASKBAR_BOTON_NORMAL = new Color(55, 55, 55);
    private static final Color TASKBAR_BOTON_ACTIVO = TemaUI.ACCENT;
 
    public EscritorioPrincipal(Usuario usuario) {
        super("Mini-Windows - " + usuario.getUsername()
                + (usuario.isAdministrador() ? "  [ADMINISTRADOR]" : ""));
        this.usuarioActual = usuario;
 
        this.escritorio = new JDesktopPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
 
                if (usuarioActual.getRutaWallpaper() != null && !usuarioActual.getRutaWallpaper().isEmpty()) {
                    File imgFile = new File(usuarioActual.getRutaWallpaper());
                    if (imgFile.exists()) {
                        Image bg = new ImageIcon(imgFile.getAbsolutePath()).getImage();
                        g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g2.setPaint(new GradientPaint(0, 0, TemaUI.ACCENT_CLARO, 0, getHeight(), TemaUI.FONDO));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }
                } else {
                    g2.setPaint(new GradientPaint(0, 0, TemaUI.ACCENT_CLARO, 0, getHeight(), TemaUI.FONDO));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 650));
        setSize(1280, 800);
        setLocationRelativeTo(null);
 
        escritorio.setBackground(TemaUI.FONDO);
        panelIconos = construirPanelIconos();
        escritorio.add(panelIconos, Integer.valueOf(-100));
        escritorio.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                panelIconos.setBounds(0, 0, escritorio.getWidth(), escritorio.getHeight());
            }
        });
 
        add(construirBarraSuperior(), BorderLayout.NORTH);
        add(escritorio, BorderLayout.CENTER);
        add(construirBarraTareas(), BorderLayout.SOUTH);
 
      
        setExtendedState(JFrame.MAXIMIZED_BOTH);
 
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (timerRelojTaskbar != null) {
                    timerRelojTaskbar.stop();
                }
            }
        });
    }
 
   
 
    private JPanel construirBarraTareas() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(TemaUI.TASKBAR_FONDO);
        barra.setPreferredSize(new Dimension(0, 46));
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 0, 0, 90)));
 
        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.add(crearBotonInicio());
        barra.add(panelIzquierdo, BorderLayout.WEST);
 
        panelVentanasTaskbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 7));
        panelVentanasTaskbar.setOpaque(false);
        barra.add(panelVentanasTaskbar, BorderLayout.CENTER);
 
        lblRelojTaskbar = new JLabel("", SwingConstants.CENTER);
        lblRelojTaskbar.setForeground(Color.WHITE);
        lblRelojTaskbar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        actualizarRelojTaskbar();
        timerRelojTaskbar = new Timer(1000, e -> actualizarRelojTaskbar());
        timerRelojTaskbar.start();
 
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        panelDerecho.setOpaque(false);
        panelDerecho.add(lblRelojTaskbar);
        barra.add(panelDerecho, BorderLayout.EAST);
 
        return barra;
    }
 
    private void actualizarRelojTaskbar() {
        Date ahora = new Date();
        lblRelojTaskbar.setText("<html><div style='text-align:center;'>"
                + new SimpleDateFormat("HH:mm").format(ahora) + "<br>"
                + new SimpleDateFormat("dd/MM/yyyy").format(ahora) + "</div></html>");
    }
 
    private JButton crearBotonInicio() {
        JButton boton = new JButton("\u229E  Inicio");
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
 
        JPopupMenu menu = construirMenuInicio();
        boton.addActionListener(e -> menu.show(boton, 0, -menu.getPreferredSize().height));
        return boton;
    }
 
    private JPopupMenu construirMenuInicio() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(crearItemMenuInicio("Explorador", e -> abrirExplorador()));
        menu.add(crearItemMenuInicio("Editor de texto", e -> abrirEditorTexto()));
        menu.add(crearItemMenuInicio("Visor de imágenes", e -> abrirVisorImagenes()));
        menu.add(crearItemMenuInicio("Consola", e -> abrirConsola()));
        menu.add(crearItemMenuInicio("Reproductor", e -> abrirReproductor()));
        menu.add(crearItemMenuInicio("INSTA+", e -> abrirInstaPlus()));
        if (usuarioActual.isAdministrador()) {
            menu.add(crearItemMenuInicio("Administrar usuarios", e -> abrirAdministrarUsuarios()));
        }
        menu.addSeparator();
        menu.add(crearItemMenuInicio("Mi perfil", e -> abrirPerfil()));
        menu.add(crearItemMenuInicio("Cerrar sesión", e -> cerrarSesion()));
        return menu;
    }
 
    private JMenuItem crearItemMenuInicio(String texto, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.addActionListener(accion);
        return item;
    }
 
    private void alternarVentanaTaskbar(String clave) {
        JInternalFrame ventana = ventanasAbiertas.get(clave);
        if (ventana == null) {
            return;
        }
        try {
            if (ventana.isIcon()) {
                ventana.setIcon(false);
                ventana.setSelected(true);
            } else if (ventana.isSelected()) {
                ventana.setIcon(true);
            } else {
                ventana.setSelected(true);
            }
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }
 
    private void marcarBotonTaskbarActivo(String clave) {
        for (Map.Entry<String, JButton> entrada : botonesTaskbar.entrySet()) {
            entrada.getValue().setBackground(
                    entrada.getKey().equals(clave) ? TASKBAR_BOTON_ACTIVO : TASKBAR_BOTON_NORMAL);
        }
    }
 
    private JPanel construirPanelIconos() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 28));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
 
        int i = 0;
        panel.add(TemaUI.crearBotonApp("Explorador", "EX", TemaUI.colorApp(i++), e -> abrirExplorador()));
        panel.add(TemaUI.crearBotonApp("Editor de texto", "ED", TemaUI.colorApp(i++), e -> abrirEditorTexto()));
        panel.add(TemaUI.crearBotonApp("Visor de imágenes", "IMG", TemaUI.colorApp(i++), e -> abrirVisorImagenes()));
        panel.add(TemaUI.crearBotonApp("Consola", "CMD", TemaUI.colorApp(i++), e -> abrirConsola()));
        panel.add(TemaUI.crearBotonApp("Reproductor", "MUS", TemaUI.colorApp(i++), e -> abrirReproductor()));
        panel.add(TemaUI.crearBotonApp("INSTA+", "IG", TemaUI.colorApp(i++), e -> abrirInstaPlus()));
 
        if (usuarioActual.isAdministrador()) {
            panel.add(TemaUI.crearBotonApp("Administrar usuarios", "ADM", TemaUI.colorApp(i++), e -> abrirAdministrarUsuarios()));
        }
 
        return panel;
    }
 
    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(TemaUI.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaUI.BORDE),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
 
        JLabel lblLogo = new JLabel("Mini-Windows");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblLogo.setForeground(TemaUI.ACCENT_OSCURO);
        barra.add(lblLogo, BorderLayout.WEST);
 
        btnAvatarSuperior = new JButton();
        btnAvatarSuperior.setIcon(iconoDeAvatar(36));
        btnAvatarSuperior.setContentAreaFilled(false);
        btnAvatarSuperior.setBorderPainted(false);
        btnAvatarSuperior.setFocusPainted(false);
        btnAvatarSuperior.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAvatarSuperior.setToolTipText("Ver mi perfil");
        btnAvatarSuperior.addActionListener(e -> abrirPerfil());
 
        JLabel lblNombre = new JLabel(usuarioActual.getUsername()
                + (usuarioActual.isAdministrador() ? "  ·  Admin" : "") + "  ");
        lblNombre.setForeground(TemaUI.TEXTO_SUAVE);
 
        JButton btnMiPerfil = new JButton("Mi perfil");
        btnMiPerfil.setContentAreaFilled(false);
        btnMiPerfil.setFocusPainted(false);
        btnMiPerfil.setForeground(TemaUI.ACCENT_OSCURO);
        btnMiPerfil.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMiPerfil.addActionListener(e -> abrirPerfil());
 
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setForeground(new Color(190, 40, 40));
        btnCerrarSesion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
 
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecho.setOpaque(false);
        panelDerecho.add(lblNombre);
        panelDerecho.add(btnAvatarSuperior);
        panelDerecho.add(btnMiPerfil);
        panelDerecho.add(btnCerrarSesion);
        barra.add(panelDerecho, BorderLayout.EAST);
 
        return barra;
    }
 
    private Icon iconoDeAvatar(int diametro) {
        if (usuarioActual.getFotoPerfil() != null) {
            File foto = new File(usuarioActual.getFotoPerfil());
            if (foto.exists()) {
                return TemaUI.crearIconoCircularDeImagen(foto, diametro);
            }
        }
        String nombre = usuarioActual.getNombreCompleto();
        String iniciales = (nombre == null || nombre.isBlank()) ? "?" : nombre.substring(0, 1).toUpperCase();
        return TemaUI.crearIconoCircular(iniciales, TemaUI.ACCENT, diametro);
    }
 
    private void abrirPerfil() {
        if (traerAlFrenteSiExiste("perfil")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("Mi perfil", true, true, true, true);
        ventana.setSize(340, 520);
        ventana.setLayout(new BorderLayout(10, 10));
        ((JComponent) ventana.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
        JLabel lblFoto = new JLabel(iconoDeAvatar(120));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
 
        JPanel panelInfo = new JPanel();
        panelInfo.setOpaque(false);
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.add(crearLineaPerfil("Nombre:", usuarioActual.getNombreCompleto()));
        panelInfo.add(crearLineaPerfil("Usuario:", usuarioActual.getUsername()));
        panelInfo.add(crearLineaPerfil("Género:", String.valueOf(usuarioActual.getGenero())));
        panelInfo.add(crearLineaPerfil("Edad:", String.valueOf(usuarioActual.getEdad())));
        panelInfo.add(crearLineaPerfil("Registrado el:", usuarioActual.getFechaRegistroTexto()));
        panelInfo.add(crearLineaPerfil("Tipo de cuenta:",
                usuarioActual.isAdministrador() ? "Administrador" : "Estándar"));
 
        JPanel panelBotonesPerfil = new JPanel(new GridLayout(3, 1, 5, 5));
 
        JButton btnCambiarFoto = new JButton("Cambiar foto de perfil");
        btnCambiarFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                usuarioActual.setFotoPerfil(chooser.getSelectedFile().getAbsolutePath());
                try {
                    GestorArchivosBinarios.actualizarUsuario(usuarioActual);
                    lblFoto.setIcon(iconoDeAvatar(120));
                    btnAvatarSuperior.setIcon(iconoDeAvatar(36));
                } catch (ArchivoCorruptoException | IOException ex) {
                    JOptionPane.showMessageDialog(ventana, "No se pudo guardar la nueva foto: " + ex.getMessage());
                }
            }
        });
 
        JButton btnCambiarFondo = new JButton("Cambiar fondo de escritorio");
        btnCambiarFondo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                usuarioActual.setRutaWallpaper(chooser.getSelectedFile().getAbsolutePath());
                try {
                    GestorArchivosBinarios.actualizarUsuario(usuarioActual);
                    escritorio.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ventana, "Error guardando fondo: " + ex.getMessage());
                }
            }
        });
 
        JButton btnCambiarPass = new JButton("Cambiar Contraseña");
        btnCambiarPass.addActionListener(e -> abrirCambiarPassword());
 
        panelBotonesPerfil.add(btnCambiarFoto);
        panelBotonesPerfil.add(btnCambiarFondo);
        panelBotonesPerfil.add(btnCambiarPass);
 
        ventana.add(lblFoto, BorderLayout.NORTH);
        ventana.add(panelInfo, BorderLayout.CENTER);
        ventana.add(panelBotonesPerfil, BorderLayout.SOUTH);
 
        mostrarVentanaInterna("perfil", ventana);
    }
 
    private JPanel crearLineaPerfil(String etiqueta, String valor) {
        JPanel linea = new JPanel(new BorderLayout());
        linea.setOpaque(false);
        linea.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setForeground(TemaUI.TEXTO_SUAVE);
        JLabel lblValor = new JLabel(valor == null || valor.isBlank() ? "-" : valor);
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD));
        linea.add(lblEtiqueta, BorderLayout.WEST);
        linea.add(lblValor, BorderLayout.EAST);
        return linea;
    }
 
    private void abrirCambiarPassword() {
        if (traerAlFrenteSiExiste("cambiarPassword")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("Cambiar contraseña", true, true, false, true);
        ventana.setSize(360, 300);
        ventana.setLayout(new GridBagLayout());
        ((JComponent) ventana.getContentPane()).setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
 
        JPasswordField pwdActual = new JPasswordField(14);
        JPasswordField pwdNueva = new JPasswordField(14);
        JPasswordField pwdConfirmar = new JPasswordField(14);
 
        int fila = 0;
 
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Cambiar contraseña");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 15f));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        ventana.add(lblTitulo, gbc);
        gbc.gridwidth = 1;
        fila++;
 
        gbc.gridx = 0;
        gbc.gridy = fila;
        ventana.add(new JLabel("Contraseña actual:"), gbc);
        gbc.gridx = 1;
        ventana.add(TemaUI.crearCampoPassword(pwdActual), gbc);
        fila++;
 
        gbc.gridx = 0;
        gbc.gridy = fila;
        ventana.add(new JLabel("Nueva contraseña:"), gbc);
        gbc.gridx = 1;
        ventana.add(TemaUI.crearCampoPassword(pwdNueva), gbc);
        fila++;
 
        gbc.gridx = 0;
        gbc.gridy = fila;
        ventana.add(new JLabel("Confirmar nueva:"), gbc);
        gbc.gridx = 1;
        ventana.add(TemaUI.crearCampoPassword(pwdConfirmar), gbc);
        fila++;
 
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        JLabel lblRequisitos = new JLabel("<html>" + TemaUI.REQUISITOS_PASSWORD + "</html>");
        lblRequisitos.setFont(lblRequisitos.getFont().deriveFont(Font.PLAIN, 10f));
        lblRequisitos.setForeground(TemaUI.TEXTO_SUAVE);
        ventana.add(lblRequisitos, gbc);
        fila++;
 
        JLabel lblEstado = new JLabel(" ");
        lblEstado.setFont(lblEstado.getFont().deriveFont(11f));
        gbc.gridx = 0;
        gbc.gridy = fila;
        ventana.add(lblEstado, gbc);
        fila++;
 
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = TemaUI.crearBotonPrimario("Guardar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        ventana.add(panelBotones, gbc);
 
        btnCancelar.addActionListener(e -> ventana.dispose());
 
        btnGuardar.addActionListener(e -> {
            String actual = new String(pwdActual.getPassword());
            String nueva = new String(pwdNueva.getPassword());
            String confirmar = new String(pwdConfirmar.getPassword());
 
            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                lblEstado.setForeground(Color.RED);
                lblEstado.setText("Completa todos los campos.");
                return;
            }
            if (!nueva.equals(confirmar)) {
                lblEstado.setForeground(Color.RED);
                lblEstado.setText("La nueva contraseña y su confirmación no coinciden.");
                return;
            }
            if (!TemaUI.PATRON_PASSWORD_SEGURA.matcher(nueva).matches()) {
                lblEstado.setForeground(Color.RED);
                lblEstado.setText("La nueva contraseña no cumple los requisitos mínimos.");
                return;
            }
 
            try {
                boolean exito = GestorArchivosBinarios.cambiarPassword(
                        usuarioActual.getUsername(), actual, nueva);
                if (exito) {
                    usuarioActual.setPassword(nueva);
                    lblEstado.setForeground(new Color(16, 120, 60));
                    lblEstado.setText("Contraseña actualizada exitosamente.");
                    pwdActual.setText("");
                    pwdNueva.setText("");
                    pwdConfirmar.setText("");
                } else {
                    lblEstado.setForeground(Color.RED);
                    lblEstado.setText("La contraseña actual es incorrecta.");
                }
            } catch (Exception ex) {
                lblEstado.setForeground(Color.RED);
                lblEstado.setText("Error de archivo: " + ex.getMessage());
            }
        });
 
        mostrarVentanaInterna("cambiarPassword", ventana);
    }
 
    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Cerrar la sesión actual?", "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new PantallaLogin().setVisible(true));
        }
    }
 
    private File obtenerRaizDeTrabajo() {
        return new File(GestorArchivosBinarios.rutaCarpetaUsuario(usuarioActual.getUsername()));
    }
 
    private void abrirExplorador() {
        if (traerAlFrenteSiExiste("explorador")) {
            return;
        }
 
        File raiz = obtenerRaizDeTrabajo();
        if (!raiz.exists()) {
            raiz.mkdirs();
        }
 
        JInternalFrame ventana = new JInternalFrame(
                "Explorador - " + raiz.getName(), true, true, true, true);
        ventana.setSize(420, 420);
 
        JComboBox<String> cmbOrden = new JComboBox<>(new String[]{"Nombre", "Fecha", "Tipo", "Tamaño"});
        Comparator<File> comparadorInicial = comparadorPara((String) cmbOrden.getSelectedItem());
 
        DefaultMutableTreeNode nodoRaiz = construirNodo(raiz, comparadorInicial);
        cargarHijosReales(nodoRaiz, comparadorInicial);
        JTree arbol = new JTree(nodoRaiz);
        arbol.setRootVisible(true);
        arbol.expandPath(new TreePath(nodoRaiz));
 
        // Sin esto, Nimbus dibuja las carpetas como un simple punto
        javax.swing.filechooser.FileSystemView vistaSistema = javax.swing.filechooser.FileSystemView.getFileSystemView();
        arbol.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObj instanceof File) {
                    File archivo = (File) userObj;
                    String nombre = archivo.getName();
                    setText(nombre.isEmpty() ? archivo.getPath() : nombre);
                    setIcon(vistaSistema.getSystemIcon(archivo));
                }
                return this;
            }
        });
 
        // Carga perezosa
        arbol.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (esNodoSinCargar(nodo)) {
                    String orden = (String) cmbOrden.getSelectedItem();
                    cargarHijosReales(nodo, comparadorPara(orden));
                    ((DefaultTreeModel) arbol.getModel()).nodeStructureChanged(nodo);
                }
            }
 
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });
 
        JScrollPane scroll = new JScrollPane(arbol);
 
        JPanel panelBotones = new JPanel();
        JButton btnCortar = new JButton("Cortar");
        btnCortar.addActionListener(e -> {
            cortarArchivo(arbol, ventana);
        });
 
        JButton btnCrear = new JButton("Nueva carpeta");
        btnCrear.addActionListener(e -> crearCarpeta(arbol, ventana, raiz));
 
        JButton btnRenombrar = new JButton("Renombrar");
        btnRenombrar.addActionListener(e -> renombrarArchivo(arbol, ventana, raiz));
 
        JButton btnCopiar = new JButton("Copiar");
        btnCopiar.addActionListener(e -> copiarArchivo(arbol, ventana));
 
        JButton btnPegar = new JButton("Pegar");
        btnPegar.addActionListener(e -> pegarArchivo(arbol, ventana, raiz));

        JButton btnBorrar = new JButton("Borrar");
        btnBorrar.addActionListener(e -> borrarArchivo(arbol, ventana, raiz));

        JButton btnOrganizar = new JButton("Organizar carpeta seleccionada");
        btnOrganizar.addActionListener(e -> organizarCarpetaSeleccionada(arbol, ventana, raiz, btnOrganizar));
 
        cmbOrden.addActionListener(e -> actualizarArbol(arbol, raiz, (String) cmbOrden.getSelectedItem()));
 
        panelBotones.add(btnCortar);
        panelBotones.add(btnCrear);
        panelBotones.add(btnRenombrar);
        panelBotones.add(btnCopiar);
        panelBotones.add(btnPegar);
        panelBotones.add(btnBorrar);
        panelBotones.add(btnOrganizar);
        panelBotones.add(new JLabel("Ordenar: "));
        panelBotones.add(cmbOrden);
 
        ventana.setLayout(new BorderLayout());
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);
 
        mostrarVentanaInterna("explorador", ventana);
    }
 
    private DefaultMutableTreeNode construirNodo(File archivo, Comparator<File> comparador) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo);
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null && hijos.length > 0) {
                nodo.add(new DefaultMutableTreeNode("cargando..."));
            }
        }
        return nodo;
    }
 
    private boolean esNodoSinCargar(DefaultMutableTreeNode nodo) {
        return nodo.getChildCount() == 1
                && !(((DefaultMutableTreeNode) nodo.getChildAt(0)).getUserObject() instanceof File);
    }
 
    private void cargarHijosReales(DefaultMutableTreeNode nodo, Comparator<File> comparador) {
        Object userObj = nodo.getUserObject();
        if (!(userObj instanceof File) || !esNodoSinCargar(nodo)) {
            return;
        }
        nodo.removeAllChildren();
        File[] hijos = ((File) userObj).listFiles();
        if (hijos != null) {
            Arrays.sort(hijos, comparador);
            for (File hijo : hijos) {
                nodo.add(construirNodo(hijo, comparador));
            }
        }
    }
 
    private File obtenerArchivoSeleccionado(JTree arbol) {
        TreePath ruta = arbol.getSelectionPath();
        if (ruta == null) {
            return null;
        }
        Object objeto = ((DefaultMutableTreeNode) ruta.getLastPathComponent()).getUserObject();
        return objeto instanceof File ? (File) objeto : null;
    }
 
    private Comparator<File> comparadorPara(String orden) {
        Comparator<File> comparador;
        switch (orden) {
            case "Fecha":
                comparador = Comparator.comparingLong(File::lastModified);
                break;
            case "Tipo":
                comparador = Comparator.comparing((File archivo) -> {
                    String nombre = archivo.getName();
                    int punto = nombre.lastIndexOf('.');
                    return archivo.isDirectory() ? "" : (punto >= 0 ? nombre.substring(punto + 1) : "");
                }, String.CASE_INSENSITIVE_ORDER).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Tamaño":
                comparador = Comparator.comparingLong(File::length).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                comparador = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }
        return Comparator.comparing(File::isFile).thenComparing(comparador);
    }
 
    private void actualizarArbol(JTree arbol, File raiz, String orden) {
        Comparator<File> comparador = comparadorPara(orden);
        DefaultMutableTreeNode nodoRaiz = construirNodo(raiz, comparador);
        cargarHijosReales(nodoRaiz, comparador);
        arbol.setModel(new DefaultTreeModel(nodoRaiz));
        arbol.expandPath(new TreePath(nodoRaiz));
    }
 
    private boolean estaDentroDeRaiz(File archivo, File raiz) {
        try {
            String rutaRaiz = raiz.getCanonicalPath();
            String rutaArchivo = archivo.getCanonicalPath();
            return rutaArchivo.equals(rutaRaiz) || rutaArchivo.startsWith(rutaRaiz + File.separator);
        } catch (IOException e) {
            return false;
        }
    }
 
    private String solicitarNombreSeguro(Component padre, String mensaje, String valorInicial) {
        String nombre = (String) JOptionPane.showInputDialog(padre, mensaje, "Explorador",
                JOptionPane.QUESTION_MESSAGE, null, null, valorInicial);
        if (nombre == null) {
            return null;
        }
        nombre = nombre.trim();
        if (nombre.isEmpty() || nombre.equals(".") || nombre.equals("..") || !new File(nombre).getName().equals(nombre)) {
            JOptionPane.showMessageDialog(padre, "Ingresa un nombre de archivo o carpeta válido.");
            return null;
        }
        return nombre;
    }
 
    private void crearCarpeta(JTree arbol, JInternalFrame ventana, File raiz) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        File destino = seleccion != null && seleccion.isDirectory() ? seleccion
                : (seleccion != null ? seleccion.getParentFile() : raiz);
        String nombre = solicitarNombreSeguro(ventana, "Nombre de la carpeta:", "");
        if (nombre == null) {
            return;
        }
        File nuevaCarpeta = new File(destino, nombre);
        if (!estaDentroDeRaiz(nuevaCarpeta, raiz) || !nuevaCarpeta.mkdir()) {
            JOptionPane.showMessageDialog(ventana, "No se pudo crear la carpeta. Verifica que no exista.", "Explorador", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarArbol(arbol, raiz, "Nombre");
        expandirYSeleccionar(arbol, nuevaCarpeta);
    }
 
  
    private void expandirYSeleccionar(JTree arbol, File objetivo) {
        DefaultMutableTreeNode raizNodo = (DefaultMutableTreeNode) arbol.getModel().getRoot();
        DefaultMutableTreeNode encontrado = buscarNodoPorArchivo(raizNodo, objetivo);
        if (encontrado != null) {
            TreePath ruta = new TreePath(encontrado.getPath());
            arbol.expandPath(ruta.getParentPath() != null ? ruta.getParentPath() : ruta);
            arbol.setSelectionPath(ruta);
            arbol.scrollPathToVisible(ruta);
        } else {
            arbol.expandPath(new TreePath(raizNodo));
        }
    }
 
    private DefaultMutableTreeNode buscarNodoPorArchivo(DefaultMutableTreeNode nodo, File objetivo) {
        Object userObj = nodo.getUserObject();
        if (userObj instanceof File && ((File) userObj).equals(objetivo)) {
            return nodo;
        }
        for (int i = 0; i < nodo.getChildCount(); i++) {
            DefaultMutableTreeNode resultado = buscarNodoPorArchivo((DefaultMutableTreeNode) nodo.getChildAt(i), objetivo);
            if (resultado != null) {
                return resultado;
            }
        }
        return null;
    }
 
    private void renombrarArchivo(JTree arbol, JInternalFrame ventana, File raiz) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        if (seleccion == null || seleccion.equals(raiz)) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un archivo o carpeta que desees renombrar.");
            return;
        }
        String nombre = solicitarNombreSeguro(ventana, "Nuevo nombre:", seleccion.getName());
        if (nombre == null) {
            return;
        }
        File destino = new File(seleccion.getParentFile(), nombre);
        try {
            if (!estaDentroDeRaiz(destino, raiz) || destino.exists()) {
                throw new IOException("El destino ya existe.");
            }
            Files.move(seleccion.toPath(), destino.toPath());
            actualizarArbol(arbol, raiz, "Nombre");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventana, "No se pudo renombrar: " + ex.getMessage(), "Explorador", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void copiarArchivo(JTree arbol, JInternalFrame ventana) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        if (seleccion == null) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un archivo o carpeta para copiar.");
            return;
        }
        archivoCopiado = seleccion;
        esCortar = false;
    }
 
    private void pegarArchivo(JTree arbol, JInternalFrame ventana, File raiz) {
        if (archivoCopiado == null || !archivoCopiado.exists()) {
            JOptionPane.showMessageDialog(ventana, "Primero copia un archivo o carpeta existente.");
            return;
        }
        File seleccion = obtenerArchivoSeleccionado(arbol);
        File destinoCarpeta = seleccion != null && seleccion.isDirectory() ? seleccion
                : (seleccion != null ? seleccion.getParentFile() : raiz);
 
 
        if (archivoCopiado.isDirectory() && estaDentroDeRaiz(destinoCarpeta, archivoCopiado)) {
            destinoCarpeta = archivoCopiado.getParentFile();
        }
 
        File destino = nombreDisponible(destinoCarpeta, archivoCopiado.getName());
        try {
            if (esCortar) {
                Files.move(archivoCopiado.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                archivoCopiado = null;
                esCortar = false;
            } else {
                copiarRecursivamente(archivoCopiado, destino);
            }
 
            actualizarArbol(arbol, raiz, "Nombre");
            expandirYSeleccionar(arbol, destino);
 
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventana, "No se pudo pegar: " + ex.getMessage(), "Explorador", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private File nombreDisponible(File carpeta, String nombre) {
        File candidato = new File(carpeta, nombre);
        if (!candidato.exists()) {
            return candidato;
        }
        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        String extension = punto > 0 ? nombre.substring(punto) : "";
        int contador = 1;
        do {
            candidato = new File(carpeta, base + " (copia " + contador++ + ")" + extension);
        } while (candidato.exists());
        return candidato;
    }
 
    /**
     * Borra un archivo o carpeta (recursivamente si es carpeta), pidiendo
     * confirmación antes — a diferencia de cortar/copiar/pegar, esta acción no
     * se puede deshacer, así que el diálogo de confirmación no es opcional.
     */
    private void borrarArchivo(JTree arbol, JInternalFrame ventana, File raiz) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        if (seleccion == null || seleccion.equals(raiz)) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un archivo o carpeta para borrar.");
            return;
        }
        if (!estaDentroDeRaiz(seleccion, raiz)) {
            JOptionPane.showMessageDialog(ventana, "No puedes borrar algo fuera de tu carpeta de usuario.",
                    "Explorador", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mensaje = seleccion.isDirectory()
                ? "¿Borrar la carpeta \"" + seleccion.getName() + "\" y TODO su contenido?\nEsta acción no se puede deshacer."
                : "¿Borrar el archivo \"" + seleccion.getName() + "\"?\nEsta acción no se puede deshacer.";
        int confirmacion = JOptionPane.showConfirmDialog(ventana, mensaje, "Confirmar borrado",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        if (!borrarRecursivamente(seleccion)) {
            JOptionPane.showMessageDialog(ventana, "No se pudo borrar \"" + seleccion.getName() + "\" por completo.",
                    "Explorador", JOptionPane.ERROR_MESSAGE);
        }

        // Si lo que acabamos de borrar era justo lo que tenías copiado/cortado,
        // esa referencia ya no sirve — sin esto, "Pegar" fallaría después con un
        // error confuso ("el archivo no existe") en vez de simplemente no ofrecer
        // pegar nada.
        if (seleccion.equals(archivoCopiado)) {
            archivoCopiado = null;
        }

        actualizarArbol(arbol, raiz, "Nombre");
    }

    private boolean borrarRecursivamente(File archivo) {
        if (archivo.isDirectory()) {
            File[] hijos = archivo.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (!borrarRecursivamente(hijo)) {
                        return false;
                    }
                }
            }
        }
        return archivo.delete();
    }

    /**
     * Clasifica los archivos SUELTOS de la carpeta seleccionada (no entra a
     * subcarpetas) en imagenes/musica/documentos/otros, cada una como
     * subcarpeta dentro de la carpeta seleccionada.
     *
     * Corre en su propio Thread porque recorrer y mover archivos es una
     * operación de disco que puede tardar en una carpeta grande — si esto
     * corriera en el hilo de Swing, la ventana entera del explorador (y el
     * resto de la interfaz) se congelaría hasta que termine. El hilo nunca
     * toca el JTree directamente: solo prepara los datos, y la actualización
     * de la interfaz al final se pide con SwingUtilities.invokeLater, igual
     * que hace el reproductor de música al terminar una canción.
     */
    private void organizarCarpetaSeleccionada(JTree arbol, JInternalFrame ventana, File raiz, JButton boton) {
        File carpetaSeleccionada = obtenerArchivoSeleccionado(arbol);
        if (carpetaSeleccionada == null || !carpetaSeleccionada.isDirectory()) {
            JOptionPane.showMessageDialog(ventana, "Selecciona una carpeta para organizar.");
            return;
        }
        if (!estaDentroDeRaiz(carpetaSeleccionada, raiz)) {
            JOptionPane.showMessageDialog(ventana, "No puedes organizar algo fuera de tu carpeta de usuario.",
                    "Explorador", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boton.setEnabled(false); // evita lanzar dos organizaciones a la vez sobre la misma carpeta

        Thread hiloOrganizador = new Thread(() -> {
            // Cada categoría se arma con TU lista enlazada, no con ArrayList —
            // aquí es literalmente el lugar que sugiere el enunciado para usarla.
            ListaEnlazada<File> imagenes = new ListaEnlazada<>();
            ListaEnlazada<File> musica = new ListaEnlazada<>();
            ListaEnlazada<File> documentos = new ListaEnlazada<>();
            ListaEnlazada<File> otros = new ListaEnlazada<>();

            File[] archivos = carpetaSeleccionada.listFiles(File::isFile);
            if (archivos != null) {
                for (File archivo : archivos) {
                    String nombre = archivo.getName().toLowerCase();
                    if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")
                            || nombre.endsWith(".gif") || nombre.endsWith(".bmp") || nombre.endsWith(".webp")) {
                        imagenes.agregar(archivo);
                    } else if (nombre.endsWith(".mp3") || nombre.endsWith(".wav") || nombre.endsWith(".flac")) {
                        musica.agregar(archivo);
                    } else if (nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".docx")
                            || nombre.endsWith(".doc") || nombre.endsWith(".xlsx") || nombre.endsWith(".pptx")) {
                        documentos.agregar(archivo);
                    } else {
                        otros.agregar(archivo);
                    }
                }
            }

            int movidos = moverCategoria(carpetaSeleccionada, "imagenes", imagenes)
                    + moverCategoria(carpetaSeleccionada, "musica", musica)
                    + moverCategoria(carpetaSeleccionada, "documentos", documentos)
                    + moverCategoria(carpetaSeleccionada, "otros", otros);

            SwingUtilities.invokeLater(() -> {
                boton.setEnabled(true);
                actualizarArbol(arbol, raiz, "Nombre");
                if (movidos == 0) {
                    JOptionPane.showMessageDialog(ventana,
                            "No había archivos sueltos para organizar en esta carpeta.");
                } else {
                    JOptionPane.showMessageDialog(ventana, "Se organizaron " + movidos + " archivo(s).");
                }
            });
        }, "organizador-archivos");

        hiloOrganizador.start();
    }

    /** Mueve todos los archivos de una ListaEnlazada a su subcarpeta de categoría. Devuelve cuántos se movieron. */
    private int moverCategoria(File carpetaBase, String categoria, ListaEnlazada<File> archivos) {
        if (archivos.estaVacia()) {
            return 0;
        }
        File carpetaDestino = new File(carpetaBase, categoria);
        carpetaDestino.mkdirs();
        int movidos = 0;
        for (File archivo : archivos) { // recorrido con for-each gracias a que ListaEnlazada implementa Iterable
            try {
                Files.move(archivo.toPath(), nombreDisponible(carpetaDestino, archivo.getName()).toPath());
                movidos++;
            } catch (IOException ignored) {
                // Si un archivo puntual falla (ej. está abierto en otro programa),
                // seguimos con los demás en vez de abortar toda la organización.
            }
        }
        return movidos;
    }

    private void copiarRecursivamente(File origen, File destino) throws IOException {
        if (origen.isDirectory()) {
            if (!destino.mkdirs() && !destino.isDirectory()) {
                throw new IOException("No se pudo crear " + destino.getName());
            }
            File[] hijos = origen.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    if (hijo.equals(destino)) {
                        continue;
                    }
                    copiarRecursivamente(hijo, new File(destino, hijo.getName()));
                }
            }
        } else {
            Files.copy(origen.toPath(), destino.toPath());
        }
    }
 
    private void cortarArchivo(JTree arbol, JInternalFrame ventana) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        if (seleccion == null) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un archivo o carpeta para cortar.");
            return;
        }
        archivoCopiado = seleccion;
        esCortar = true;
    }
 
    private void abrirEditorTexto() {
        if (traerAlFrenteSiExiste("editor")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("Editor de texto", true, true, true, true);
        ventana.setSize(500, 400);
        ventana.setLayout(new BorderLayout());
 
        JTextPane areaTexto = new JTextPane();
        areaTexto.setEditorKit(new RTFEditorKit());
        JScrollPane scroll = new JScrollPane(areaTexto);
 
        JToolBar barraFormato = new JToolBar();
        barraFormato.setFloatable(false);
 
        JButton btnColor = new JButton("Color");
        btnColor.addActionListener(e -> {
            Color color = JColorChooser.showDialog(ventana, "Color del texto", Color.BLACK);
            if (color != null) {
                StyledDocument doc = areaTexto.getStyledDocument();
                MutableAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, color);
                aplicarEstiloASeleccionOTodo(areaTexto, doc, attrs);
            }
        });
 
        String[] fuentesDisponibles = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        JComboBox<String> cmbFuente = new JComboBox<>(fuentesDisponibles);
        cmbFuente.setSelectedItem("SansSerif");
        cmbFuente.addActionListener(e -> {
            StyledDocument doc = areaTexto.getStyledDocument();
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, (String) cmbFuente.getSelectedItem());
            aplicarEstiloASeleccionOTodo(areaTexto, doc, attrs);
        });
 
        JSpinner spnTamano = new JSpinner(new SpinnerNumberModel(14, 6, 96, 1));
        spnTamano.addChangeListener(e -> {
            StyledDocument doc = areaTexto.getStyledDocument();
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontSize(attrs, (Integer) spnTamano.getValue());
            aplicarEstiloASeleccionOTodo(areaTexto, doc, attrs);
        });
 
        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.addActionListener(e -> abrirArchivoTexto(ventana, areaTexto));
 
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarArchivoTexto(ventana, areaTexto));
 
        barraFormato.add(btnAbrir);
        barraFormato.add(btnGuardar);
        barraFormato.addSeparator();
        barraFormato.add(btnColor);
        barraFormato.add(new JLabel(" Fuente: "));
        barraFormato.add(cmbFuente);
        barraFormato.add(new JLabel(" Tamaño: "));
        barraFormato.add(spnTamano);
 
        ventana.add(barraFormato, BorderLayout.NORTH);
        ventana.add(scroll, BorderLayout.CENTER);
 
        mostrarVentanaInterna("editor", ventana);
    }
 
    private void aplicarEstiloASeleccionOTodo(JTextPane areaTexto, StyledDocument doc, MutableAttributeSet attrs) {
        int inicio = areaTexto.getSelectionStart();
        int fin = areaTexto.getSelectionEnd();
        if (inicio == fin) {
            doc.setCharacterAttributes(0, doc.getLength(), attrs, false);
        } else {
            doc.setCharacterAttributes(inicio, fin - inicio, attrs, false);
        }
    }
 
    private void abrirArchivoTexto(Component padre, JTextPane areaTexto) {
        JFileChooser chooser = new JFileChooser(obtenerRaizDeTrabajo());
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Texto (.txt)", "txt"));
        if (chooser.showOpenDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                byte[] contenido = Files.readAllBytes(archivo.toPath());
                String inicio = new String(contenido, 0, Math.min(contenido.length, 5), StandardCharsets.US_ASCII);
                if (inicio.startsWith("{\\rtf")) {
                    areaTexto.setText("");
                    try (InputStream entrada = new ByteArrayInputStream(contenido)) {
                        new RTFEditorKit().read(entrada, areaTexto.getDocument(), 0);
                    }
                } else {
                    areaTexto.setText(new String(contenido, StandardCharsets.UTF_8));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo abrir el archivo: " + ex.getMessage());
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(padre, "El formato del archivo no es válido.");
            }
        }
    }
 
    private void guardarArchivoTexto(Component padre, JTextPane areaTexto) {
        JFileChooser chooser = new JFileChooser(obtenerRaizDeTrabajo());
        if (chooser.showSaveDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".txt")) {
                destino = new File(destino.getAbsolutePath() + ".txt");
            }
            try (OutputStream salida = new FileOutputStream(destino)) {
                new RTFEditorKit().write(salida, areaTexto.getStyledDocument(), 0,
                        areaTexto.getDocument().getLength());
                JOptionPane.showMessageDialog(padre, "Archivo guardado correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo guardar el archivo: " + ex.getMessage());
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo leer el contenido a guardar.");
            }
        }
    }
 
    private void abrirVisorImagenes() {
        if (traerAlFrenteSiExiste("visor")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("Visor de imágenes", true, true, true, true);
        ventana.setSize(500, 450);
        ventana.setLayout(new BorderLayout());
 
        JLabel lblImagen = new JLabel("Selecciona una carpeta con imágenes", SwingConstants.CENTER);
        JScrollPane scroll = new JScrollPane(lblImagen);
        
        JLabel lblInfo = new JLabel("Sin imagenes cargadas", SwingConstants.CENTER);
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD, 12f));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
 
        List<File> imagenes = new ArrayList<>();
        int[] indiceActual = {-1};
 
        JButton btnCarpeta = new JButton("Abrir imagen / carpeta");
        JButton btnAnterior = new JButton("◄ Anterior");
        JButton btnSiguiente = new JButton("Siguiente ►");
        
        Runnable actualizarVista = () -> {
            mostrarImagenActual(lblImagen, lblInfo, imagenes, indiceActual[0]);
        };
 
        btnCarpeta.addActionListener(e -> {
            File carpetaImagenes = new File(obtenerRaizDeTrabajo(), "Mis Imágenes");
            File carpetaInicial = carpetaImagenes.exists() ? carpetaImagenes : obtenerRaizDeTrabajo();

            JFileChooser chooser = new JFileChooser(carpetaInicial);
            
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagenes (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
            
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                File eleccion = chooser.getSelectedFile();
                imagenes.clear();
                
                if (eleccion.isDirectory()) {
                    buscarImagenesRecursivo(eleccion, imagenes);
                    indiceActual[0] = imagenes.isEmpty() ? -1 : 0;
                } else {
                File carpetaPadre = eleccion.getParentFile();
                    if (carpetaPadre != null) {
                        buscarImagenesRecursivo(carpetaPadre, imagenes);
                    } else {
                    imagenes.add(eleccion);
                    }
                    indiceActual[0] = imagenes.indexOf(eleccion);
                    if (indiceActual[0] == -1 && !imagenes.isEmpty()) {
                        indiceActual[0] = 0;
                    }
                }
                actualizarVista.run();
            }
        });
 
        btnAnterior.addActionListener(e -> {
            if (!imagenes.isEmpty()) {
                indiceActual[0] = (indiceActual[0] - 1 + imagenes.size()) % imagenes.size();
                actualizarVista.run();
            }
        });
 
        btnSiguiente.addActionListener(e -> {
            if (!imagenes.isEmpty()) {
                indiceActual[0] = (indiceActual[0] + 1) % imagenes.size();
                actualizarVista.run();
            }
        });
        
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(lblInfo, BorderLayout.CENTER);
        
 
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnCarpeta);
        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);
 
        ventana.add(panelSuperior, BorderLayout.NORTH);
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);
        
        actualizarVista.run();
 
        mostrarVentanaInterna("visor", ventana);
    }
 
 
    private void buscarImagenesRecursivo(File carpeta, List<File> destino) {
        File[] archivos = carpeta.listFiles();
        if (archivos == null) {
            return;
        }
        for (File f : archivos) {
            if (f.isDirectory()) {
                buscarImagenesRecursivo(f, destino);
            } else {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                    destino.add(f);
                }
            }
        }
    }
 
    private void mostrarImagenActual(JLabel lblImagen, JLabel lblInfo, List<File> imagenes, int indice) {
        if (indice < 0 || indice >= imagenes.size()) {
            lblImagen.setIcon(null);
            lblImagen.setText("No hay imágenes seleccionadas");
            lblInfo.setText("0 / 0 - Sin imagenes");
            return;
        }
        File imgFile = imagenes.get(indice);
        ImageIcon iconoOriginal = new ImageIcon(imgFile.getAbsolutePath());
        
        Image img = iconoOriginal.getImage();
        
        int anchoOriginal = iconoOriginal.getIconWidth();
        int altoOriginal = iconoOriginal.getIconHeight();
        
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            lblImagen.setIcon(null);
            lblImagen.setText("No se pudo cargar la imagen");
            return;
        }
        
        int anchoMax = 440;
        int altoMax = 320;
        
        double escala = Math.min((double) anchoMax / anchoOriginal, (double) altoMax / altoOriginal);
        int nuevoAncho = (int) (anchoOriginal * escala);
        int nuevoAlto = (int) (altoOriginal * escala);
        
        java.awt.image.BufferedImage imagenEscalada = new java.awt.image.BufferedImage(nuevoAncho, nuevoAlto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagenEscalada.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.drawImage(img, 0, 0, nuevoAncho, nuevoAlto, null);
        g2.dispose();
        lblImagen.setIcon(new ImageIcon(imagenEscalada));
        lblImagen.setText(null);
        lblInfo.setText(imgFile.getName() + " (" + (indice + 1) + " de " + imagenes.size() + ")");
 
        lblImagen.revalidate();
        lblImagen.repaint();
    
    }
 
    private void abrirConsola() {
        if (traerAlFrenteSiExiste("consola")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("Consola", true, true, true, true);
        ventana.setSize(500, 350);
        ventana.setLayout(new BorderLayout());
 
        JTextArea areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(areaSalida);
 
        File raizPermitida = obtenerRaizDeTrabajo();
        File[] carpetaActual = {raizPermitida};
 
        JTextField campoComando = new JTextField();
        areaSalida.append("Mini-Windows CMD. Escribe 'help' para ver los comandos disponibles.\n");
        areaSalida.append(carpetaActual[0].getAbsolutePath() + ">\n");
 
        campoComando.addActionListener((ActionEvent e) -> {
            String comando = campoComando.getText().trim();
            areaSalida.append(carpetaActual[0].getAbsolutePath() + "> " + comando + "\n");
            String resultado = procesarComandoConsola(comando, carpetaActual, raizPermitida, areaSalida);
            if (resultado != null) {
                areaSalida.append(resultado + "\n");
            }
            campoComando.setText("");
            areaSalida.setCaretPosition(areaSalida.getDocument().getLength());
        });
 
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(campoComando, BorderLayout.SOUTH);
        ventana.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {
                campoComando.requestFocusInWindow();
            }
        });
 
        mostrarVentanaInterna("consola", ventana);
        SwingUtilities.invokeLater(campoComando::requestFocusInWindow);
    }
 
    private String procesarComandoConsola(String comando, File[] carpetaActual, File raizPermitida, JTextArea areaSalidaConsola) {
        if (comando.isEmpty()) {
            return null;
        }
 
        String[] partes = comando.split("\\s+", 2);
        String instruccion = partes[0].toLowerCase();
        String argumento = partes.length > 1 ? partes[1] : "";
 
        switch (instruccion) {
            case "help":
            case "ayuda":
                return "Comandos disponibles:\n"
                        + "  help                  Muestra esta ayuda\n"
                        + "  mkdir <nombre>        Crea una carpeta\n"
                        + "  rmdir <nombre>        Elimina una carpeta vacía\n"
                        + "  del <archivo>         Elimina un archivo\n"
                        + "  ren <viejo> <nuevo>    Renombra un archivo o carpeta\n"
                        + "  copy <origen> <dest>  Copia un archivo\n"
                        + "  cd <carpeta>          Entra a una carpeta\n"
                        + "  cd..                  Sube a la carpeta anterior\n"
                        + "  dir                   Lista el contenido de la carpeta actual\n"
                        + "  cls                   Limpia la pantalla\n"
                        + "  echo <texto>          Muestra un texto\n"
                        + "  whoami                Muestra el usuario actual\n"
                        + "  date                  Muestra la fecha\n"
                        + "  time                  Muestra la hora\n"
                        + "  ver                   Muestra la versión del sistema";
 
            case "cls":
                areaSalidaConsola.setText("");
                return null;
 
            case "mkdir":
                if (argumento.isEmpty()) {
                    return "Uso: mkdir <nombre>";
                }
                if (!esNombreSeguro(argumento)) {
                    return "Nombre de carpeta no válido.";
                }
                boolean creado = new File(carpetaActual[0], argumento).mkdir();
                return creado ? "Carpeta creada." : "No se pudo crear la carpeta.";
 
            case "rm":
            case "rmdir":
                if (argumento.isEmpty()) {
                    return "Uso: " + instruccion + " <nombre>";
                }
                File aEliminar = new File(carpetaActual[0], argumento);
                if (!esNombreSeguro(argumento) || !estaDentroDeRaiz(aEliminar, raizPermitida)) {
                    return "No puedes eliminar fuera de tu carpeta de usuario.";
                }
                boolean eliminado = aEliminar.isDirectory() && aEliminar.delete();
                return eliminado ? "Eliminado." : "No se pudo eliminar (¿existe, es una carpeta y está vacía?).";
 
            case "del":
                if (argumento.isEmpty()) {
                    return "Uso: del <archivo>";
                }
                File archivoAEliminar = new File(carpetaActual[0], argumento);
                if (!esNombreSeguro(argumento) || !estaDentroDeRaiz(archivoAEliminar, raizPermitida)) {
                    return "No puedes eliminar fuera de tu carpeta de usuario.";
                }
                boolean archivoEliminado = archivoAEliminar.isFile() && archivoAEliminar.delete();
                return archivoEliminado ? "Archivo eliminado." : "No se pudo eliminar (¿existe y es un archivo?).";
 
            case "ren":
                String[] argsRen = argumento.split("\\s+", 2);
                if (argsRen.length < 2 || argsRen[0].isEmpty() || argsRen[1].isEmpty()) {
                    return "Uso: ren <nombre actual> <nombre nuevo>";
                }
                if (!esNombreSeguro(argsRen[0]) || !esNombreSeguro(argsRen[1])) {
                    return "Nombre no válido.";
                }
                File origenRen = new File(carpetaActual[0], argsRen[0]);
                File destinoRen = new File(carpetaActual[0], argsRen[1]);
                if (!origenRen.exists()) {
                    return "No existe: " + argsRen[0];
                }
                if (destinoRen.exists()) {
                    return "Ya existe un archivo o carpeta con ese nombre.";
                }
                return origenRen.renameTo(destinoRen) ? "Renombrado." : "No se pudo renombrar.";
 
            case "copy":
                String[] argsCopy = argumento.split("\\s+", 2);
                if (argsCopy.length < 2 || argsCopy[0].isEmpty() || argsCopy[1].isEmpty()) {
                    return "Uso: copy <origen> <destino>";
                }
                if (!esNombreSeguro(argsCopy[0]) || !esNombreSeguro(argsCopy[1])) {
                    return "Nombre no válido.";
                }
                File origenCopy = new File(carpetaActual[0], argsCopy[0]);
                File destinoCopy = new File(carpetaActual[0], argsCopy[1]);
                if (!origenCopy.isFile()) {
                    return "El origen no existe o no es un archivo.";
                }
                if (destinoCopy.exists()) {
                    return "Ya existe un archivo con ese nombre.";
                }
                try {
                    java.nio.file.Files.copy(origenCopy.toPath(), destinoCopy.toPath());
                    return "Copiado.";
                } catch (IOException ex) {
                    return "No se pudo copiar: " + ex.getMessage();
                }
 
            case "echo":
                return argumento;
 
            case "whoami":
                return usuarioActual.getUsername()
                        + (usuarioActual.isAdministrador() ? " (administrador)" : " (estándar)");
 
            case "ver":
                return "Mini-Windows [Versión 1.0]";
 
            case "cd":
                if (argumento.isEmpty()) {
                    return "Uso: cd <carpeta>";
                }
                File nuevaCarpeta = new File(carpetaActual[0], argumento);
                if (nuevaCarpeta.isDirectory() && estaDentroDeRaiz(nuevaCarpeta, raizPermitida)) {
                    carpetaActual[0] = nuevaCarpeta;
                    return null;
                }
                return "La carpeta no existe o está fuera de tu espacio de trabajo.";
 
            case "cd..":
                File padre = carpetaActual[0].getParentFile();
                if (padre != null && estaDentroDeRaiz(padre, raizPermitida)) {
                    carpetaActual[0] = padre;
                }
                return null;
 
            case "dir":
                File[] contenido = carpetaActual[0].listFiles();
                if (contenido == null || contenido.length == 0) {
                    return "(carpeta vacía)";
                }
                StringBuilder sb = new StringBuilder();
                for (File f : contenido) {
                    sb.append(f.isDirectory() ? "<DIR>  " : "       ").append(f.getName()).append("\n");
                }
                return sb.toString().trim();
 
            case "date":
                return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
 
            case "time":
                return new SimpleDateFormat("HH:mm:ss").format(new Date());
 
            default:
                return "Comando no reconocido: " + instruccion + " (escribe 'help' para ver los comandos disponibles)";
        }
    }
 
    private boolean esNombreSeguro(String nombre) {
        return !nombre.isBlank() && !nombre.equals(".") && !nombre.equals("..")
                && new File(nombre).getName().equals(nombre);
    }
 
    private void abrirReproductor() {
        if (traerAlFrenteSiExiste("reproductor")) {
            return;
        }
 
        File carpetaMusica = new File(obtenerRaizDeTrabajo(), "Música");
        if (!carpetaMusica.exists()) {
            carpetaMusica.mkdirs();
        }
 
        mostrarVentanaInterna("reproductor", new Reproductormusica(carpetaMusica));
    }
 
    private void abrirInstaPlus() {
        if (traerAlFrenteSiExiste("instaplus")) {
            return;
        }
 
        JInternalFrame ventana = new JInternalFrame("INSTA+", true, true, true, true);
        ventana.setSize(760, 600);
        ventana.setLayout(new BorderLayout());
 
        
        PantallaInstaPlus panelInstaPlus = new PantallaInstaPlus();
        ventana.add(panelInstaPlus, BorderLayout.CENTER);
 
        mostrarVentanaInterna("instaplus", ventana);
    }
 
    private void abrirAdministrarUsuarios() {
        if (traerAlFrenteSiExiste("administrar")) {
            return;
        }
 
        try {
            List<Usuario> usuarios = GestorArchivosBinarios.cargarUsuarios();
 
            JInternalFrame ventana = new JInternalFrame("Administrar usuarios", true, true, true, true);
            ventana.setSize(460, 380);
            ventana.setLayout(new BorderLayout());
 
            DefaultListModel<String> modelo = new DefaultListModel<>();
            for (Usuario u : usuarios) {
                modelo.addElement(u.getUsername() + "  -  " + (u.isActiva() ? "Activa" : "Desactivada"));
            }
            JList<String> lista = new JList<>(modelo);
            ventana.add(new JScrollPane(lista), BorderLayout.CENTER);
 
            JButton btnActivarDesactivar = new JButton("Activar / Desactivar");
            btnActivarDesactivar.addActionListener(e -> {
                int indice = lista.getSelectedIndex();
                if (indice < 0) {
                    return;
                }
                Usuario seleccionado = usuarios.get(indice);
                seleccionado.setActiva(!seleccionado.isActiva());
                try {
                    GestorArchivosBinarios.actualizarUsuario(seleccionado);
                    modelo.set(indice, seleccionado.getUsername() + "  -  "
                            + (seleccionado.isActiva() ? "Activa" : "Desactivada"));
                } catch (ArchivoCorruptoException | IOException ex) {
                    JOptionPane.showMessageDialog(ventana, "No se pudo actualizar: " + ex.getMessage());
                }
            });
 
            JButton btnEliminar = new JButton("Eliminar usuario");
            btnEliminar.addActionListener(e -> {
                int indice = lista.getSelectedIndex();
                if (indice < 0) {
                    return;
                }
                Usuario seleccionado = usuarios.get(indice);
 
                if (seleccionado.getUsername().equalsIgnoreCase(usuarioActual.getUsername())) {
                    JOptionPane.showMessageDialog(ventana,
                            "No puedes eliminar la cuenta con la que iniciaste sesión.",
                            "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                    return;
                }
 
                int confirmar = JOptionPane.showConfirmDialog(ventana,
                        "¿Eliminar al usuario '" + seleccionado.getUsername()
                        + "' y todos sus archivos? Esta acción no se puede deshacer.",
                        "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmar != JOptionPane.YES_OPTION) {
                    return;
                }
 
                try {
                    GestorArchivosBinarios.eliminarUsuario(seleccionado.getUsername());
                    usuarios.remove(indice);
                    modelo.remove(indice);
                } catch (ArchivoCorruptoException | IOException ex) {
                    JOptionPane.showMessageDialog(ventana, "No se pudo eliminar: " + ex.getMessage());
                }
            });
 
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
            panelBotones.add(btnActivarDesactivar);
            panelBotones.add(btnEliminar);
            ventana.add(panelBotones, BorderLayout.SOUTH);
 
            mostrarVentanaInterna("administrar", ventana);
 
        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la lista de usuarios.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private boolean traerAlFrenteSiExiste(String clave) {
        JInternalFrame existente = ventanasAbiertas.get(clave);
        if (existente == null || existente.isClosed()) {
            return false;
        }
        try {
            if (existente.isIcon()) {
                existente.setIcon(false);
            }
            existente.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
        return true;
    }
 
    private void mostrarVentanaInterna(String clave, JInternalFrame ventana) {
        ventanasAbiertas.put(clave, ventana);
 
        JButton botonTaskbar = new JButton(ventana.getTitle());
        botonTaskbar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        botonTaskbar.setForeground(Color.WHITE);
        botonTaskbar.setBackground(TASKBAR_BOTON_ACTIVO);
        botonTaskbar.setOpaque(true);
        botonTaskbar.setContentAreaFilled(true);
        botonTaskbar.setBorderPainted(false);
        botonTaskbar.setFocusPainted(false);
        botonTaskbar.setMargin(new Insets(4, 12, 4, 12));
        botonTaskbar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonTaskbar.addActionListener(e -> alternarVentanaTaskbar(clave));
        botonesTaskbar.put(clave, botonTaskbar);
        panelVentanasTaskbar.add(botonTaskbar);
        panelVentanasTaskbar.revalidate();
        panelVentanasTaskbar.repaint();
 
        ventana.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                ventanasAbiertas.remove(clave);
                JButton boton = botonesTaskbar.remove(clave);
                if (boton != null) {
                    panelVentanasTaskbar.remove(boton);
                    panelVentanasTaskbar.revalidate();
                    panelVentanasTaskbar.repaint();
                }
            }
 
            @Override
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {
                marcarBotonTaskbarActivo(clave);
            }
 
            @Override
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent e) {
                marcarBotonTaskbarActivo(clave);
            }
        });
        escritorio.add(ventana);
        ventana.setVisible(true);
        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
        marcarBotonTaskbarActivo(clave);
    }
}