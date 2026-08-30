/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operativesystem;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class EscritorioPrincipal extends JFrame {

    private final Usuario usuarioActual;
    private final JDesktopPane escritorio = new JDesktopPane();

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

        DefaultMutableTreeNode nodoRaiz = construirNodo(raiz);
        JTree arbol = new JTree(nodoRaiz);
        JScrollPane scroll = new JScrollPane(arbol);

        JPanel panelBotones = new JPanel();
        JButton btnOrganizar = new JButton("Organizar carpeta seleccionada");
        btnOrganizar.addActionListener(e -> organizarCarpetaSeleccionada(arbol, ventana, raiz));
        panelBotones.add(btnOrganizar);

        ventana.setLayout(new BorderLayout());
        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        mostrarVentanaInterna(ventana);
    }

    private DefaultMutableTreeNode construirNodo(File archivo) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo.getName().isEmpty()
                ? archivo.getPath() : archivo.getName());
        File[] hijos = archivo.listFiles();
        if (hijos != null) {
            Arrays.sort(hijos, Comparator.comparing(File::getName));
            for (File hijo : hijos) {
                if (hijo.isDirectory()) {
                    nodo.add(construirNodo(hijo));
                } else {
                    nodo.add(new DefaultMutableTreeNode(hijo.getName()));
                }
            }
        }
        return nodo;
    }

   
    private void organizarCarpetaSeleccionada(JTree arbol, JInternalFrame ventana, File raiz) {
        Thread hiloOrganizador = new Thread(() -> {
            File[] archivos = raiz.listFiles(File::isFile);
            if (archivos != null) {
                File carpetaImagenes = new File(raiz, "imagenes");
                File carpetaDocumentos = new File(raiz, "documentos");
                File carpetaMusica = new File(raiz, "musica");
                carpetaImagenes.mkdirs();
                carpetaDocumentos.mkdirs();
                carpetaMusica.mkdirs();

                for (File archivo : archivos) {
                    String nombre = archivo.getName().toLowerCase();
                    File destino;
                    if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
                        destino = new File(carpetaImagenes, archivo.getName());
                    } else if (nombre.endsWith(".mp3") || nombre.endsWith(".wav")) {
                        destino = new File(carpetaMusica, archivo.getName());
                    } else if (nombre.endsWith(".txt") || nombre.endsWith(".pdf") || nombre.endsWith(".docx")) {
                        destino = new File(carpetaDocumentos, archivo.getName());
                    } else {
                        continue;
                    }
                    archivo.renameTo(destino);
                }
            }

            SwingUtilities.invokeLater(() -> {
                arbol.setModel(new DefaultTreeModel(construirNodo(raiz)));
                JOptionPane.showMessageDialog(ventana, "Carpeta organizada.");
            });
        });
        hiloOrganizador.start();
    }


    private void abrirEditorTexto() {
        JInternalFrame ventana = new JInternalFrame("Editor de texto", true, true, true, true);
        ventana.setSize(500, 400);
        ventana.setLayout(new BorderLayout());

        JTextPane areaTexto = new JTextPane();
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
            try (BufferedReader lector = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                StringBuilder contenido = new StringBuilder();
                String linea;
                while ((linea = lector.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }
                areaTexto.setText(contenido.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo abrir el archivo: " + ex.getMessage());
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
            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(destino))) {
                escritor.write(areaTexto.getText());
                JOptionPane.showMessageDialog(padre, "Archivo guardado correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo guardar el archivo: " + ex.getMessage());
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

        File[] carpetaActual = {obtenerRaizDeTrabajo()};

        JTextField campoComando = new JTextField();
        areaSalida.append(carpetaActual[0].getAbsolutePath() + ">\n");

        campoComando.addActionListener((ActionEvent e) -> {
            String comando = campoComando.getText().trim();
            areaSalida.append(carpetaActual[0].getAbsolutePath() + "> " + comando + "\n");
            String resultado = procesarComandoConsola(comando, carpetaActual);
            if (resultado != null) areaSalida.append(resultado + "\n");
            campoComando.setText("");
            areaSalida.setCaretPosition(areaSalida.getDocument().getLength());
        });

        ventana.add(scroll, BorderLayout.CENTER);
        ventana.add(campoComando, BorderLayout.SOUTH);

        mostrarVentanaInterna(ventana);
    }

    private String procesarComandoConsola(String comando, File[] carpetaActual) {
        if (comando.isEmpty()) return null;

        String[] partes = comando.split("\\s+", 2);
        String instruccion = partes[0].toLowerCase();
        String argumento = partes.length > 1 ? partes[1] : "";

        switch (instruccion) {
            case "mkdir":
                if (argumento.isEmpty()) return "Uso: mkdir <nombre>";
                boolean creado = new File(carpetaActual[0], argumento).mkdir();
                return creado ? "Carpeta creada." : "No se pudo crear la carpeta.";

            case "rm":
                if (argumento.isEmpty()) return "Uso: rm <nombre>";
                File aEliminar = new File(carpetaActual[0], argumento);
                boolean eliminado = aEliminar.isDirectory() ? aEliminar.delete() : aEliminar.delete();
                return eliminado ? "Eliminado." : "No se pudo eliminar (¿existe y está vacío?).";

            case "cd":
                if (argumento.isEmpty()) return "Uso: cd <carpeta>";
                File nuevaCarpeta = new File(carpetaActual[0], argumento);
                if (nuevaCarpeta.isDirectory()) {
                    carpetaActual[0] = nuevaCarpeta;
                    return null;
                }
                return "La carpeta no existe.";

            case "cd..":
                File padre = carpetaActual[0].getParentFile();
                if (padre != null) carpetaActual[0] = padre;
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
