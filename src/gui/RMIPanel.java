package gui;

import model.Book;
import model.Loan;
import rmi.RMIClient;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * RMI Query Panel — demonstrates Remote Method Invocation.
 * Includes explanation, connection status, and three functional query types.
 */
public class RMIPanel extends JPanel {

    private JLabel     connectionStatus;
    private JTextArea  resultArea;
    private JTextField searchTitleField, bookIdField, memberIdField;
    private JTable     resultTable;
    private DefaultTableModel tableModel;

    public RMIPanel() {
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel title = UIComponents.titleLabel("🔗  RMI Remote Query");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel explanation = new JLabel(
            "<html><i>RMI (Remote Method Invocation) allows this application to communicate "
            + "with a remote library server over a network. Use the queries below to fetch "
            + "live data from the RMI server.</i></html>");
        explanation.setFont(AppTheme.FONT_SMALL);
        explanation.setForeground(AppTheme.TEXT_SECONDARY);
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(title);
        headerLeft.add(Box.createVerticalStrut(6));
        headerLeft.add(explanation);

        // Connection status + reconnect
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        connectionStatus = new JLabel("● Checking...");
        connectionStatus.setFont(AppTheme.FONT_SMALL);
        connectionStatus.setForeground(AppTheme.TEXT_SECONDARY);

        JButton reconnectBtn = UIComponents.outlineButton("⟳ Reconnect");
        reconnectBtn.addActionListener(e -> reconnect());

        headerRight.add(connectionStatus);
        headerRight.add(reconnectBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Query cards
        JPanel queriesPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        queriesPanel.setOpaque(false);
        queriesPanel.setPreferredSize(new Dimension(0, 130));

        // Card 1: Search Book
        JPanel card1 = UIComponents.cardPanel();
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        JLabel c1Title = UIComponents.subtitleLabel("Search Book");
        c1Title.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchTitleField = UIComponents.styledTextField("Enter book title...");
        searchTitleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchTitleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton searchBtn = UIComponents.primaryButton("Search via RMI");
        searchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBtn.addActionListener(e -> rmiSearchBook());
        searchTitleField.addActionListener(e -> rmiSearchBook());
        card1.add(c1Title);
        card1.add(Box.createVerticalStrut(8));
        card1.add(searchTitleField);
        card1.add(Box.createVerticalStrut(8));
        card1.add(searchBtn);

        // Card 2: Check Availability
        JPanel card2 = UIComponents.cardPanel();
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
        JLabel c2Title = UIComponents.subtitleLabel("Check Availability");
        c2Title.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookIdField = UIComponents.styledTextField("Enter Book ID...");
        bookIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bookIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton checkBtn = UIComponents.primaryButton("Check via RMI");
        checkBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBtn.addActionListener(e -> rmiCheckAvailability());
        bookIdField.addActionListener(e -> rmiCheckAvailability());
        card2.add(c2Title);
        card2.add(Box.createVerticalStrut(8));
        card2.add(bookIdField);
        card2.add(Box.createVerticalStrut(8));
        card2.add(checkBtn);

        // Card 3: Member Loans
        JPanel card3 = UIComponents.cardPanel();
        card3.setLayout(new BoxLayout(card3, BoxLayout.Y_AXIS));
        JLabel c3Title = UIComponents.subtitleLabel("Member Loans");
        c3Title.setAlignmentX(Component.LEFT_ALIGNMENT);
        memberIdField = UIComponents.styledTextField("Enter Member ID...");
        memberIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        memberIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton loansBtn = UIComponents.primaryButton("Get Loans via RMI");
        loansBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loansBtn.addActionListener(e -> rmiGetMemberLoans());
        memberIdField.addActionListener(e -> rmiGetMemberLoans());
        card3.add(c3Title);
        card3.add(Box.createVerticalStrut(8));
        card3.add(memberIdField);
        card3.add(Box.createVerticalStrut(8));
        card3.add(loansBtn);

        queriesPanel.add(card1);
        queriesPanel.add(card2);
        queriesPanel.add(card3);

        // Results area
        JPanel resultsCard = UIComponents.cardPanel();
        resultsCard.setLayout(new BorderLayout(0, 10));

        JLabel resultsTitle = UIComponents.subtitleLabel("Query Results");
        resultsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        tableModel = new DefaultTableModel(new String[]{"Field", "Value"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultTable = new JTable(tableModel);
        UIComponents.styleTable(resultTable);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(500);

        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setForeground(AppTheme.TEXT_PRIMARY);
        resultArea.setBackground(new Color(248, 250, 252));
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BODY);
        tabs.addTab("Table View", UIComponents.styledScrollPane(resultTable));
        tabs.addTab("Text View",  new JScrollPane(resultArea));

        resultsCard.add(resultsTitle, BorderLayout.NORTH);
        resultsCard.add(tabs,         BorderLayout.CENTER);

        // Bottom: ping
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomRow.setOpaque(false);
        JButton pingBtn = UIComponents.outlineButton("📡  Ping RMI Server");
        pingBtn.addActionListener(e -> {
            RMIClient client = MainFrame.rmiClient;
            String response  = (client != null) ? client.ping() : "RMI client not initialized";
            showResult("PING", response);
        });
        bottomRow.add(pingBtn);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(queriesPanel, BorderLayout.NORTH);
        centerPanel.add(resultsCard,  BorderLayout.CENTER);
        centerPanel.add(bottomRow,    BorderLayout.SOUTH);

        add(header,      BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Check connection after short delay
        Timer t = new Timer(2000, e -> updateConnectionStatus());
        t.setRepeats(false);
        t.start();
    }

    // ── Connection ────────────────────────────────────────────────────────────

    private void updateConnectionStatus() {
        RMIClient client = MainFrame.rmiClient;
        if (client != null && client.isConnected()) {
            connectionStatus.setText("● Connected to RMI Server");
            connectionStatus.setForeground(AppTheme.ACCENT);
        } else {
            connectionStatus.setText("● RMI Server not connected");
            connectionStatus.setForeground(AppTheme.DANGER);
        }
    }

    private void reconnect() {
        connectionStatus.setText("● Connecting...");
        connectionStatus.setForeground(AppTheme.WARNING);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                if (MainFrame.rmiClient == null) MainFrame.rmiClient = new RMIClient();
                return MainFrame.rmiClient.connect();
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    connectionStatus.setText(ok ? "● Connected to RMI Server" : "● Connection failed");
                    connectionStatus.setForeground(ok ? AppTheme.ACCENT : AppTheme.DANGER);
                    if (ok) showResult("STATUS", "Successfully connected to RMI server.");
                    else    showResult("STATUS", "Could not connect. Make sure the app is running.");
                } catch (Exception e) {
                    connectionStatus.setText("● Connection error");
                    connectionStatus.setForeground(AppTheme.DANGER);
                }
            }
        };
        worker.execute();
    }

    // ── RMI Queries ───────────────────────────────────────────────────────────

    private void rmiSearchBook() {
        String title = searchTitleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a title to search.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RMIClient client = MainFrame.rmiClient;
        if (client == null || !client.isConnected()) { showNotConnected(); return; }

        SwingWorker<List<Book>, Void> worker = new SwingWorker<>() {
            @Override protected List<Book> doInBackground() { return client.searchBook(title); }
            @Override protected void done() {
                try {
                    List<Book> books = get();
                    tableModel.setRowCount(0);
                    StringBuilder sb = new StringBuilder(
                        "searchBook(\"" + title + "\") — " + books.size() + " result(s):\n\n");
                    for (Book b : books) {
                        tableModel.addRow(new Object[]{
                            "Book #" + b.getBookId(),
                            b.getTitle() + " by " + b.getAuthor()
                            + " | Available: " + b.getAvailableCopies()
                        });
                        sb.append("• ").append(b.getTitle()).append(" by ").append(b.getAuthor())
                          .append(" [").append(b.getAvailableCopies()).append(" available]\n");
                    }
                    if (books.isEmpty()) sb.append("No books found.");
                    resultArea.setText(sb.toString());
                } catch (Exception e) { showResult("ERROR", e.getMessage()); }
            }
        };
        worker.execute();
    }

    private void rmiCheckAvailability() {
        String idStr = bookIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Book ID.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RMIClient client = MainFrame.rmiClient;
        if (client == null || !client.isConnected()) { showNotConnected(); return; }
        try {
            int bookId = Integer.parseInt(idStr);
            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override protected Integer doInBackground() { return client.checkAvailability(bookId); }
                @Override protected void done() {
                    try {
                        int avail = get();
                        tableModel.setRowCount(0);
                        tableModel.addRow(new Object[]{"Book ID", bookId});
                        tableModel.addRow(new Object[]{"Available Copies",
                            avail == -1 ? "Book not found" : String.valueOf(avail)});
                        resultArea.setText("checkAvailability(" + bookId + "): "
                            + (avail == -1 ? "Book not found" : avail + " copies available"));
                    } catch (Exception e) { showResult("ERROR", e.getMessage()); }
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Book ID must be a number.",
                "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void rmiGetMemberLoans() {
        String idStr = memberIdField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Member ID.",
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RMIClient client = MainFrame.rmiClient;
        if (client == null || !client.isConnected()) { showNotConnected(); return; }
        try {
            int memberId = Integer.parseInt(idStr);
            SwingWorker<List<Loan>, Void> worker = new SwingWorker<>() {
                @Override protected List<Loan> doInBackground() { return client.getMemberLoans(memberId); }
                @Override protected void done() {
                    try {
                        List<Loan> loans = get();
                        tableModel.setRowCount(0);
                        StringBuilder sb = new StringBuilder(
                            "getMemberLoans(" + memberId + ") — " + loans.size() + " loan(s):\n\n");
                        for (Loan l : loans) {
                            tableModel.addRow(new Object[]{
                                "Loan #" + l.getLoanId(),
                                l.getBookTitle() + " | Due: " + l.getDueDate() + " | " + l.getStatus()
                            });
                            sb.append("• Loan #").append(l.getLoanId()).append(": ")
                              .append(l.getBookTitle()).append(" (due ").append(l.getDueDate())
                              .append(") — ").append(l.getStatus()).append("\n");
                        }
                        if (loans.isEmpty()) sb.append("No active loans for this member.");
                        resultArea.setText(sb.toString());
                    } catch (Exception e) { showResult("ERROR", e.getMessage()); }
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Member ID must be a number.",
                "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showResult(String label, Object value) {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{label, value});
        resultArea.setText(label + ": " + value);
    }

    private void showNotConnected() {
        JOptionPane.showMessageDialog(this,
            "RMI server is not connected.\nClick 'Reconnect' and wait a moment.",
            "Not Connected", JOptionPane.WARNING_MESSAGE);
    }
}
