package gui;

import db.LibrarianDAO;
import model.Librarian;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen — authenticates librarians before granting access.
 */
public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private JLabel loadingLabel;

    private final LibrarianDAO librarianDAO = new LibrarianDAO();

    public LoginScreen() {
        setTitle("Smart Library System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());

        // ── Left panel: branding ────────────────────────────────────────────────
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42),
                                                     0, getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(380, 580));

        JPanel brandContent = new JPanel();
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));
        brandContent.setOpaque(false);

        JLabel bookIcon = new JLabel("📚");
        bookIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        bookIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Smart Library");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appSub = new JLabel("System");
        appSub.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appSub.setForeground(new Color(147, 197, 253));
        appSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Manage your library<br>efficiently and smartly</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(148, 163, 184));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        brandContent.add(bookIcon);
        brandContent.add(Box.createVerticalStrut(16));
        brandContent.add(appName);
        brandContent.add(appSub);
        brandContent.add(Box.createVerticalStrut(16));
        brandContent.add(tagline);

        leftPanel.add(brandContent);

        // ── Right panel: login form ─────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(AppTheme.BG_MAIN);

        JPanel formCard = UIComponents.cardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(360, 420));

        JLabel welcomeLabel = new JLabel("Welcome back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(AppTheme.TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signInLabel = new JLabel("Sign in to your account");
        signInLabel.setFont(AppTheme.FONT_BODY);
        signInLabel.setForeground(AppTheme.TEXT_SECONDARY);
        signInLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(AppTheme.FONT_BODY);
        userLbl.setForeground(AppTheme.TEXT_PRIMARY);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIComponents.styledTextField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(AppTheme.FONT_BODY);
        passLbl.setForeground(AppTheme.TEXT_PRIMARY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = UIComponents.styledPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Loading label
        loadingLabel = new JLabel(" ");
        loadingLabel.setFont(AppTheme.FONT_SMALL);
        loadingLabel.setForeground(AppTheme.TEXT_SECONDARY);
        loadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        loginButton = UIComponents.primaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(300, 40));

        // Hint
        JLabel hintLabel = new JLabel("Default: admin / Admin@123");
        hintLabel.setFont(AppTheme.FONT_SMALL);
        hintLabel.setForeground(AppTheme.TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(welcomeLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(signInLabel);
        formCard.add(Box.createVerticalStrut(24));
        formCard.add(userLbl);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(passLbl);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(errorLabel);
        formCard.add(loadingLabel);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(hintLabel);

        rightPanel.add(formCard);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);
        setContentPane(root);

        // ── Event listeners ─────────────────────────────────────────────────────
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loadingLabel.setText("Authenticating...");
        errorLabel.setText(" ");

        SwingWorker<Librarian, Void> worker = new SwingWorker<>() {
            @Override protected Librarian doInBackground() throws Exception {
                return librarianDAO.authenticate(username, password);
            }

            @Override protected void done() {
                loginButton.setEnabled(true);
                loadingLabel.setText(" ");
                try {
                    Librarian librarian = get();
                    if (librarian != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            MainFrame mainFrame = new MainFrame(librarian);
                            mainFrame.setVisible(true);
                        });
                    } else {
                        showError("Invalid username or password.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    showError("Connection error: " + ex.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
