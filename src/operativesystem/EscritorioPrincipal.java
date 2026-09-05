package operativesystem;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class EscritorioPrincipal extends JFrame {

    private final Usuario usuarioActual;
    private final JDesktopPane escritorio = new JDesktopPane() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, TemaUI.ACCENT_CLARO, 0, getHeight(), TemaUI.FONDO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    };
    private JPanel panelIconos;
    private JButton btnAvatarSuperior;
    private File archivoCopiado;
    private final Map<String, JInternalFrame> ventanasAbiertas = new HashMap<>();

    public EscritorioPrincipal(Usuario usuario) {
        super("Mini-Windows - " + usuario.getUsername() +
                (usuario.isAdministrador() ? "  [ADMINISTRADOR]" : ""));
        this.usuarioActual = usuario;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
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
        btnAvatarSuperior.setToolTipText(usuarioActual.getUsername());
        btnAvatarSuperior.addActionListener(e -> mostrarMenuPerfil(btnAvatarSuperior));

        JLabel lblNombre = new JLabel(usuarioActual.getUsername()
                + (usuarioActual.isAdministrador() ? "  ·  Admin" : "") + "  ");
        lblNombre.setForeground(TemaUI.TEXTO_SUAVE);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecho.setOpaque(false);
        panelDerecho.add(lblNombre);
        panelDerecho.add(btnAvatarSuperior);
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

    private void mostrarMenuPerfil(Component invocador) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemPerfil = new JMenuItem("Ver perfil");
        itemPerfil.addActionListener(e -> abrirPerfil());
        JMenuItem itemCerrar = new JMenuItem("Cerrar sesión");
        itemCerrar.addActionListener(e -> cerrarSesion());
        menu.add(itemPerfil);
        menu.addSeparator();
        menu.add(itemCerrar);
        menu.show(invocador, 0, invocador.getHeight());
    }

    private void abrirPerfil() {
        if (traerAlFrenteSiExiste("perfil")) return;

        JInternalFrame ventana = new JInternalFrame("Mi perfil", true, true, true, true);
        ventana.setSize(340, 440);
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

        ventana.add(lblFoto, BorderLayout.NORTH);
        ventana.add(panelInfo, BorderLayout.CENTER);
        ventana.add(btnCambiarFoto, BorderLayout.SOUTH);

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

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Cerrar la sesión actual?", "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new PantallaLogin().setVisible(true));
        }
    }


    private File obtenerRaizDeTrabajo() {
        if (usuarioActual.isAdministrador()) {
            try {
                List<Usuario> todos = GestorArchivosBinarios.cargarUsuarios();
                String[] usernames = todos.stream().map(Usuario::getUsername).toArray(String[]::new);
                String elegido = (String) JOptionPane.showInputDialog(this,
                        "¿Qué carpeta de usuario deseas explorar?",
                        "Selección de usuario (modo administrador)",
                        JOptionPane.QUESTION_MESSAGE, null, usernames, usuarioActual.getUsername());
                if (elegido == null) {
                    elegido = usuarioActual.getUsername();
                }
                return new File(GestorArchivosBinarios.rutaCarpetaUsuario(elegido));
            } catch (ArchivoCorruptoException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer la lista de usuarios.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return new File(GestorArchivosBinarios.rutaCarpetaUsuario(usuarioActual.getUsername()));
    }


    private void abrirExplorador() {
        if (traerAlFrenteSiExiste("explorador")) return;

        File raiz = obtenerRaizDeTrabajo();
        if (!raiz.exists()) raiz.mkdirs();

        JInternalFrame ventana = new JInternalFrame(
                "Explorador - " + raiz.getName(), true, true, true, true);
        ventana.setSize(420, 420);

        DefaultMutableTreeNode nodoRaiz = construirNodo(raiz, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        JTree arbol = new JTree(nodoRaiz);
        arbol.setRootVisible(true);
        JScrollPane scroll = new JScrollPane(arbol);

        JPanel panelBotones = new JPanel();
        JButton btnOrganizar = new JButton("Organizar carpeta seleccionada");
        btnOrganizar.addActionListener(e -> {
            File seleccion = obtenerArchivoSeleccionado(arbol);
            if (seleccion == null || !seleccion.isDirectory()) {
                JOptionPane.showMessageDialog(ventana, "Selecciona una carpeta para organizar.");
                return;
            }
            organizarCarpetaSeleccionada(arbol, ventana, raiz, seleccion);
        });
        JButton btnCrear = new JButton("Nueva carpeta");
        btnCrear.addActionListener(e -> crearCarpeta(arbol, ventana, raiz));
        JButton btnRenombrar = new JButton("Renombrar");
        btnRenombrar.addActionListener(e -> renombrarArchivo(arbol, ventana, raiz));
        JButton btnCopiar = new JButton("Copiar");
        btnCopiar.addActionListener(e -> copiarArchivo(arbol, ventana));
        JButton btnPegar = new JButton("Pegar");
        btnPegar.addActionListener(e -> pegarArchivo(arbol, ventana, raiz));
        JComboBox<String> cmbOrden = new JComboBox<>(new String[]{"Nombre", "Fecha", "Tipo", "Tamaño"});
        cmbOrden.addActionListener(e -> actualizarArbol(arbol, raiz, (String) cmbOrden.getSelectedItem()));
        panelBotones.add(btnOrganizar);
        panelBotones.add(btnCrear);
        panelBotones.add(btnRenombrar);
        panelBotones.add(btnCopiar);
        panelBotones.add(btnPegar);
        panelBotones.add(new JLabel("Ordenar: "));
        panelBotones.add(cmbOrden);

        ventana.setLayout(new BorderLayout());
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        mostrarVentanaInterna("explorador", ventana);
    }

    private DefaultMutableTreeNode construirNodo(File archivo, Comparator<File> comparador) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo);
        File[] hijos = archivo.listFiles();
        if (hijos != null) {
            Arrays.sort(hijos, comparador);
            for (File hijo : hijos) {
                nodo.add(construirNodo(hijo, comparador));
            }
        }
        return nodo;
    }

    private File obtenerArchivoSeleccionado(JTree arbol) {
        TreePath ruta = arbol.getSelectionPath();
        if (ruta == null) return null;
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
        arbol.setModel(new DefaultTreeModel(construirNodo(raiz, comparadorPara(orden))));
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
        if (nombre == null) return null;
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
        if (nombre == null) return;
        File nuevaCarpeta = new File(destino, nombre);
        if (!estaDentroDeRaiz(nuevaCarpeta, raiz) || !nuevaCarpeta.mkdir()) {
            JOptionPane.showMessageDialog(ventana, "No se pudo crear la carpeta. Verifica que no exista.", "Explorador", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarArbol(arbol, raiz, "Nombre");
    }

    private void renombrarArchivo(JTree arbol, JInternalFrame ventana, File raiz) {
        File seleccion = obtenerArchivoSeleccionado(arbol);
        if (seleccion == null || seleccion.equals(raiz)) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un archivo o carpeta que desees renombrar.");
            return;
        }
        String nombre = solicitarNombreSeguro(ventana, "Nuevo nombre:", seleccion.getName());
        if (nombre == null) return;
        File destino = new File(seleccion.getParentFile(), nombre);
        try {
            if (!estaDentroDeRaiz(destino, raiz) || destino.exists()) throw new IOException("El destino ya existe.");
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
    }

    private void pegarArchivo(JTree arbol, JInternalFrame ventana, File raiz) {
        if (archivoCopiado == null || !archivoCopiado.exists()) {
            JOptionPane.showMessageDialog(ventana, "Primero copia un archivo o carpeta existente.");
            return;
        }
        File seleccion = obtenerArchivoSeleccionado(arbol);
        File destinoCarpeta = seleccion != null && seleccion.isDirectory() ? seleccion
                : (seleccion != null ? seleccion.getParentFile() : raiz);
        File destino = nombreDisponible(destinoCarpeta, archivoCopiado.getName());
        try {
            copiarRecursivamente(archivoCopiado, destino);
            actualizarArbol(arbol, raiz, "Nombre");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventana, "No se pudo pegar: " + ex.getMessage(), "Explorador", JOptionPane.ERROR_MESSAGE);
        }
    }

    private File nombreDisponible(File carpeta, String nombre) {
        File candidato = new File(carpeta, nombre);
        if (!candidato.exists()) return candidato;
        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        String extension = punto > 0 ? nombre.substring(punto) : "";
        int contador = 1;
        do {
            candidato = new File(carpeta, base + " (copia " + contador++ + ")" + extension);
        } while (candidato.exists());
        return candidato;
    }

    private void copiarRecursivamente(File origen, File destino) throws IOException {
        if (origen.isDirectory()) {
            if (!destino.mkdirs() && !destino.isDirectory()) throw new IOException("No se pudo crear " + destino.getName());
            File[] hijos = origen.listFiles();
            if (hijos != null) for (File hijo : hijos) copiarRecursivamente(hijo, new File(destino, hijo.getName()));
        } else {
            Files.copy(origen.toPath(), destino.toPath());
        }
    }

    private void organizarCarpetaSeleccionada(JTree arbol, JInternalFrame ventana, File raiz, File carpetaSeleccionada) {
        Thread hiloOrganizador = new Thread(() -> {
            File[] archivos = carpetaSeleccionada.listFiles(File::isFile);
            if (archivos != null) {
                for (File archivo : archivos) {
                    String nombre = archivo.getName().toLowerCase();
                    String categoria;
                    if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
                        categoria = "imagenes";
                    } else if (nombre.endsWith(".mp3") || nombre.endsWith(".wav")) {
                        categoria = "musica";
                    } else if (nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".docx")) {
                        categoria = "documentos";
                    } else {
                        continue;
                    }
                    File carpetaDestino = new File(carpetaSeleccionada, categoria);
                    carpetaDestino.mkdirs();
                    try {
                        Files.move(archivo.toPath(), nombreDisponible(carpetaDestino, archivo.getName()).toPath());
                    } catch (IOException ignored) {
                        
                    }
                }
            }

            SwingUtilities.invokeLater(() -> {
                actualizarArbol(arbol, raiz, "Nombre");
                JOptionPane.showMessageDialog(ventana, "Carpeta organizada.");
            });
        }, "organizador-archivos");
        hiloOrganizador.start();
    }


    private void abrirEditorTexto() {
        if (traerAlFrenteSiExiste("editor")) return;

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
        if (traerAlFrenteSiExiste("visor")) return;

        JInternalFrame ventana = new JInternalFrame("Visor de imágenes", true, true, true, true);
        ventana.setSize(450, 420);
        ventana.setLayout(new BorderLayout());

        JLabel lblImagen = new JLabel("Selecciona una carpeta con imágenes", SwingConstants.CENTER);
        JScrollPane scroll = new JScrollPane(lblImagen);

        List<File> imagenes = new ArrayList<>();
        int[] indiceActual = {-1};

        JButton btnCarpeta = new JButton("Elegir carpeta");
        JButton btnAnterior = new JButton("Anterior");
        JButton btnSiguiente = new JButton("Siguiente");

        btnCarpeta.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(obtenerRaizDeTrabajo());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                imagenes.clear();
                File[] archivos = chooser.getSelectedFile().listFiles((dir, nombre) -> {
                    String n = nombre.toLowerCase();
                    return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
                });
                if (archivos != null) imagenes.addAll(Arrays.asList(archivos));
                indiceActual[0] = imagenes.isEmpty() ? -1 : 0;
                mostrarImagenActual(lblImagen, imagenes, indiceActual[0]);
            }
        });

        btnAnterior.addActionListener(e -> {
            if (!imagenes.isEmpty()) {
                indiceActual[0] = (indiceActual[0] - 1 + imagenes.size()) % imagenes.size();
                mostrarImagenActual(lblImagen, imagenes, indiceActual[0]);
            }
        });

        btnSiguiente.addActionListener(e -> {
            if (!imagenes.isEmpty()) {
                indiceActual[0] = (indiceActual[0] + 1) % imagenes.size();
                mostrarImagenActual(lblImagen, imagenes, indiceActual[0]);
            }
        });

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnCarpeta);
        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);

        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        mostrarVentanaInterna("visor", ventana);
    }

    private void mostrarImagenActual(JLabel lblImagen, List<File> imagenes, int indice) {
        if (indice < 0 || indice >= imagenes.size()) {
            lblImagen.setIcon(null);
            lblImagen.setText("No hay imágenes en esta carpeta");
            return;
        }
        ImageIcon icono = new ImageIcon(imagenes.get(indice).getAbsolutePath());
        Image escalada = icono.getImage().getScaledInstance(380, 300, Image.SCALE_SMOOTH);
        lblImagen.setIcon(new ImageIcon(escalada));
        lblImagen.setText(null);
    }


    // CONSOLA DE COMANDOS 

    private void abrirConsola() {
        if (traerAlFrenteSiExiste("consola")) return;

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
        areaSalida.append(carpetaActual[0].getAbsolutePath() + ">\n");

        campoComando.addActionListener((ActionEvent e) -> {
            String comando = campoComando.getText().trim();
            areaSalida.append(carpetaActual[0].getAbsolutePath() + "> " + comando + "\n");
            String resultado = procesarComandoConsola(comando, carpetaActual, raizPermitida);
            if (resultado != null) areaSalida.append(resultado + "\n");
            campoComando.setText("");
            areaSalida.setCaretPosition(areaSalida.getDocument().getLength());
        });

        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(campoComando, BorderLayout.SOUTH);

        mostrarVentanaInterna("consola", ventana);
    }

    private String procesarComandoConsola(String comando, File[] carpetaActual, File raizPermitida) {
        if (comando.isEmpty()) return null;

        String[] partes = comando.split("\\s+", 2);
        String instruccion = partes[0].toLowerCase();
        String argumento = partes.length > 1 ? partes[1] : "";

        switch (instruccion) {
            case "mkdir":
                if (argumento.isEmpty()) return "Uso: mkdir <nombre>";
                if (!esNombreSeguro(argumento)) return "Nombre de carpeta no válido.";
                boolean creado = new File(carpetaActual[0], argumento).mkdir();
                return creado ? "Carpeta creada." : "No se pudo crear la carpeta.";

            case "rm":
                if (argumento.isEmpty()) return "Uso: rm <nombre>";
                File aEliminar = new File(carpetaActual[0], argumento);
                if (!esNombreSeguro(argumento) || !estaDentroDeRaiz(aEliminar, raizPermitida)) {
                    return "No puedes eliminar fuera de tu carpeta de usuario.";
                }
                boolean eliminado = aEliminar.isDirectory() && aEliminar.delete();
                return eliminado ? "Eliminado." : "No se pudo eliminar (¿existe y está vacío?).";

            case "cd":
                if (argumento.isEmpty()) return "Uso: cd <carpeta>";
                File nuevaCarpeta = new File(carpetaActual[0], argumento);
                if (nuevaCarpeta.isDirectory() && estaDentroDeRaiz(nuevaCarpeta, raizPermitida)) {
                    carpetaActual[0] = nuevaCarpeta;
                    return null;
                }
                return "La carpeta no existe o está fuera de tu espacio de trabajo.";

            case "cd..":
                File padre = carpetaActual[0].getParentFile();
                if (padre != null && estaDentroDeRaiz(padre, raizPermitida)) carpetaActual[0] = padre;
                return null;

            case "dir":
                File[] contenido = carpetaActual[0].listFiles();
                if (contenido == null || contenido.length == 0) return "(carpeta vacía)";
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
                return "Comando no reconocido: " + instruccion;
        }
    }

    private boolean esNombreSeguro(String nombre) {
        return !nombre.isBlank() && !nombre.equals(".") && !nombre.equals("..")
                && new File(nombre).getName().equals(nombre);
    }


    // REPRODUCTOR DE MÚSICA (pendiente)

    private void abrirReproductor() {
        if (traerAlFrenteSiExiste("reproductor")) return;

        JInternalFrame ventana = new JInternalFrame("Reproductor", true, true, true, true);
        ventana.setSize(360, 260);
        ventana.setLayout(new BorderLayout());

        JLabel lblInfo = new JLabel(
                "<html><center>Módulo pendiente de implementación.<br><br>" +
                        "Aquí debe correr en su propio hilo la reproducción<br>" +
                        "de archivos .mp3 obtenidos del explorador, con<br>" +
                        "controles de Play / Pause / Stop, lista de canciones,<br>" +
                        "carátula y descripción.</center></html>",
                SwingConstants.CENTER);

        JPanel controles = new JPanel();
        controles.add(new JButton("Play"));
        controles.add(new JButton("Pause"));
        controles.add(new JButton("Stop"));

        ventana.add(lblInfo, BorderLayout.CENTER);
        ventana.add(controles, BorderLayout.SOUTH);

        mostrarVentanaInterna("reproductor", ventana);
    }


    // INSTA+ (pendiente)

    private void abrirInstaPlus() {
        if (traerAlFrenteSiExiste("instaplus")) return;

        JInternalFrame ventana = new JInternalFrame("INSTA+", true, true, true, true);
        ventana.setSize(420, 320);
        ventana.setLayout(new BorderLayout());

        JLabel lblInfo = new JLabel(
                "<html><center>Módulo INSTA+ pendiente de implementación.<br><br>" +
                        "Debe integrarse en una sola vista con: Perfil, Cargar<br>" +
                        "imágenes, Timeline, Interacciones, Buscar Profile,<br>" +
                        "Buscar Hashtag, Inbox, Editar perfil y Cerrar sesión.</center></html>",
                SwingConstants.CENTER);

        ventana.add(lblInfo, BorderLayout.CENTER);
        mostrarVentanaInterna("instaplus", ventana);
    }

   
    private void abrirAdministrarUsuarios() {
        if (traerAlFrenteSiExiste("administrar")) return;

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
                if (indice < 0) return;
                Usuario seleccionado = usuarios.get(indice);
                seleccionado.setActiva(!seleccionado.isActiva());
                try {
                    GestorArchivosBinarios.actualizarUsuario(seleccionado);
                    modelo.set(indice, seleccionado.getUsername() + "  -  " +
                            (seleccionado.isActiva() ? "Activa" : "Desactivada"));
                } catch (ArchivoCorruptoException | IOException ex) {
                    JOptionPane.showMessageDialog(ventana, "No se pudo actualizar: " + ex.getMessage());
                }
            });

            JButton btnEliminar = new JButton("Eliminar usuario");
            btnEliminar.addActionListener(e -> {
                int indice = lista.getSelectedIndex();
                if (indice < 0) return;
                Usuario seleccionado = usuarios.get(indice);

                if (seleccionado.getUsername().equalsIgnoreCase(usuarioActual.getUsername())) {
                    JOptionPane.showMessageDialog(ventana,
                            "No puedes eliminar la cuenta con la que iniciaste sesión.",
                            "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirmar = JOptionPane.showConfirmDialog(ventana,
                        "¿Eliminar al usuario '" + seleccionado.getUsername() +
                                "' y todos sus archivos? Esta acción no se puede deshacer.",
                        "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmar != JOptionPane.YES_OPTION) return;

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
        ventana.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                ventanasAbiertas.remove(clave);
            }
        });
        escritorio.add(ventana);
        ventana.setVisible(true);
        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }
}