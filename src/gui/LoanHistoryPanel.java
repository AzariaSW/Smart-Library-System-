package gui;

import db.LoanDAO;
import model.Loan;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.RoundingMode;
import java.util.List;

/**
 * Loan History — all loans with live filter by status and keyword.
 * Always loads fresh data from DB on refresh().
 */
public class LoanHistoryPanel extends JPanel {

    private final LoanDAO loanDAO = new LoanDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JComboBox<String> statusFilter;
    private JLabel            statusLabel;
    private List<Loan>        allLoans;

    private static final String[] COLUMNS = {
        "Loan ID", "Book Title", "ISBN", "Member", "Librarian",
        "Loan Date", "Due Date", "Return Date", "Status", "Fine (Birr)", "Fine Paid"
    };

    public LoanHistoryPanel() {
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIComponents.titleLabel("📋  Loan History");
        statusLabel  = UIComponents.secondaryLabel("Loading...");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,       BorderLayout.NORTH);
        headerLeft.add(statusLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        searchField  = UIComponents.searchField("🔍  Search by book or member...");
        statusFilter = UIComponents.styledComboBox(
            new String[]{"All Status", "ACTIVE", "RETURNED", "OVERDUE"});
        statusFilter.setPreferredSize(new Dimension(140, 36));
        JButton refreshBtn = UIComponents.outlineButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> refresh());

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        statusFilter.addActionListener(e -> filterTable());

        headerRight.add(searchField);
        headerRight.add(statusFilter);
        headerRight.add(refreshBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);

        // Status column
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setForeground(switch (val != null ? val.toString() : "") {
                        case "OVERDUE"  -> AppTheme.DANGER;
                        case "ACTIVE"   -> AppTheme.WARNING;
                        case "RETURNED" -> AppTheme.ACCENT;
                        default         -> AppTheme.TEXT_SECONDARY;
                    });
                    setFont(AppTheme.FONT_TABLE.deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Fine Paid column
        table.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    boolean paid = "Yes".equals(val);
                    c.setForeground(paid ? AppTheme.ACCENT : AppTheme.TEXT_SECONDARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        int[] widths = {65, 190, 110, 130, 110, 95, 95, 95, 85, 90, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(header,                              BorderLayout.NORTH);
        add(UIComponents.styledScrollPane(table), BorderLayout.CENTER);
    }

    public void refresh() {
        statusLabel.setText("Loading...");
        DataLoaderWorker.execute(
            () -> loanDAO.getAllLoans(),
            loans -> {
                allLoans = loans;
                populateTable(loans);
                statusLabel.setText(loans.size() + " total loans");
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
            tableModel.addRow(new Object[]{
                l.getLoanId(),
                l.getBookTitle(),
                l.getBookIsbn(),
                l.getMemberName(),
                l.getLibrarianName() != null ? l.getLibrarianName() : "—",
                l.getLoanDate(),
                l.getDueDate(),
                l.getReturnDate() != null ? l.getReturnDate().toString() : "—",
                l.getStatus().name(),
                l.getFineAmount().setScale(2, RoundingMode.HALF_UP) + " Birr",
                l.isFinePaid() ? "Yes" : "No"
            });
        }
    }

    private void filterTable() {
        if (allLoans == null) return;
        String kw        = searchField.getText().trim().toLowerCase();
        String statusSel = (String) statusFilter.getSelectedItem();
        List<Loan> filtered = allLoans.stream()
            .filter(l -> {
                boolean matchKw = kw.isEmpty()
                    || l.getBookTitle().toLowerCase().contains(kw)
                    || l.getMemberName().toLowerCase().contains(kw);
                boolean matchStatus = "All Status".equals(statusSel)
                    || l.getStatus().name().equals(statusSel);
                return matchKw && matchStatus;
            })
            .toList();
        populateTable(filtered);
        statusLabel.setText(filtered.size() + " loans shown");
    }
}
