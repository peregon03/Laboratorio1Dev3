package co.edu.univalle.duellite;

import co.edu.univalle.duellite.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación Yu-Gi-Oh! Duel Lite.
 *
 * La ventana se crea dentro de SwingUtilities.invokeLater para garantizar
 * que todos los componentes Swing se inicialicen en el Event Dispatch Thread (EDT),
 * tal como lo requiere Swing para ser thread-safe.
 */
public class Main {

    public static void main(String[] args) {
        // Intentar usar el Look & Feel del sistema operativo
        // para que la aplicación se vea más nativa
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing usa su L&F por defecto. No es un error crítico.
            System.err.println("No se pudo cargar el Look & Feel del sistema: " + e.getMessage());
        }

        // Crear y mostrar la ventana en el EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame ventana = new MainFrame();
            ventana.setVisible(true);
        });
    }
}
