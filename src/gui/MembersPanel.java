package gui;

import db.MemberDAO;
import model.Librarian;
import model.Member;
import model.Member.MembershipType;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Members management screen — full CRUD with role-based access control.
 * ADMIN: add, edit, delete.  LIBRARIAN: add, edit only.
 */
public class MembersPanel extends JPanel {

    private final Librarian librarian;
    private final MemberDAO memberDAO = new MemberDAO();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            statusLabel;
    private JButton           deleteBtn;
    private List<Member>      currentMembers;

    private static final String[] COLUMNS = {
        "ID", "Full Name", "Email", "Phone", "Type",
        "Joined", "Expires", "Active", "Active Loans", "Max Loans"
    };

    public MembersPanel(Librarian librarian) {
        this.librarian = librarian;
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIComponents.titleLabel("👥  Manage Members");
        statusLabel  = UIComponents.secondaryLabel("Loading...");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,       BorderLayout.NORTH);
        headerLeft.add(statusLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        searchField = UIComponents.searchField("🔍  Search by name, email, phone...");
        JButton searchBtn  = UIComponents.primaryButton("Search");
        JButton addBtn     = UIComponents.successButton("+ Add Member");
        JButton refreshBtn = UIComponents.outlineButton("⟳ Refresh");

        searchBtn.addActionListener(e  -> searchMembers());
        searchField.addActionListener(e -> searchMembers());
        addBtn.addActionListener(e     -> showAddEditDialog(null));
        refreshBtn.addActionListener(e -> refresh());

        headerRight.add(searchField);
        headerRight.add(searchBtn);
        headerRight.add(addBtn);
        headerRight.add(refreshBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 8 || c == 9) return Integer.class;
                if (c == 7) return Boolean.class;
                return String.class;
            }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);

        int[] widths = {50, 160, 180, 110, 80, 100, 100, 60, 90, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel && val instanceof Integer loans) {
                    c.setForeground(loans > 0 ? AppTheme.WARNING : AppTheme.TEXT_SECONDARY);
                    setFont(loans > 0 ? AppTheme.FONT_TABLE.deriveFont(Font.BOLD) : AppTheme.FONT_TABLE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton editBtn = UIComponents.outlineButton("✏ Edit");
        deleteBtn       = UIComponents.dangerButton("🗑 Delete");
        JButton viewBtn = UIComponents.outlineButton("👁 View Loans");

        deleteBtn.setEnabled(librarian.isAdmin());
        deleteBtn.setToolTipText(librarian.isAdmin() ? "Delete selected member" : "Admin only");

        editBtn.addActionListener(e   -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.addActionListener(e   -> viewMemberLoans());

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);
        actionRow.add(viewBtn);

        if (!librarian.isAdmin()) {
            actionRow.add(UIComponents.secondaryLabel("  🔒 Delete requires Admin role"));
        }

        add(header,                              BorderLayout.NORTH);
        add(UIComponents.styledScrollPane(table), BorderLayout.CENTER);
        add(actionRow,                           BorderLayout.SOUTH);
    }

    public void refresh() {
        statusLabel.setText("Loading...");
        DataLoaderWorker.execute(
            () -> memberDAO.getAllMembers(),
            members -> {
                currentMembers = members;
                populateTable(members);
                statusLabel.setText(members.size() + " members found");
            },
            err -> {
                statusLabel.setText("Error");
                JOptionPane.showMessageDialog(this,
                    "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            },
            null
        );
    }

    private void searchMembers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { refresh(); return; }
        DataLoaderWorker.execute(
            () -> memberDAO.searchMembers(keyword),
            members -> {
                currentMembers = members;
                populateTable(members);
                statusLabel.setText(members.size() + " results");
            },
            err -> JOptionPane.showMessageDialog(this,
                "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void populateTable(List<Member> members) {
        tableModel.setRowCount(0);
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                m.getMemberId(), m.getFullName(), m.getEmail(), m.getPhone(),
                m.getMembershipType().name(),
                m.getMembershipDate() != null ? m.getMembershipDate().toString() : "",
                m.getExpiryDate()     != null ? m.getExpiryDate().toString()     : "",
                m.isActive(), m.getActiveLoans(), m.getMaxLoans()
            });
        }
    }

    private void showAddEditDialog(Member existing) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add New Member" : "Edit Member",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridwidth = 2;

        JTextField nameField     = UIComponents.styledTextField("Full name");
        JTextField emailField    = UIComponents.styledTextField("Email address");
        JTextField phoneField    = UIComponents.styledTextField("Phone number");
        JTextArea  addressArea   = UIComponents.styledTextArea();
        addressArea.setRows(2);
        JComboBox<String> typeCombo = UIComponents.styledComboBox(
            new String[]{"STUDENT", "FACULTY", "PUBLIC"});
        JTextField expiryField   = UIComponents.styledTextField("YYYY-MM-DD");
        JTextField maxLoansField = UIComponents.styledTextField("e.g. 3");
        JCheckBox  activeCheck   = new JCheckBox("Active", true);
        activeCheck.setFont(AppTheme.FONT_BODY);
        activeCheck.setOpaque(false);

        if (existing != null) {
            nameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            addressArea.setText(existing.getAddress());
            typeCombo.setSelectedItem(existing.getMembershipType().name());
            if (existing.getExpiryDate() != null)
                expiryField.setText(existing.getExpiryDate().toString());
            maxLoansField.setText(String.valueOf(existing.getMaxLoans()));
            activeCheck.setSelected(existing.isActive());
        }

        String[]     labels = {"Full Name *", "Email *", "Phone", "Address",
                                "Membership Type", "Expiry Date", "Max Loans", ""};
        JComponent[] inputs = {nameField, emailField, phoneField,
                                new JScrollPane(addressArea),
                                typeCombo, expiryField, maxLoansField, activeCheck};

        for (int i = 0; i < inputs.length; i++) {
            gbc.gridy = i * 2;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(AppTheme.FONT_BODY);
            lbl.setForeground(AppTheme.TEXT_PRIMARY);
            panel.add(lbl, gbc);
            gbc.gridy = i * 2 + 1;
            inputs[i].setPreferredSize(new Dimension(420, inputs[i] instanceof JScrollPane ? 60 : 36));
            panel.add(inputs[i], gbc);
        }

        JButton saveBtn   = UIComponents.primaryButton(existing == null ? "Add Member" : "Save Changes");
        JButton cancelBtn = UIComponents.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Name and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Member m = existing != null ? existing : new Member();
                m.setFullName(name);
                m.setEmail(email);
                m.setPhone(phoneField.getText().trim());
                m.setAddress(addressArea.getText().trim());
                m.setMembershipType(MembershipType.valueOf((String) typeCombo.getSelectedItem()));
                if (existing == null) m.setMembershipDate(LocalDate.now());
                String expiry = expiryField.getText().trim();
                m.setExpiryDate(expiry.isEmpty()
                    ? LocalDate.now().plusYears(1) : LocalDate.parse(expiry));
                m.setMaxLoans(maxLoansField.getText().isEmpty()
                    ? 3 : Integer.parseInt(maxLoansField.getText().trim()));
                m.setActive(activeCheck.isSelected());

                boolean success = existing == null
                    ? memberDAO.addMember(m) : memberDAO.updateMember(m);

                if (success) {
                    String notifMsg = existing == null
                        ? "👤 New member registered: " + name
                        : "✏ Member updated: " + name;
                    MainFrame.notificationServer.broadcast(notifMsg);
                    dialog.dispose();
                    refresh();
                    globalRefresh();
                    JOptionPane.showMessageDialog(this,
                        existing == null ? "Member added!" : "Member updated!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = labels.length * 2 + 1;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        panel.add(btnRow, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    memberId = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        Member m        = currentMembers.stream()
            .filter(x -> x.getMemberId() == memberId).findFirst().orElse(null);
        if (m != null) showAddEditDialog(m);
    }

    private void deleteSelected() {
        if (!librarian.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Only Admins can delete members.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    modelRow  = table.convertRowIndexToModel(row);
        String name      = (String) tableModel.getValueAt(modelRow, 1);
        int    memberId  = (int)    tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete member \"" + name + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (memberDAO.deleteMember(memberId)) {
                MainFrame.notificationServer.broadcast("🗑 Member deleted: " + name);
                refresh();
                globalRefresh();
                JOptionPane.showMessageDialog(this, "Member deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewMemberLoans() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.navigateTo("HISTORY", 5);
    }

    private void globalRefresh() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.refreshAll();
    }
}
