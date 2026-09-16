package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Pantalla para editar el username, nombre completo, descripción y estado (activa/inactiva) del perfil propio.
 * Se abre desde el botón "Editar perfil" de tu propio perfil. Sin ventanas emergentes.
 */
public class PanelInstaEditarPerfil extends JPanel {

    private static final int MAX_DESCRIPCION = 150;

    private final InstaControlador controlador;

    private final JLabel lblAvatar = new JLabel();
    private final JLabel lblArroba = new JLabel();
    private final JTextField txtNombre;
    private final JTextField txtUsername;
    private final JTextArea txtDescripcion;
    private final JLabel lblContadorDesc;
    private final JButton btnGuardar;
    private final MensajeEnLinea mensaje = new MensajeEnLinea();

    private final JLabel lblEstadoActual;
    private final JButton btnEstadoCuenta;
    private final ConfirmacionEnLinea confirmacion = new ConfirmacionEnLinea();
    private final MensajeEnLinea mensajeEstado = new MensajeEnLinea();

    public PanelInstaEditarPerfil(InstaControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setOpaque(false);

        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setBorder(new EmptyBorder(18, 30, 30, 30));

        // Cabecera
        JButton btnVolver = EstiloInsta.botonTexto("← Volver a mi perfil", TemaUI.ACCENT_OSCURO, 13, true);
        btnVolver.addActionListener(e -> volverAlPerfil());
        JPanel filaVolver = EstiloInsta.filaAjustada(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaVolver.add(btnVolver);
        columna.add(filaVolver);
        columna.add(Box.createVerticalStrut(8));

        JLabel lblTitulo = EstiloInsta.etiqueta("Editar perfil", 22, true, TemaUI.ACCENT_OSCURO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(lblTitulo);
        columna.add(Box.createVerticalStrut(12));

        // Tarjeta con datos
        EstiloInsta.PanelRedondeado tarjeta = new EstiloInsta.PanelRedondeado(
                new GridBagLayout(), TemaUI.SUPERFICIE, TemaUI.BORDE, 18) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setBorder(new EmptyBorder(18, 20, 18, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        lblArroba.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblArroba.setForeground(TemaUI.TEXTO);
        JPanel filaAvatar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaAvatar.setOpaque(false);
        filaAvatar.add(lblAvatar);
        filaAvatar.add(lblArroba);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        tarjeta.add(filaAvatar, gbc);
        gbc.gridwidth = 1;

        txtNombre = EstiloInsta.campoTexto(22);
        txtUsername = EstiloInsta.campoTexto(22);
        txtDescripcion = new JTextArea(4, 22);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtDescripcion.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setBorder(BorderFactory.createLineBorder(TemaUI.BORDE));

        lblContadorDesc = EstiloInsta.etiqueta("", 11, false, TemaUI.TEXTO_SUAVE);
        txtDescripcion.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarContador(); }
        });

        agregarFila(tarjeta, gbc, 1, "Nombre completo", txtNombre);
        agregarFila(tarjeta, gbc, 2, "Username", txtUsername);
        agregarFila(tarjeta, gbc, 3, "Descripción", scrollDesc);

        gbc.gridx = 1; gbc.gridy = 4;
        gbc.insets = new Insets(0, 4, 6, 4);
        tarjeta.add(lblContadorDesc, gbc);
        gbc.insets = new Insets(6, 4, 6, 4);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        tarjeta.add(mensaje, gbc);

        btnGuardar = TemaUI.crearBotonPrimario("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filaBotones.setOpaque(false);
        JButton btnCancelar = EstiloInsta.botonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> volverAlPerfil());
        filaBotones.add(btnCancelar);
        filaBotones.add(btnGuardar);
        gbc.gridy = 6;
        tarjeta.add(filaBotones, gbc);

        columna.add(tarjeta);
        columna.add(Box.createVerticalStrut(18));

        // Tarjeta de estado de la cuenta
        EstiloInsta.PanelRedondeado tarjetaEstado = new EstiloInsta.PanelRedondeado(
                null, TemaUI.SUPERFICIE, TemaUI.BORDE, 18) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tarjetaEstado.setLayout(new BoxLayout(tarjetaEstado, BoxLayout.Y_AXIS));
        tarjetaEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjetaEstado.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblEstadoTitulo = EstiloInsta.etiqueta("Estado de la cuenta", 14, true, TemaUI.TEXTO);
        lblEstadoTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblEstadoActual = EstiloInsta.etiqueta("", 12, false, TemaUI.TEXTO_SUAVE);
        lblEstadoActual.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEstadoCuenta = EstiloInsta.botonSecundario("");
        btnEstadoCuenta.addActionListener(e -> pedirCambioEstado());
        JPanel filaEstado = EstiloInsta.filaAjustada(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaEstado.add(btnEstadoCuenta);

        tarjetaEstado.add(lblEstadoTitulo);
        tarjetaEstado.add(Box.createVerticalStrut(4));
        tarjetaEstado.add(lblEstadoActual);
        tarjetaEstado.add(Box.createVerticalStrut(10));
        tarjetaEstado.add(filaEstado);
        tarjetaEstado.add(Box.createVerticalStrut(8));
        tarjetaEstado.add(confirmacion);
        tarjetaEstado.add(mensajeEstado);

        columna.add(tarjetaEstado);

        JPanel envoltorio = new PanelDesplazable(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(columna, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        JLabel lbl = EstiloInsta.etiqueta(etiqueta, 12, true, TemaUI.TEXTO_SUAVE);
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    /** Llamar cada vez que se muestre este panel, para cargar los datos actuales del usuario. */
    public void cargarDatos() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        lblAvatar.setIcon(AvatarHelper.avatarPara(actual.getUsername(), 44));
        lblArroba.setText("@" + actual.getUsername());
        txtNombre.setText(actual.getNombreCompleto());
        txtUsername.setText(actual.getUsername());
        txtDescripcion.setText(actual.getDescripcion());
        txtDescripcion.setCaretPosition(0);
        mensaje.ocultar();
        mensajeEstado.ocultar();
        confirmacion.ocultar();
        actualizarContador();
        actualizarEstado(actual);
    }

    private void volverAlPerfil() {
        confirmacion.ocultar();
        controlador.refrescarPerfilPropio();
        controlador.mostrarSeccion("PERFIL");
    }

    private void actualizarContador() {
        int n = txtDescripcion.getText().length();
        lblContadorDesc.setText(n + " / " + MAX_DESCRIPCION);
        lblContadorDesc.setForeground(n > MAX_DESCRIPCION ? EstiloInsta.ROJO : TemaUI.TEXTO_SUAVE);
    }

    private void actualizarEstado(UsuarioInsta actual) {
        if (actual.isActiva()) {
            lblEstadoActual.setText("Tu cuenta está activa y visible para los demás.");
            btnEstadoCuenta.setText("Desactivar cuenta");
            btnEstadoCuenta.setForeground(EstiloInsta.ROJO);
        } else {
            lblEstadoActual.setText("Tu cuenta está desactivada: no apareces en búsquedas ni se muestran tus publicaciones.");
            btnEstadoCuenta.setText("Reactivar cuenta");
            btnEstadoCuenta.setForeground(EstiloInsta.VERDE);
        }
    }

    private void guardar() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }

        String nuevoNombre = txtNombre.getText().trim();
        String nuevoUsername = txtUsername.getText().trim();
        String nuevaDescripcion = txtDescripcion.getText().trim();

        if (nuevoNombre.isEmpty() || nuevoUsername.isEmpty()) {
            mensaje.error("El nombre y el username no pueden estar vacíos.");
            return;
        }
        if (!nuevoUsername.matches("[A-Za-z0-9._]{3,30}")) {
            mensaje.error("El username debe tener de 3 a 30 caracteres y solo letras, números, punto o guion bajo.");
            return;
        }
        if (nuevaDescripcion.length() > MAX_DESCRIPCION) {
            mensaje.error("La descripción no puede superar " + MAX_DESCRIPCION + " caracteres.");
            return;
        }

        String usernameOriginal = actual.getUsername();
        String nombreAnterior = actual.getNombreCompleto();
        String descripcionAnterior = actual.getDescripcion();
        boolean cambiaUsername = !nuevoUsername.equals(usernameOriginal);

        try {
            if (cambiaUsername && !nuevoUsername.equalsIgnoreCase(usernameOriginal)
                    && GestorInstaPlus.buscarPorUsername(nuevoUsername) != null) {
                mensaje.error("Ese username ya está en uso por otra cuenta.");
                return;
            }

            if (cambiaUsername) {
                GestorInstaPlus.renombrarUsuario(usernameOriginal, nuevoUsername);
            }
            actual.setNombreCompleto(nuevoNombre);
            actual.setUsername(nuevoUsername);
            actual.setDescripcion(nuevaDescripcion);
            GestorInstaPlus.actualizarUsuario(actual, usernameOriginal);

            controlador.usuarioActualizado(usernameOriginal);
            controlador.mostrarSeccion("PERFIL");
            controlador.mostrarAviso("¡Perfil actualizado!", "Tus cambios ya se ven en tu perfil.");
        } catch (ArchivoCorruptoException | IOException ex) {
            // Revertimos lo que estaba en memoria para no quedar en un estado a medias.
            actual.setNombreCompleto(nombreAnterior);
            actual.setUsername(usernameOriginal);
            actual.setDescripcion(descripcionAnterior);
            mensaje.error("No se pudo guardar el perfil: " + ex.getMessage());
        }
    }

    private void pedirCambioEstado() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        mensajeEstado.ocultar();
        if (actual.isActiva()) {
            confirmacion.preguntar("¿Seguro que quieres desactivar tu cuenta? No aparecerás en búsquedas "
                    + "ni se mostrarán tus publicaciones mientras esté desactivada.", "Desactivar", this::alternarEstadoCuenta);
        } else {
            // Si ya está desactivada, se reactiva directo (sin pedir confirmación).
            alternarEstadoCuenta();
        }
    }

    private void alternarEstadoCuenta() {
        UsuarioInsta actual = controlador.getUsuarioActual();
        if (actual == null) {
            return;
        }
        boolean estabaActiva = actual.isActiva();
        try {
            actual.setActiva(!estabaActiva);
            GestorInstaPlus.actualizarUsuario(actual, actual.getUsername());
            actualizarEstado(actual);
            // Avisa a la pantalla principal para bloquear o desbloquear el menú.
            controlador.usuarioActualizado(actual.getUsername());
            controlador.mostrarSeccion("EDITAR_PERFIL");
            mensajeEstado.exito(estabaActiva
                    ? "Tu cuenta fue desactivada. Solo podrás entrar para reactivarla."
                    : "¡Tu cuenta fue reactivada! Ya puedes usar INSTA+ normalmente.");
        } catch (ArchivoCorruptoException | IOException ex) {
            actual.setActiva(estabaActiva);
            mensajeEstado.error("No se pudo actualizar el estado de la cuenta: " + ex.getMessage());
        }
    }
}