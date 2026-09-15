package operativesystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Franja que se muestra arriba de TU perfil: descripción (bio) + botón "Editar perfil".
 */
public class PanelDescripcionPerfil extends EstiloInsta.PanelRedondeado {

    private final JLabel lblNombre;
    private final JTextArea txtDescripcion;

    public PanelDescripcionPerfil(InstaControlador controlador) {
        super(new BorderLayout(14, 0), TemaUI.SUPERFICIE, TemaUI.BORDE, 18);
        setBorder(new EmptyBorder(12, 16, 12, 14));

        lblNombre = EstiloInsta.etiqueta("", 14, true, TemaUI.TEXTO);
        txtDescripcion = EstiloInsta.textoAjustable("", 13, TemaUI.TEXTO);

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        textos.add(lblNombre, BorderLayout.NORTH);
        textos.add(txtDescripcion, BorderLayout.CENTER);

        JButton btnEditar = TemaUI.crearBotonPrimario("✏  Editar perfil");
        btnEditar.addActionListener(e -> controlador.abrirEditarPerfil());
        JPanel colBoton = new JPanel(new BorderLayout());
        colBoton.setOpaque(false);
        colBoton.add(btnEditar, BorderLayout.NORTH);

        add(textos, BorderLayout.CENTER);
        add(colBoton, BorderLayout.EAST);
    }

    public void refrescar(UsuarioInsta usuario) {
        if (usuario == null) {
            return;
        }
        lblNombre.setText(usuario.getNombreCompleto());
        String desc = usuario.getDescripcion();
        if (desc == null || desc.isBlank()) {
            txtDescripcion.setText("Aún no tienes descripción. Toca “Editar perfil” para agregar una.");
            txtDescripcion.setForeground(TemaUI.TEXTO_SUAVE);
            txtDescripcion.setFont(new Font("SansSerif", Font.ITALIC, 12));
        } else {
            txtDescripcion.setText(desc);
            txtDescripcion.setForeground(TemaUI.TEXTO);
            txtDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        revalidate();
        repaint();
    }
}