
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
    private final JDesktopPane escritorio = new JDesktopPane();
    private File archivoCopiado;

    public EscritorioPrincipal(Usuario usuario) {
        super("Mini-Windows - " + usuario.getUsername() +
                (usuario.isAdministrador() ? "  [ADMINISTRADOR]" : ""));
        this.usuarioActual = usuario;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        escritorio.setBackground(new Color(0, 90, 150));
        add(escritorio, BorderLayout.CENTER);
        add(construirBarraHerramientas(), BorderLayout.NORTH);
    }


    private JToolBar construirBarraHerramientas() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);

        agregarBoton(barra, "Explorador", e -> abrirExplorador());
        agregarBoton(barra, "Editor de texto", e -> abrirEditorTexto());
        agregarBoton(barra, "Visor de imágenes", e -> abrirVisorImagenes());
        agregarBoton(barra, "Consola", e -> abrirConsola());
        agregarBoton(barra, "Reproductor", e -> abrirReproductor());
        barra.addSeparator();
        agregarBoton(barra, "INSTA+", e -> abrirInstaPlus());

        if (usuarioActual.isAdministrador()) {
            barra.addSeparator();
            agregarBoton(barra, "Administrar usuarios", e -> abrirAdministrarUsuarios());
        }

        barra.add(Box.createHorizontalGlue());
        JLabel lblUsuario = new JLabel("  " + usuarioActual.getUsername() + "  ");
        barra.add(lblUsuario);
        agregarBoton(barra, "Cerrar sesión", e -> cerrarSesion());

        return barra;
    }

    private void agregarBoton(JToolBar barra, String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        barra.add(boton);
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

        mostrarVentanaInterna(ventana);
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
                        // El resto de archivos continúa organizándose aunque uno falle.
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

        mostrarVentanaInterna(ventana);
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

        mostrarVentanaInterna(ventana);
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

    // ---------------------------------------------------------------
    // CONSOLA DE COMANDOS (imita el CMD de Windows)
    // ---------------------------------------------------------------
    private void abrirConsola() {
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

        mostrarVentanaInterna(ventana);
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

    // ---------------------------------------------------------------
    // REPRODUCTOR DE MÚSICA (pendiente - punto de partida para el equipo)
    // ---------------------------------------------------------------
    private void abrirReproductor() {
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

        mostrarVentanaInterna(ventana);
    }

    // ---------------------------------------------------------------
    // INSTA+ (pendiente - debe vivir en una sola pantalla integrada)
    // ---------------------------------------------------------------
    private void abrirInstaPlus() {
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
        mostrarVentanaInterna(ventana);
    }

   
    private void abrirAdministrarUsuarios() {
        try {
            List<Usuario> usuarios = GestorArchivosBinarios.cargarUsuarios();

            JInternalFrame ventana = new JInternalFrame("Administrar usuarios", true, true, true, true);
            ventana.setSize(450, 350);
            ventana.setLayout(new BorderLayout());

            DefaultListModel<String> modelo = new DefaultListModel<>();
            for (Usuario u : usuarios) {
                modelo.addElement(u.getUsername() + "  -  " + (u.isActiva() ? "Activa" : "Desactivada"));
            }
            JList<String> lista = new JList<>(modelo);
            ventana.add(new JScrollPane(lista), BorderLayout.CENTER);

            JButton btnActivarDesactivar = new JButton("Activar / Desactivar seleccionado");
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
            ventana.add(btnActivarDesactivar, BorderLayout.SOUTH);

            mostrarVentanaInterna(ventana);

        } catch (ArchivoCorruptoException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la lista de usuarios.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void mostrarVentanaInterna(JInternalFrame ventana) {
        escritorio.add(ventana);
        ventana.setVisible(true);
        try {
            ventana.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }
}
