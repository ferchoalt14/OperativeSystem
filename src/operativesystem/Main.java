package operativesystem;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicación Mini-Windows. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        GestorArchivosBinarios.inicializarSistema();

        SwingUtilities.invokeLater(() -> {
            establecerAparienciaDelSistema();
            new PantallaLogin().setVisible(true);
        });
    }

    private static void establecerAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // La aplicación funciona con la apariencia predeterminada de Swing.
        }
    }
}
