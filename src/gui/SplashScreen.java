package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Animated splash screen shown on application startup.
 * Displays logo, app name, and a progress bar while initializing.
 */
public class SplashScreen extends JWindow {

    private JProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        setSize(520, 320);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 520, 320, 20, 20));
        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                                                     getWidth(), getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(40, 50, 30, 50));

        // ── Center content ──────────────────────────────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Book icon (Unicode emoji as placeholder)
        JLabel iconLabel = new JLabel("📚", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Smart Library System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

//        JLabel subtitleLabel = new JLabel("Advanced Programming Project", SwingConstants.CENTER);
//        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        subtitleLabel.setForeground(new Color(148, 163, 184));
//        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        JLabel versionLabel = new JLabel("Version 1.0.0", SwingConstants.CENTER);
//        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
//        versionLabel.setForeground(new Color(100, 116, 139));
//        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(iconLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(6));
        //center.add(subtitleLabel);
        center.add(Box.createVerticalStrut(4));
        //center.add(versionLabel);

        // ── Bottom: progress ────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        statusLabel = new JLabel("Initializing...", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(148, 163, 184));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(37, 99, 235));
        progressBar.setBackground(new Color(30, 41, 59));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(420, 4));

        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(progressBar, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);
        setContentPane(main);
    }

    /**
     * Animates the splash screen with progress updates, then closes.
     * @param onComplete callback to run after splash finishes
     */
    public void showAndAnimate(Runnable onComplete) {
        setVisible(true);
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                String[] steps = {
                    "Loading configuration...",
                    "Connecting to database...",
                    "Starting RMI server...",
                    "Starting notification server...",
                    "Loading resources...",
                    "Ready!"
                };
                for (int i = 0; i < steps.length; i++) {
                    final int progress = (i + 1) * (100 / steps.length);
                    final String step = steps[i];
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(progress);
                        statusLabel.setText(step);
                    });
                    Thread.sleep(700);
                }
                return null;
            }

            @Override protected void done() {
                dispose();
                if (onComplete != null) onComplete.run();
            }
        };
        worker.execute();
    }
}
