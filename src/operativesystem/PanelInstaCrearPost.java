package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;


public class PanelInstaCrearPost extends JPanel {

    private final InstaControlador controlador;
    private JLabel lblVistaPrevia;
    private JTextArea txtTexto;
    private JLabel lblContador;
    private File archivoSeleccionado;

    public PanelInstaCrearPost(InstaControlador controlador) {
        super(new GridBagLayout());
        this.controlador = controlador;
        setOpaque(false);
        setBorder(new EmptyBorder(30, 40, 30, 40));
        construirUI();
    }

    private void construirUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Nueva publicación", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(TemaUI.ACCENT_OSCURO);
        add(lblTitulo, gbc);

        lblVistaPrevia = new JLabel("Sin imagen seleccionada", SwingConstants.CENTER);
        lblVistaPrevia.setPreferredSize(new Dimension(320, 220));
        lblVistaPrevia.setOpaque(true);
        lblVistaPrevia.setBackground(TemaUI.FONDO);
        lblVistaPrevia.setForeground(TemaUI.TEXTO_SUAVE);
        gbc.gridy = 1;
        add(lblVistaPrevia, gbc);

        JButton btnElegirImagen = new JButton("Elegir imagen (opcional)");
        btnElegirImagen.addActionListener(e -> elegirImagen());
        gbc.gridy = 2;
        add(btnElegirImagen, gbc);

        txtTexto = new JTextArea(4, 24);
        txtTexto.setLineWrap(true);
        txtTexto.setWrapStyleWord(true);
        txtTexto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
        });
        JScrollPane scrollTexto = new JScrollPane(txtTexto);
        scrollTexto.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));
        gbc.gridy = 3;
        add(scrollTexto, gbc);

        JButton btnEmoji = SelectorEmojis.crearBoton(txtTexto);
        JPanel panelBarraTexto = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBarraTexto.setOpaque(false);
        panelBarraTexto.add(btnEmoji);
        gbc.gridy = 4;
        add(panelBarraTexto, gbc);

        lblContador = new JLabel("0 / " + Post.MAX_CARACTERES + " caracteres   ·   usa # para hashtags y @ para mencionar");
        lblContador.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblContador.setForeground(TemaUI.TEXTO_SUAVE);
        gbc.gridy = 5;
        add(lblContador, gbc);

        JButton btnPublicar = TemaUI.crearBotonPrimario("Publicar");
        btnPublicar.addActionListener(e -> publicar());
        gbc.gridy = 6; gbc.gridwidth = 1; gbc.gridx = 0;
        add(btnPublicar, gbc);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        gbc.gridx = 1;
        add(btnLimpiar, gbc);
    }

    private void actualizarContador() {
        int longitud = txtTexto.getText().length();
        lblContador.setText(longitud + " / " + Post.MAX_CARACTERES + " caracteres   ·   usa # para hashtags y @ para mencionar");
        lblContador.setForeground(longitud > Post.MAX_CARACTERES ? new Color(190, 40, 40) : TemaUI.TEXTO_SUAVE);
    }

    private void elegirImagen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (.png, .jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            ImageIcon icono = new ImageIcon(new ImageIcon(archivoSeleccionado.getAbsolutePath())
                    .getImage().getScaledInstance(320, 220, Image.SCALE_SMOOTH));
            lblVistaPrevia.setText("");
            lblVistaPrevia.setIcon(icono);
        }
    }

    private void publicar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        String texto = txtTexto.getText().trim();
        if (texto.isEmpty() && archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Agrega una imagen o un texto para poder publicar.",
                    "Publicación vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (texto.length() > Post.MAX_CARACTERES) {
            JOptionPane.showMessageDialog(this,
                    "El texto supera el máximo de " + Post.MAX_CARACTERES + " caracteres.",
                    "Texto demasiado largo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ruta = archivoSeleccionado != null ? archivoSeleccionado.getAbsolutePath() : "";
        try {
            GestorPosts.crearPost(actual.getUsername(), texto, ruta);
            JOptionPane.showMessageDialog(this, "¡Publicación creada!",
                    "Listo", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            controlador.refrescarPerfilPropio();
            controlador.refrescarFeed();
            controlador.mostrarSeccion("FEED");
        } catch (ArchivoCorruptoException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la publicación: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtTexto.setText("");
        archivoSeleccionado = null;
        lblVistaPrevia.setIcon(null);
        lblVistaPrevia.setText("Sin imagen seleccionada");
        actualizarContador();
    }
}