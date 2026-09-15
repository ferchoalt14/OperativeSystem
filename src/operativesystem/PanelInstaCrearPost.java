package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Crear publicación. Todo pasa dentro del panel: el selector de imágenes está incrustado (no abre
 * ventana aparte) y los avisos se muestran en línea. La publicación se envía por socket.
 */
public class PanelInstaCrearPost extends JPanel {

    private static final String VISTA_FORMULARIO = "FORMULARIO";
    private static final String VISTA_SELECTOR = "SELECTOR";

    private final InstaControlador controlador;
    private final CardLayout cartas = new CardLayout();

    private JLabel lblVistaPrevia;
    private JTextArea txtTexto;
    private JLabel lblContador;
    private JButton btnPublicar;
    private JButton btnQuitarImagen;
    private final MensajeEnLinea mensaje = new MensajeEnLinea();
    private File archivoSeleccionado;

    private JPanel panelSelector;
    private JFileChooser selector;
    private final MensajeEnLinea mensajeSelector = new MensajeEnLinea();

    public PanelInstaCrearPost(InstaControlador controlador) {
        this.controlador = controlador;
        setLayout(cartas);
        setOpaque(false);
        add(construirFormulario(), VISTA_FORMULARIO);
        panelSelector = new JPanel(new BorderLayout(0, 10));
        panelSelector.setOpaque(false);
        add(panelSelector, VISTA_SELECTOR);
        cartas.show(this, VISTA_FORMULARIO);
    }

    private JComponent construirFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        formulario.setBorder(new EmptyBorder(24, 40, 24, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblTitulo = EstiloInsta.etiqueta("Nueva publicación", 20, true, TemaUI.ACCENT_OSCURO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        formulario.add(lblTitulo, gbc);

        lblVistaPrevia = new JLabel("<html><center><font size='6'>🖼</font><br>Sin imagen seleccionada</center></html>",
                SwingConstants.CENTER);
        lblVistaPrevia.setPreferredSize(new Dimension(340, 220));
        lblVistaPrevia.setOpaque(true);
        lblVistaPrevia.setBackground(EstiloInsta.mezclar(TemaUI.FONDO, TemaUI.TEXTO, 0.05));
        lblVistaPrevia.setForeground(TemaUI.TEXTO_SUAVE);
        lblVistaPrevia.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        gbc.gridy = 1;
        formulario.add(lblVistaPrevia, gbc);

        JButton btnElegirImagen = EstiloInsta.botonSecundario("📁  Elegir imagen (opcional)");
        btnElegirImagen.addActionListener(e -> abrirSelector());
        btnQuitarImagen = EstiloInsta.botonTexto("Quitar imagen", EstiloInsta.ROJO, 12, false);
        btnQuitarImagen.setVisible(false);
        btnQuitarImagen.addActionListener(e -> quitarImagen());
        JPanel filaImagen = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        filaImagen.setOpaque(false);
        filaImagen.add(btnElegirImagen);
        filaImagen.add(btnQuitarImagen);
        gbc.gridy = 2;
        formulario.add(filaImagen, gbc);

        txtTexto = new JTextArea(8, 30);
        txtTexto.setLineWrap(true);
        txtTexto.setWrapStyleWord(true);
        txtTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtTexto.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtTexto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
        });
        JScrollPane scrollTexto = new JScrollPane(txtTexto);
        scrollTexto.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        scrollTexto.setPreferredSize(new Dimension(420, 170));
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formulario.add(scrollTexto, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        lblContador = EstiloInsta.etiqueta("", 11, false, TemaUI.TEXTO_SUAVE);
        JPanel barraTexto = new JPanel(new BorderLayout());
        barraTexto.setOpaque(false);
        barraTexto.add(lblContador, BorderLayout.CENTER);
        barraTexto.add(SelectorEmojis.crearBoton(txtTexto), BorderLayout.EAST);
        gbc.gridy = 4;
        formulario.add(barraTexto, gbc);

        gbc.gridy = 5;
        formulario.add(mensaje, gbc);

        btnPublicar = TemaUI.crearBotonPrimario("Publicar");
        btnPublicar.addActionListener(e -> publicar());
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        formulario.add(btnPublicar, gbc);

        JButton btnLimpiar = EstiloInsta.botonSecundario("Limpiar");
        btnLimpiar.addActionListener(e -> {
            limpiarFormulario();
            mensaje.ocultar();
        });
        gbc.gridx = 1;
        formulario.add(btnLimpiar, gbc);

        actualizarContador();

        JScrollPane scroll = new JScrollPane(formulario);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ------------------------------------------------------------------ Selector incrustado

    private void abrirSelector() {
        if (selector == null) {
            construirSelector();
        }
        mensajeSelector.ocultar();
        selector.rescanCurrentDirectory();
        cartas.show(this, VISTA_SELECTOR);
    }

    private void construirSelector() {
        selector = new JFileChooser();
        selector.setAcceptAllFileFilterUsed(false);
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes (.png, .jpg, .jpeg, .gif)",
                "png", "jpg", "jpeg", "gif"));
        selector.setApproveButtonText("Usar imagen");
        selector.setControlButtonsAreShown(true);
        selector.addActionListener(e -> {
            if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                usarImagen(selector.getSelectedFile());
            } else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
                cartas.show(this, VISTA_FORMULARIO);
            }
        });

        JButton btnVolver = EstiloInsta.botonTexto("← Volver", TemaUI.ACCENT_OSCURO, 13, true);
        btnVolver.addActionListener(e -> cartas.show(this, VISTA_FORMULARIO));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(btnVolver, BorderLayout.WEST);
        JLabel titulo = EstiloInsta.etiqueta("Elige una imagen para tu publicación", 16, true, TemaUI.TEXTO);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        cabecera.add(titulo, BorderLayout.CENTER);

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(cabecera, BorderLayout.NORTH);
        superior.add(mensajeSelector, BorderLayout.CENTER);

        panelSelector.setBorder(new EmptyBorder(18, 22, 18, 22));
        panelSelector.add(superior, BorderLayout.NORTH);
        panelSelector.add(selector, BorderLayout.CENTER);
        panelSelector.revalidate();
    }

    private void usarImagen(File archivo) {
        if (archivo == null || !archivo.isFile()) {
            mensajeSelector.error("Selecciona un archivo de imagen válido.");
            return;
        }
        ImageIcon icono = EstiloInsta.imagenAjustada(archivo.getAbsolutePath(), 340, 220);
        if (icono == null) {
            mensajeSelector.error("No se pudo leer esa imagen. Prueba con un archivo PNG o JPG.");
            return;
        }
        archivoSeleccionado = archivo;
        lblVistaPrevia.setText("");
        lblVistaPrevia.setIcon(icono);
        btnQuitarImagen.setVisible(true);
        mensaje.ocultar();
        cartas.show(this, VISTA_FORMULARIO);
    }

    private void quitarImagen() {
        archivoSeleccionado = null;
        lblVistaPrevia.setIcon(null);
        lblVistaPrevia.setText("<html><center><font size='6'>🖼</font><br>Sin imagen seleccionada</center></html>");
        btnQuitarImagen.setVisible(false);
    }

    // ------------------------------------------------------------------ Publicar

    private void actualizarContador() {
        int longitud = txtTexto.getText().length();
        lblContador.setText(longitud + " / " + Post.MAX_CARACTERES + " caracteres   ·   usa # para hashtags y @ para mencionar");
        lblContador.setForeground(longitud > Post.MAX_CARACTERES ? EstiloInsta.ROJO : TemaUI.TEXTO_SUAVE);
    }

    private void publicar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        String texto = txtTexto.getText().trim();
        if (texto.isEmpty() && archivoSeleccionado == null) {
            mensaje.error("Agrega una imagen o un texto para poder publicar.");
            return;
        }
        if (texto.length() > Post.MAX_CARACTERES) {
            mensaje.error("El texto supera el máximo de " + Post.MAX_CARACTERES + " caracteres.");
            return;
        }

        String ruta = archivoSeleccionado != null ? archivoSeleccionado.getAbsolutePath() : "";
        btnPublicar.setEnabled(false);
        mensaje.info("Publicando…");
        controlador.enviarAlServidor(PaqueteInsta.crearPost(texto, ruta), r -> {
            btnPublicar.setEnabled(true);
            if (!r.esOk()) {
                mensaje.error("No se pudo guardar la publicación: " + r.getError());
                return;
            }
            mensaje.ocultar();
            limpiarFormulario();
            controlador.refrescarPerfilPropio();
            controlador.refrescarFeed();
            controlador.mostrarSeccion("FEED");
            controlador.mostrarAviso("¡Publicación creada!", "Tus seguidores ya pueden verla.");
        });
    }

    private void limpiarFormulario() {
        txtTexto.setText("");
        quitarImagen();
        actualizarContador();
    }
}