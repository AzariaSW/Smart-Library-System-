package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Live Notification Panel.
 * Color-coded cards:
 *   Blue   = new book / new member
 *   Orange = book borrowed
 *   Green  = book returned
 *   Red    = overdue detected
 *   Purple = fine paid
 *   Gray   = deleted / updated
 */
public class NotificationPanel extends JPanel {

    private JPanel notificationsContainer;
    private JScrollPane scrollPane;
    private JLabel countLabel;
    private final List<String> notifications = new ArrayList<>();
    private int unreadCount = 0;

    private static final ZoneId ET_ZONE = ZoneId.of("Africa/Addis_Ababa");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificationPanel() {
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIComponents.titleLabel("🔔  Live Notifications");
        countLabel   = UIComponents.secondaryLabel("No notifications yet");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,      BorderLayout.NORTH);
        headerLeft.add(countLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JButton testBtn  = UIComponents.outlineButton("📡 Test Broadcast");
        JButton clearBtn = UIComponents.dangerButton("🗑 Clear All");

        testBtn.addActionListener(e -> {
            if (MainFrame.notificationServer != null) {
                MainFrame.notificationServer.broadcast(
                    "🧪 Test notification — " + LocalTime.now(ET_ZONE).format(TIME_FMT));
            }
        });
        clearBtn.addActionListener(e -> clearNotifications());

        headerRight.add(testBtn);
        headerRight.add(clearBtn);
        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Notifications container
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(AppTheme.BG_MAIN);

        showEmptyState("No notifications yet. Events will appear here in real-time.");

        scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scrollPane.getViewport().setBackground(AppTheme.BG_MAIN);
        scrollPane.setBackground(AppTheme.BG_MAIN);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JLabel connLabel = new JLabel("● Socket Notification Server: Active on port 9090");
        connLabel.setFont(AppTheme.FONT_SMALL);
        connLabel.setForeground(AppTheme.ACCENT);
        statusBar.add(connLabel);

        add(header,    BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Adds a notification card. Called on EDT by NotificationClient.
     */
    public void addNotification(String message) {
        notifications.add(message);
        unreadCount++;

        if (notifications.size() == 1) {
            notificationsContainer.removeAll();
        }

        JPanel card = buildCard(message, notifications.size());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        notificationsContainer.add(card, 0);
        notificationsContainer.add(Box.createVerticalStrut(6), 0);
        notificationsContainer.revalidate();
        notificationsContainer.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMinimum());
        });

        countLabel.setText(notifications.size() + " notification(s) | " + unreadCount + " unread");
    }

    private JPanel buildCard(String message, int index) {
        Color accent = resolveAccentColor(message);

        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight()-1, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel iconLabel = new JLabel(resolveIcon(message));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        // Strip the timestamp prefix if present for display
        String displayMsg = message;
        if (message.startsWith("[") && message.contains("] ")) {
            displayMsg = message.substring(message.indexOf("] ") + 2);
        }

        JLabel msgLabel = new JLabel(displayMsg);
        msgLabel.setFont(AppTheme.FONT_BODY);
        msgLabel.setForeground(AppTheme.TEXT_PRIMARY);

        // Timestamp badge
        String ts = LocalTime.now(ET_ZONE).format(TIME_FMT);
        JLabel tsLabel = new JLabel(ts);
        tsLabel.setFont(AppTheme.FONT_SMALL);
        tsLabel.setForeground(AppTheme.TEXT_SECONDARY);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(msgLabel,  BorderLayout.CENTER);
        card.add(tsLabel,   BorderLayout.EAST);
        return card;
    }

    private Color resolveAccentColor(String msg) {
        if (msg.contains("⚠") || msg.contains("OVERDUE"))                    return AppTheme.DANGER;
        if (msg.contains("↩")  || msg.contains("returned"))                  return AppTheme.ACCENT;
        if (msg.contains("📖") || msg.contains("added") || msg.contains("👤")) return AppTheme.PRIMARY;
        if (msg.contains("📋") || msg.contains("borrowed"))                   return AppTheme.WARNING;
        if (msg.contains("💜") || msg.contains("Fine paid"))                  return AppTheme.PURPLE;
        if (msg.contains("🗑")  || msg.contains("deleted") || msg.contains("✏")) return AppTheme.GRAY;
        return AppTheme.TEXT_SECONDARY;
    }

    private String resolveIcon(String msg) {
        if (msg.contains("⚠"))  return "⚠";
        if (msg.contains("↩"))  return "↩";
        if (msg.contains("📖")) return "📖";
        if (msg.contains("📋")) return "📋";
        if (msg.contains("💜")) return "💜";
        if (msg.contains("🗑")) return "🗑";
        if (msg.contains("✏")) return "✏";
        if (msg.contains("👤")) return "👤";
        if (msg.contains("🧪")) return "🧪";
        return "🔔";
    }

    private void clearNotifications() {
        notifications.clear();
        unreadCount = 0;
        notificationsContainer.removeAll();
        showEmptyState("Notifications cleared. New events will appear here.");
        notificationsContainer.revalidate();
        notificationsContainer.repaint();
        countLabel.setText("No notifications");
    }

    private void showEmptyState(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(16, 8, 0, 0));
        notificationsContainer.add(lbl);
        notificationsContainer.add(Box.createVerticalGlue());
    }
}
