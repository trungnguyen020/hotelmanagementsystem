package app;

import ui.auth.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import java.awt.Color;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Fix selection colors: keep text dark on light selection background
            UIManager.put("Table.selectionBackground", new Color(220, 235, 250));
            UIManager.put("Table.selectionForeground", new Color(30, 30, 30));
            UIManager.put("Table.selectionInactiveBackground", new Color(230, 235, 240));
            UIManager.put("Table.selectionInactiveForeground", new Color(30, 30, 30));
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}