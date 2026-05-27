package gui;

import db.LoanDAO;
import model.Loan;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Overdue Books screen — shows all overdue loans with fine totals.
 * Always loads fresh data from DB.
 */
public class OverduePanel extends JPanel {

    private final LoanDAO loanDAO = new LoanDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            statusLabel;
    private JLabel            totalFineLabel;
    private List<Loan>        overdueLoans;

    private static final String[] COLUMNS = {
        "Loan ID", "Book Title", "Member", "Loan Date", "Due Date", "Days Overdue", "Fine (Birr)"
    };

    public OverduePanel() {
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

        JLabel title = UIComponents.titleLabel("⚠  Overdue Books");
        statusLabel  = UIComponents.secondaryLabel("Loading...");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,       BorderLayout.NORTH);
        headerLeft.add(statusLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JButton updateBtn  = UIComponents.warningButton("⟳ Update Statuses");
        JButton refreshBtn = UIComponents.outlineButton("⟳ Refresh");
        updateBtn.addActionListener(e  -> updateOverdueStatuses());
        refreshBtn.addActionListener(e -> refresh());

        headerRight.add(updateBtn);
        headerRight.add(refreshBtn);
        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Alert banner
        JPanel alertBanner = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 226, 226));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(AppTheme.DANGER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        alertBanner.setOpaque(false);
        alertBanner.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        alertBanner.setPreferredSize(new Dimension(0, 48));

        JLabel alertIcon = new JLabel("⚠");
        alertIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        alertIcon.setForeground(AppTheme.DANGER);

        totalFineLabel = new JLabel("Total outstanding fines: 0.00 Birr");
        totalFineLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalFineLabel.setForeground(AppTheme.DANGER);

        alertBanner.add(alertIcon,     BorderLayout.WEST);
        alertBanner.add(totalFineLabel, BorderLayout.CENTER);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 5 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);

        // Days overdue
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel && val instanceof Integer days) {
                    c.setForeground(days > 14 ? new Color(185, 28, 28) : AppTheme.DANGER);
                    setFont(AppTheme.FONT_TABLE.deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Fine column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setForeground(AppTheme.DANGER);
                    setFont(AppTheme.FONT_TABLE.deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        int[] widths = {70, 220, 160, 100, 100, 110, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(alertBanner,                          BorderLayout.NORTH);
        centerPanel.add(UIComponents.styledScrollPane(table), BorderLayout.CENTER);

        add(header,      BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        statusLabel.setText("Loading...");
        DataLoaderWorker.execute(
            () -> loanDAO.getOverdueLoans(),
            loans -> {
                overdueLoans = loans;
                populateTable(loans);
                statusLabel.setText(loans.size() + " overdue loans");
                BigDecimal total = loans.stream()
                    .map(Loan::calculateFine)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalFineLabel.setText("Total outstanding fines: "
                    + total.setScale(2, RoundingMode.HALF_UP) + " Birr");
            },
            err -> {
                statusLabel.setText("Error");
                JOptionPane.showMessageDialog(this,
                    "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            },
            null
        );
    }

    private void populateTable(List<Loan> loans) {
        tableModel.setRowCount(0);
        for (Loan l : loans) {
            long daysOverdue = ChronoUnit.DAYS.between(l.getDueDate(), LocalDate.now());
            BigDecimal fine  = l.calculateFine();
            tableModel.addRow(new Object[]{
                l.getLoanId(),
                l.getBookTitle(),
                l.getMemberName(),
                l.getLoanDate(),
                l.getDueDate(),
                (int) Math.max(0, daysOverdue),
                fine.setScale(2, RoundingMode.HALF_UP) + " Birr"
            });
        }
    }

    private void updateOverdueStatuses() {
        DataLoaderWorker.execute(
            () -> loanDAO.updateOverdueStatuses(),
            count -> {
                if (count > 0) {
                    MainFrame.notificationServer.broadcast(
                        "⚠ OVERDUE ALERT: " + count + " loan(s) are now overdue.");
                }
                refresh();
                globalRefresh();
                JOptionPane.showMessageDialog(this,
                    count + " loan(s) updated to OVERDUE status.",
                    "Update Complete", JOptionPane.INFORMATION_MESSAGE);
            },
            err -> JOptionPane.showMessageDialog(this,
                "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void globalRefresh() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.refreshAll();
    }
}
