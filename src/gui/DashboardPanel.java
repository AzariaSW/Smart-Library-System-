package gui;

import db.BookDAO;
import db.LoanDAO;
import db.MemberDAO;
import model.Librarian;
import threads.DataLoaderWorker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard — live statistics, quick actions, system info.
 */
public class DashboardPanel extends JPanel {

    private final Librarian librarian;
    private JLabel totalBooksVal, availableBooksVal, totalMembersVal,
                   activeLoansVal, overdueLoansVal;

    private final BookDAO   bookDAO   = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final LoanDAO   loanDAO   = new LoanDAO();

    public DashboardPanel(Librarian librarian) {
        this.librarian = librarian;
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + librarian.getFullName() + " 👋");
        welcomeLabel.setFont(AppTheme.FONT_TITLE);
        welcomeLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel dateLabel = new JLabel(LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(AppTheme.FONT_BODY);
        dateLabel.setForeground(AppTheme.TEXT_SECONDARY);

        headerLeft.add(welcomeLabel);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(dateLabel);

        JButton refreshBtn = UIComponents.outlineButton("⟳  Refresh");
        refreshBtn.addActionListener(e -> refresh());

        header.add(headerLeft, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        // Stats grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(0, 130));

        totalBooksVal    = new JLabel("—");
        availableBooksVal= new JLabel("—");
        totalMembersVal  = new JLabel("—");
        activeLoansVal   = new JLabel("—");
        overdueLoansVal  = new JLabel("—");

        statsGrid.add(buildStatCard("📚", "Total Books",   totalBooksVal,    AppTheme.PRIMARY,  AppTheme.PRIMARY_LIGHT));
        statsGrid.add(buildStatCard("✅", "Available",     availableBooksVal, AppTheme.ACCENT,  AppTheme.SUCCESS_LIGHT));
        statsGrid.add(buildStatCard("👥", "Members",       totalMembersVal,  AppTheme.PURPLE,   AppTheme.PURPLE_LIGHT));
        statsGrid.add(buildStatCard("📋", "Active Loans",  activeLoansVal,   AppTheme.WARNING,  AppTheme.WARNING_LIGHT));
        statsGrid.add(buildStatCard("⚠",  "Overdue",       overdueLoansVal,  AppTheme.DANGER,   new Color(254, 226, 226)));

        // Quick actions
        JPanel actionsCard = UIComponents.cardPanel();
        actionsCard.setLayout(new BorderLayout());
        JLabel actionsTitle = UIComponents.subtitleLabel("Quick Actions");
        actionsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsRow.setOpaque(false);

        JButton borrowBtn    = UIComponents.primaryButton("➕  Borrow Book");
        JButton returnBtn    = UIComponents.successButton("↩  Return Book");
        JButton addBookBtn   = UIComponents.outlineButton("📖  Add Book");
        JButton addMemberBtn = UIComponents.outlineButton("👤  Add Member");

        borrowBtn.addActionListener(e    -> navigateTo("BORROW",  3));
        returnBtn.addActionListener(e    -> navigateTo("RETURN",  4));
        addBookBtn.addActionListener(e   -> navigateTo("BOOKS",   1));
        addMemberBtn.addActionListener(e -> navigateTo("MEMBERS", 2));

        actionsRow.add(borrowBtn);
        actionsRow.add(returnBtn);
        actionsRow.add(addBookBtn);
        actionsRow.add(addMemberBtn);

        actionsCard.add(actionsTitle, BorderLayout.NORTH);
        actionsCard.add(actionsRow,   BorderLayout.CENTER);

        // System info
        JPanel infoCard = UIComponents.cardPanel();
        infoCard.setLayout(new BorderLayout());
        JLabel infoTitle = UIComponents.subtitleLabel("System Information");
        infoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel infoGrid = new JPanel(new GridLayout(2, 3, 16, 8));
        infoGrid.setOpaque(false);
        infoGrid.add(buildInfoItem("Fine Rate",            "5.00 Birr / day"));
        infoGrid.add(buildInfoItem("Loan Period",          "14 days"));
        infoGrid.add(buildInfoItem("Max Loans (Student)",  "3 books"));
        infoGrid.add(buildInfoItem("Max Loans (Faculty)",  "5 books"));
        infoGrid.add(buildInfoItem("Max Loans (Public)",   "2 books"));
        infoGrid.add(buildInfoItem("Logged in as",         librarian.getRole().name()));

        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(infoGrid,  BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(actionsCard);
        bottomRow.add(infoCard);

        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.setOpaque(false);
        southWrapper.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        southWrapper.add(bottomRow, BorderLayout.CENTER);

        add(header,       BorderLayout.NORTH);
        add(statsGrid,    BorderLayout.CENTER);
        add(southWrapper, BorderLayout.SOUTH);
    }

    private JPanel buildStatCard(String icon, String label, JLabel valueLabel,
                                  Color accent, Color bg) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight()-3, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        valueLabel.setFont(AppTheme.FONT_STAT_NUM);
        valueLabel.setForeground(accent);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(AppTheme.FONT_STAT_LABEL);
        labelLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(labelLbl);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInfoItem(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(AppTheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        DataLoaderWorker.execute(
            () -> new int[]{
                bookDAO.getTotalBooks(),
                bookDAO.getTotalAvailableCopies(),
                memberDAO.getTotalMembers(),
                loanDAO.getActiveLoansCount(),
                loanDAO.getOverdueLoansCount()
            },
            stats -> {
                totalBooksVal.setText(String.valueOf(stats[0]));
                availableBooksVal.setText(String.valueOf(stats[1]));
                totalMembersVal.setText(String.valueOf(stats[2]));
                activeLoansVal.setText(String.valueOf(stats[3]));
                overdueLoansVal.setText(String.valueOf(stats[4]));
            },
            err -> JOptionPane.showMessageDialog(this,
                "Error loading stats: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void navigateTo(String card, int idx) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.navigateTo(card, idx);
    }
}
