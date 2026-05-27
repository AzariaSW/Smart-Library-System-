package gui;

import db.BookDAO;
import db.LoanDAO;
import model.Librarian;
import model.Loan;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Return Book screen.
 * Business logic:
 *  - A member CAN return a book even if overdue.
 *  - Returning and paying a fine are SEPARATE actions.
 *  - On return: status=RETURNED, return_date=today, fine calculated, fine_paid stays FALSE.
 *  - Mark Fine Paid: ADMIN only. Updates fine_paid=TRUE.
 *  - View Details always fetches fresh data from DB.
 */
public class ReturnPanel extends JPanel {

    private final Librarian librarian;
    private final LoanDAO   loanDAO = new LoanDAO();
    private final BookDAO   bookDAO = new BookDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            statusLabel;
    private JButton           markFinePaidBtn;
    private List<Loan>        activeLoans;

    private static final String[] COLUMNS = {
        "Loan ID", "Book Title", "Member", "Loan Date", "Due Date", "Status", "Fine (Birr)", "Fine Paid"
    };

    public ReturnPanel(Librarian librarian) {
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
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIComponents.titleLabel("↩  Return Book");
        statusLabel  = UIComponents.secondaryLabel("Loading active loans...");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,       BorderLayout.NORTH);
        headerLeft.add(statusLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        searchField = UIComponents.searchField("🔍  Filter by member or book...");
        JButton refreshBtn = UIComponents.outlineButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        headerRight.add(searchField);
        headerRight.add(refreshBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setForeground(switch (val != null ? val.toString() : "") {
                        case "OVERDUE"  -> AppTheme.DANGER;
                        case "ACTIVE"   -> AppTheme.ACCENT;
                        default         -> AppTheme.TEXT_SECONDARY;
                    });
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
                if (!sel && val != null) {
                    boolean hasFine = !val.toString().startsWith("0.00");
                    c.setForeground(hasFine ? AppTheme.DANGER : AppTheme.TEXT_SECONDARY);
                    setFont(hasFine ? AppTheme.FONT_TABLE.deriveFont(Font.BOLD) : AppTheme.FONT_TABLE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Fine Paid column
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    boolean paid = "Yes".equals(val);
                    c.setForeground(paid ? AppTheme.ACCENT : AppTheme.TEXT_SECONDARY);
                    setFont(paid ? AppTheme.FONT_TABLE.deriveFont(Font.BOLD) : AppTheme.FONT_TABLE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        int[] widths = {70, 210, 150, 100, 100, 90, 90, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Live filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        // Action row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton returnBtn  = UIComponents.successButton("↩  Process Return");
        markFinePaidBtn    = UIComponents.warningButton("💰  Mark Fine Paid");
        JButton detailBtn  = UIComponents.outlineButton("👁  View Details");

        // Mark Fine Paid is ADMIN only
        markFinePaidBtn.setEnabled(librarian.isAdmin());
        markFinePaidBtn.setToolTipText(librarian.isAdmin() ? "Mark fine as paid" : "Admin only");

        returnBtn.addActionListener(e     -> processReturn());
        markFinePaidBtn.addActionListener(e -> markFinePaid());
        detailBtn.addActionListener(e     -> viewDetails());

        actionRow.add(returnBtn);
        actionRow.add(markFinePaidBtn);
        actionRow.add(detailBtn);
        if (!librarian.isAdmin()) {
            actionRow.add(UIComponents.secondaryLabel("  🔒 Mark Fine Paid requires Admin"));
        }

        add(header,                              BorderLayout.NORTH);
        add(UIComponents.styledScrollPane(table), BorderLayout.CENTER);
        add(actionRow,                           BorderLayout.SOUTH);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    public void refresh() {
        statusLabel.setText("Loading...");
        DataLoaderWorker.execute(
            () -> loanDAO.getActiveLoans(),
            loans -> {
                activeLoans = loans;
                populateTable(loans);
                statusLabel.setText(loans.size() + " active / overdue loans");
            },
            err -> {
                statusLabel.setText("Error loading loans");
                JOptionPane.showMessageDialog(this,
                    "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            },
            null
        );
    }

    private void populateTable(List<Loan> loans) {
        tableModel.setRowCount(0);
        for (Loan l : loans) {
            BigDecimal fine = l.calculateFine();
            tableModel.addRow(new Object[]{
                l.getLoanId(),
                l.getBookTitle(),
                l.getMemberName(),
                l.getLoanDate(),
                l.getDueDate(),
                l.isOverdue() ? "OVERDUE" : l.getStatus().name(),
                fine.setScale(2, RoundingMode.HALF_UP) + " Birr",
                l.isFinePaid() ? "Yes" : "No"
            });
        }
    }

    private void filterTable() {
        String kw = searchField.getText().trim().toLowerCase();
        if (activeLoans == null) return;
        List<Loan> filtered = activeLoans.stream()
            .filter(l -> kw.isEmpty()
                || l.getBookTitle().toLowerCase().contains(kw)
                || l.getMemberName().toLowerCase().contains(kw))
            .toList();
        populateTable(filtered);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void processReturn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan to return.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int loanId = (int) tableModel.getValueAt(row, 0);

        // Always fetch fresh from DB
        DataLoaderWorker.execute(
            () -> loanDAO.getLoanById(loanId),
            loan -> {
                if (loan == null) { refresh(); return; }

                BigDecimal fine    = loan.calculateFine();
                String     fineStr = fine.setScale(2, RoundingMode.HALF_UP) + " Birr";

                String msg = String.format(
                    "Process return?\n\nBook   : %s\nMember : %s\nFine   : %s%s",
                    loan.getBookTitle(), loan.getMemberName(), fineStr,
                    fine.compareTo(BigDecimal.ZERO) > 0
                        ? "\n\n⚠ Fine will be recorded but NOT marked paid.\nUse 'Mark Fine Paid' separately." : "");

                int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Return",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;

                DataLoaderWorker.execute(
                    () -> {
                        boolean ok = loanDAO.returnLoan(loanId, fine);
                        if (ok) bookDAO.incrementAvailableCopies(loan.getBookId());
                        return ok;
                    },
                    success -> {
                        if (success) {
                            MainFrame.notificationServer.broadcast(
                                "↩ Book returned: \"" + loan.getBookTitle()
                                + "\" by " + loan.getMemberName()
                                + (fine.compareTo(BigDecimal.ZERO) > 0
                                    ? " | Fine: " + fineStr : ""));
                            refresh();
                            globalRefresh();
                            JOptionPane.showMessageDialog(this,
                                "Book returned successfully!"
                                + (fine.compareTo(BigDecimal.ZERO) > 0
                                    ? "\nFine of " + fineStr + " recorded (unpaid)." : ""),
                                "Return Processed", JOptionPane.INFORMATION_MESSAGE);
                        }
                    },
                    err -> JOptionPane.showMessageDialog(this,
                        "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
                    null
                );
            },
            err -> JOptionPane.showMessageDialog(this,
                "Error fetching loan: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void markFinePaid() {
        if (!librarian.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Only Admins can mark fines as paid.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    loanId    = (int)    tableModel.getValueAt(row, 0);
        String memberName= (String) tableModel.getValueAt(row, 2);
        String fineStr   = (String) tableModel.getValueAt(row, 6);

        DataLoaderWorker.execute(
            () -> loanDAO.markFinePaid(loanId),
            success -> {
                if (success) {
                    MainFrame.notificationServer.broadcast(
                        "💜 Fine paid: " + memberName + " — " + fineStr);
                    refresh();
                    globalRefresh();
                    JOptionPane.showMessageDialog(this,
                        "Fine marked as paid.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            },
            err -> JOptionPane.showMessageDialog(this,
                "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int loanId = (int) tableModel.getValueAt(row, 0);

        // Always fetch fresh from DB
        DataLoaderWorker.execute(
            () -> loanDAO.getLoanById(loanId),
            loan -> {
                if (loan == null) return;
                BigDecimal fine = loan.calculateFine();
                String info = String.format(
                    "<html><b>Loan ID:</b> %d<br><b>Book:</b> %s<br><b>ISBN:</b> %s<br>"
                    + "<b>Member:</b> %s<br><b>Librarian:</b> %s<br>"
                    + "<b>Loan Date:</b> %s<br><b>Due Date:</b> %s<br>"
                    + "<b>Return Date:</b> %s<br><b>Status:</b> %s<br>"
                    + "<b>Fine:</b> %s Birr<br><b>Fine Paid:</b> %s</html>",
                    loan.getLoanId(), loan.getBookTitle(), loan.getBookIsbn(),
                    loan.getMemberName(), loan.getLibrarianName() != null ? loan.getLibrarianName() : "—",
                    loan.getLoanDate(), loan.getDueDate(),
                    loan.getReturnDate() != null ? loan.getReturnDate().toString() : "Not returned",
                    loan.getStatus(),
                    fine.setScale(2, RoundingMode.HALF_UP),
                    loan.isFinePaid() ? "Yes ✔" : "No ✗"
                );
                JOptionPane.showMessageDialog(this, info, "Loan Details", JOptionPane.INFORMATION_MESSAGE);
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
