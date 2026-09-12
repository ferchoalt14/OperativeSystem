package operativesystem;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        GestorArchivosBinarios.inicializarSistema();
        GestorInstaPlus.inicializarSistema();

        SwingUtilities.invokeLater(() -> {
            establecerAparienciaDelSistema();
            new PantallaLogin().setVisible(true);
        });
    }

    private static void establecerAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
  
        }
    }
}