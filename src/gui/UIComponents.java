package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Factory class for reusable, styled UI components.
 * Ensures consistent look across all screens.
 */
public class UIComponents {

    // ── Buttons ────────────────────────────────────────────────────────────────

    /** Primary filled button (blue) */
    public static JButton primaryButton(String text) {
        return styledButton(text, AppTheme.PRIMARY, AppTheme.PRIMARY_DARK, AppTheme.TEXT_WHITE);
    }

    /** Success/green button */
    public static JButton successButton(String text) {
        return styledButton(text, AppTheme.ACCENT, AppTheme.ACCENT_DARK, AppTheme.TEXT_WHITE);
    }

    /** Danger/red button */
    public static JButton dangerButton(String text) {
        return styledButton(text, AppTheme.DANGER, AppTheme.DANGER_DARK, AppTheme.TEXT_WHITE);
    }

    /** Warning/amber button */
    public static JButton warningButton(String text) {
        return styledButton(text, AppTheme.WARNING, new Color(217, 119, 6), AppTheme.TEXT_WHITE);
    }

    /** Outlined/secondary button */
    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(AppTheme.PRIMARY_LIGHT);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 246, 255));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.setColor(AppTheme.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_BUTTON);
        btn.setForeground(AppTheme.PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? hover :
                            getModel().isRollover() ? hover.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_BUTTON);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 36));
        return btn;
    }

    // ── Text Fields ────────────────────────────────────────────────────────────

    public static JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(AppTheme.TEXT_SECONDARY);
                    g2.setFont(AppTheme.FONT_BODY);
                    g2.drawString(placeholder, 8, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(AppTheme.FONT_BODY);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(AppTheme.BORDER, AppTheme.CORNER_RADIUS),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(200, 36));
        addFocusBorder(field);
        return field;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(AppTheme.FONT_BODY);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(AppTheme.BORDER, AppTheme.CORNER_RADIUS),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(200, 36));
        addFocusBorder(field);
        return field;
    }

    public static JTextArea styledTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(AppTheme.FONT_BODY);
        area.setForeground(AppTheme.TEXT_PRIMARY);
        area.setBackground(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return area;
    }

    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(AppTheme.FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setForeground(AppTheme.TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(200, 36));
        return combo;
    }

    // ── Labels ─────────────────────────────────────────────────────────────────

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_TITLE);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SUBTITLE);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel secondaryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        return lbl;
    }

    // ── Cards ──────────────────────────────────────────────────────────────────

    /** Creates a white card panel with rounded corners and drop shadow effect */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                // Card background
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, AppTheme.CORNER_RADIUS, AppTheme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.CARD_PADDING.top, AppTheme.CARD_PADDING.left,
            AppTheme.CARD_PADDING.bottom, AppTheme.CARD_PADDING.right));
        return panel;
    }

    // ── Tables ─────────────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setFont(AppTheme.FONT_TABLE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(Color.WHITE);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(AppTheme.FONT_TABLE_HDR);
        table.getTableHeader().setBackground(AppTheme.BG_TABLE_HEADER);
        table.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        table.getTableHeader().setReorderingAllowed(false);
        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : AppTheme.BG_TABLE_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBackground(Color.WHITE);
        return sp;
    }

    // ── Search Bar ─────────────────────────────────────────────────────────────

    public static JTextField searchField(String placeholder) {
        JTextField field = styledTextField(placeholder);
        field.setPreferredSize(new Dimension(280, 36));
        return field;
    }

    // ── Separator ──────────────────────────────────────────────────────────────

    public static JSeparator styledSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER);
        return sep;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static void addFocusBorder(JComponent field) {
        Border normal = field.getBorder();
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(AppTheme.BORDER_FOCUS, AppTheme.CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(normal);
            }
        });
    }

    // ── Inner: Rounded Border ──────────────────────────────────────────────────

    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
        @Override public boolean isBorderOpaque()            { return false; }
    }
}
