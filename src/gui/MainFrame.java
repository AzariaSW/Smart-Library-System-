package gui;

import model.Librarian;
import network.NotificationClient;
import network.NotificationServer;
import rmi.RMIClient;
import rmi.RMIServer;
import threads.OverdueCheckerThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window.
 * Manages sidebar navigation, CardLayout content, and all background services.
 * Acts as the central refresh coordinator — every panel calls refreshAll()
 * after any data-mutating operation so the entire UI stays in sync.
 */
public class MainFrame extends JFrame {

    // ── Services (static so panels can access them) ────────────────────────────
    public static NotificationServer notificationServer;
    public static NotificationClient notificationClient;
    public static RMIClient          rmiClient;

    private final Librarian currentLibrarian;
    private OverdueCheckerThread overdueChecker;

    // ── UI ─────────────────────────────────────────────────────────────────────
    private JPanel    contentPanel;
    private CardLayout cardLayout;
    private JLabel    clockLabel;
    private JButton[] navButtons;

    // ── Screens ────────────────────────────────────────────────────────────────
    private DashboardPanel    dashboardPanel;
    private BooksPanel        booksPanel;
    private MembersPanel      membersPanel;
    private BorrowPanel       borrowPanel;
    private ReturnPanel       returnPanel;
    private LoanHistoryPanel  loanHistoryPanel;
    private OverduePanel      overduePanel;
    private RMIPanel          rmiPanel;
    private NotificationPanel notificationPanel;

    // Ethiopian timezone
    private static final ZoneId SYSTEM_ZONE  =  ZoneId.systemDefault();
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MainFrame(Librarian librarian) {
        this.currentLibrarian = librarian;
        setTitle("Smart Library System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        initServices();
        initComponents();
        startClock();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });
    }

    // ── Service startup ────────────────────────────────────────────────────────

    private void initServices() {
        notificationServer = new NotificationServer();
        notificationServer.start();

        notificationClient = new NotificationClient();
        notificationClient.connect();

        new Thread(() -> RMIServer.startServer(), "RMI-Server-Thread").start();

        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            rmiClient = new RMIClient();
            rmiClient.connect();
        }, "RMI-Client-Thread").start();

        overdueChecker = new OverdueCheckerThread(notificationServer);
        overdueChecker.start();
    }

    // ── UI construction ────────────────────────────────────────────────────────

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_MAIN);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppTheme.BG_MAIN);

        dashboardPanel   = new DashboardPanel(currentLibrarian);
        booksPanel       = new BooksPanel(currentLibrarian);
        membersPanel     = new MembersPanel(currentLibrarian);
        borrowPanel      = new BorrowPanel(currentLibrarian);
        returnPanel      = new ReturnPanel(currentLibrarian);
        loanHistoryPanel = new LoanHistoryPanel();
        overduePanel     = new OverduePanel();
        rmiPanel         = new RMIPanel();
        notificationPanel= new NotificationPanel();

        contentPanel.add(dashboardPanel,   "DASHBOARD");
        contentPanel.add(booksPanel,       "BOOKS");
        contentPanel.add(membersPanel,     "MEMBERS");
        contentPanel.add(borrowPanel,      "BORROW");
        contentPanel.add(returnPanel,      "RETURN");
        contentPanel.add(loanHistoryPanel, "HISTORY");
        contentPanel.add(overduePanel,     "OVERDUE");
        contentPanel.add(rmiPanel,         "RMI");
        contentPanel.add(notificationPanel,"NOTIFICATIONS");

        notificationClient.setMessageHandler(msg -> notificationPanel.addNotification(msg));

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentPanel,   BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 16, 20));

        JLabel logoIcon = new JLabel("📚");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoText = new JLabel("Smart Library");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Role badge
        boolean isAdmin = currentLibrarian.isAdmin();
        JLabel roleBadge = new JLabel(isAdmin ? "  ADMIN  " : "  LIBRARIAN  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setForeground(isAdmin ? new Color(253, 224, 71) : new Color(134, 239, 172));
        roleBadge.setOpaque(true);
        roleBadge.setBackground(isAdmin ? new Color(120, 53, 15) : new Color(6, 78, 59));
        roleBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createVerticalStrut(6));
        logoPanel.add(logoText);
        logoPanel.add(Box.createVerticalStrut(6));
        logoPanel.add(roleBadge);

        // Nav items
        String[][] navItems = {
            {"🏠", "Dashboard",     "DASHBOARD"},
            {"📖", "Books",         "BOOKS"},
            {"👥", "Members",       "MEMBERS"},
            {"➕", "Borrow Book",   "BORROW"},
            {"↩",  "Return Book",   "RETURN"},
            {"📋", "Loan History",  "HISTORY"},
            {"⚠",  "Overdue Books", "OVERDUE"},
            {"🔗", "RMI Query",     "RMI"},
            {"🔔", "Notifications", "NOTIFICATIONS"},
        };

        navButtons = new JButton[navItems.length];
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            final String card = navItems[i][2];
            JButton btn = createNavButton(navItems[i][0], navItems[i][1], i == 0);
            btn.addActionListener(e -> navigateTo(card, idx));
            navButtons[i] = btn;
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(1));
        }

        // Bottom: clock + user info + logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 20, 16));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(51, 65, 85));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        clockLabel = new JLabel("00:00:00  (EAT)");
        clockLabel.setFont(AppTheme.FONT_CLOCK);
        clockLabel.setForeground(new Color(148, 163, 184));
        clockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("👤 " + currentLibrarian.getFullName());
        userLabel.setFont(AppTheme.FONT_SMALL);
        userLabel.setForeground(new Color(148, 163, 184));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("⏻  Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(127, 29, 29) : new Color(69, 10, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(AppTheme.FONT_SMALL);
        logoutBtn.setForeground(new Color(252, 165, 165));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> confirmLogout());

        bottomPanel.add(sep);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(clockLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(userLabel);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(logoutBtn);

        sidebar.add(logoPanel,   BorderLayout.NORTH);
        sidebar.add(navPanel,    BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String icon, String label, boolean active) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") == Boolean.TRUE) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(active ? AppTheme.FONT_SIDEBAR_ACTIVE : AppTheme.FONT_SIDEBAR);
        btn.setForeground(active ? AppTheme.TEXT_SIDEBAR_ACTIVE : AppTheme.TEXT_SIDEBAR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 42));
        if (active) btn.putClientProperty("active", Boolean.TRUE);
        return btn;
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    /**
     * Switches the visible panel and refreshes it with fresh DB data.
     */
    public void navigateTo(String card, int navIndex) {
        cardLayout.show(contentPanel, card);
        for (int i = 0; i < navButtons.length; i++) {
            boolean isActive = (i == navIndex);
            navButtons[i].putClientProperty("active", isActive);
            navButtons[i].setFont(isActive ? AppTheme.FONT_SIDEBAR_ACTIVE : AppTheme.FONT_SIDEBAR);
            navButtons[i].setForeground(isActive ? AppTheme.TEXT_SIDEBAR_ACTIVE : AppTheme.TEXT_SIDEBAR);
            navButtons[i].repaint();
        }
        switch (card) {
            case "DASHBOARD"     -> dashboardPanel.refresh();
            case "BOOKS"         -> booksPanel.refresh();
            case "MEMBERS"       -> membersPanel.refresh();
            case "BORROW"        -> borrowPanel.refresh();
            case "RETURN"        -> returnPanel.refresh();
            case "HISTORY"       -> loanHistoryPanel.refresh();
            case "OVERDUE"       -> overduePanel.refresh();
        }
    }

    /**
     * Refreshes ALL data-bearing panels from the database.
     * Call this after any operation that mutates data.
     */
    public void refreshAll() {
        dashboardPanel.refresh();
        // Only refresh the currently visible panels immediately;
        // others will refresh when navigated to.
        returnPanel.refresh();
        overduePanel.refresh();
        loanHistoryPanel.refresh();
    }

    // ── Clock ──────────────────────────────────────────────────────────────────

    private void startClock() {
        Timer timer = new Timer(1000, e ->
            clockLabel.setText(ZonedDateTime.now(SYSTEM_ZONE ).format(CLOCK_FMT))
        );
        timer.start();
    }

    // ── Shutdown ───────────────────────────────────────────────────────────────

    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            shutdown();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        }
    }

    private void confirmExit() {
        int r = JOptionPane.showConfirmDialog(this,
            "Exit Smart Library System?", "Confirm Exit",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            shutdown();
            System.exit(0);
        }
    }

    private void shutdown() {
        if (overdueChecker  != null) overdueChecker.stop();
        if (notificationClient != null) notificationClient.disconnect();
        if (notificationServer != null) notificationServer.stop();
        try { db.DatabaseConnection.getInstance().close(); } catch (Exception ignored) {}
    }

    // ── Accessors for panels ───────────────────────────────────────────────────

    public Librarian getCurrentLibrarian() { return currentLibrarian; }
}
