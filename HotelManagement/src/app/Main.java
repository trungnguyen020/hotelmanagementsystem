package app;

import ui.auth.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}