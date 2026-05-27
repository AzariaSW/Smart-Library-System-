import com.formdev.flatlaf.FlatLightLaf;
import gui.LoginScreen;
import gui.SplashScreen;

import javax.swing.*;

/**
 * Smart Library System — Application Entry Point
 *
 * Startup sequence:
 *  1. Apply FlatLaf modern look and feel
 *  2. Show animated splash screen
 *  3. Launch login screen
 *
 * Required JARs in /lib:
 *  - flatlaf-3.x.jar         (FlatLaf UI library)
 *  - mysql-connector-j-8.x.jar (MySQL JDBC driver)
 */
public class Main {

    public static void main(String[] args) {
        // ── Apply FlatLaf Look & Feel ────────────────────────────────────────────
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("TabbedPane.tabHeight", 36);
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        } catch (Exception e) {
            System.err.println("FlatLaf not available, using system L&F: " + e.getMessage());
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // ── Launch on Event Dispatch Thread ──────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showAndAnimate(() -> {
                SwingUtilities.invokeLater(() -> {
                    LoginScreen login = new LoginScreen();
                    login.setVisible(true);
                });
            });
        });
    }
}
