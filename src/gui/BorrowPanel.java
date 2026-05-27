package gui;

import db.BookDAO;
import db.LoanDAO;
import db.MemberDAO;
import model.Book;
import model.Librarian;
import model.Loan;
import model.Member;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Borrow Book screen.
 * Layout (top-to-bottom inside each card):
 *   Title label
 *   Search field + button (side by side)
 *   Results table (fills remaining space)
 *   Selection status label
 *
 * Bottom bar: loan period spinner + Issue Loan button + status label.
 */
public class BorrowPanel extends JPanel {

    private final Librarian librarian;
    private final BookDAO   bookDAO   = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final LoanDAO   loanDAO   = new LoanDAO();

    // Member side
    private JTextField        memberSearchField;
    private JTable            memberTable;
    private DefaultTableModel memberModel;
    private JLabel            selectedMemberLabel;
    private Member            selectedMember;

    // Book side
    private JTextField        bookSearchField;
    private JTable            bookTable;
    private DefaultTableModel bookModel;
    private JLabel            selectedBookLabel;
    private Book              selectedBook;

    // Bottom
    private JSpinner loanDaysSpinner;
    private JButton  borrowBtn;
    private JLabel   statusLabel;

    public BorrowPanel(Librarian librarian) {
        this.librarian = librarian;
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
    }

    private void initComponents() {

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIComponents.titleLabel("➕  Borrow Book"), BorderLayout.WEST);

        // ── Two-column content area ─────────────────────────────────────────────
        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setOpaque(false);

        content.add(buildMemberCard());
        content.add(buildBookCard());

        // ── Bottom bar ─────────────────────────────────────────────────────────
        JPanel bottomCard = UIComponents.cardPanel();
        bottomCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bottomCard.setPreferredSize(new Dimension(0, 64));

        JLabel daysLabel = UIComponents.bodyLabel("Loan Period (days):");
        loanDaysSpinner  = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        loanDaysSpinner.setPreferredSize(new Dimension(80, 34));
        loanDaysSpinner.setFont(AppTheme.FONT_BODY);

        borrowBtn = UIComponents.successButton("✔  Issue Loan");
        borrowBtn.setPreferredSize(new Dimension(160, 38));
        borrowBtn.addActionListener(e -> issueLoan());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppTheme.FONT_BODY);

        bottomCard.add(daysLabel);
        bottomCard.add(loanDaysSpinner);
        bottomCard.add(Box.createHorizontalStrut(8));
        bottomCard.add(borrowBtn);
        bottomCard.add(Box.createHorizontalStrut(8));
        bottomCard.add(statusLabel);

        add(header,     BorderLayout.NORTH);
        add(content,    BorderLayout.CENTER);
        add(bottomCard, BorderLayout.SOUTH);
    }

    // ── Card builders ─────────────────────────────────────────────────────────

    private JPanel buildMemberCard() {
        // Use a plain JPanel with BorderLayout so the table fills the center
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIComponents.RoundedBorder(AppTheme.BORDER, AppTheme.CORNER_RADIUS),
            BorderFactory.createEmptyBorder(16, 16, 12, 16)
        ));

        // Title
        JLabel title = UIComponents.subtitleLabel("1. Select Member");

        // Search row
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        memberSearchField = UIComponents.styledTextField("Search by name or email...");
        JButton searchBtn = UIComponents.primaryButton("Search");
        searchBtn.addActionListener(e -> searchMembers());
        memberSearchField.addActionListener(e -> searchMembers());
        searchRow.add(memberSearchField, BorderLayout.CENTER);
        searchRow.add(searchBtn,         BorderLayout.EAST);

        // Table
        memberModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Type", "Loans"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(memberModel);
        UIComponents.styleTable(memberTable);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMemberSelected();
        });
        int[] mw = {40, 140, 150, 70, 50};
        for (int i = 0; i < mw.length; i++)
            memberTable.getColumnModel().getColumn(i).setPreferredWidth(mw[i]);

        JScrollPane tableScroll = UIComponents.styledScrollPane(memberTable);

        // Selection label
        selectedMemberLabel = new JLabel("No member selected");
        selectedMemberLabel.setFont(AppTheme.FONT_SMALL);
        selectedMemberLabel.setForeground(AppTheme.TEXT_SECONDARY);

        // Top section: title + search stacked
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(title);
        topSection.add(Box.createVerticalStrut(8));
        topSection.add(searchRow);

        card.add(topSection,    BorderLayout.NORTH);
        card.add(tableScroll,   BorderLayout.CENTER);
        card.add(selectedMemberLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBookCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIComponents.RoundedBorder(AppTheme.BORDER, AppTheme.CORNER_RADIUS),
            BorderFactory.createEmptyBorder(16, 16, 12, 16)
        ));

        // Title
        JLabel title = UIComponents.subtitleLabel("2. Select Book");

        // Search row
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        bookSearchField = UIComponents.styledTextField("Search by title, author, or ISBN...");
        JButton searchBtn = UIComponents.primaryButton("Search");
        searchBtn.addActionListener(e -> searchBooks());
        bookSearchField.addActionListener(e -> searchBooks());
        searchRow.add(bookSearchField, BorderLayout.CENTER);
        searchRow.add(searchBtn,       BorderLayout.EAST);

        // Table
        bookModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "Available"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = new JTable(bookModel);
        UIComponents.styleTable(bookTable);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onBookSelected();
        });
        // Color available copies column
        bookTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel && val instanceof Integer avail) {
                    c.setForeground(avail == 0 ? AppTheme.DANGER :
                                    avail == 1 ? AppTheme.WARNING : AppTheme.ACCENT);
                    setFont(AppTheme.FONT_TABLE.deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        int[] bw = {40, 190, 130, 75};
        for (int i = 0; i < bw.length; i++)
            bookTable.getColumnModel().getColumn(i).setPreferredWidth(bw[i]);

        JScrollPane tableScroll = UIComponents.styledScrollPane(bookTable);

        // Selection label
        selectedBookLabel = new JLabel("No book selected");
        selectedBookLabel.setFont(AppTheme.FONT_SMALL);
        selectedBookLabel.setForeground(AppTheme.TEXT_SECONDARY);

        // Top section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(title);
        topSection.add(Box.createVerticalStrut(8));
        topSection.add(searchRow);

        card.add(topSection,   BorderLayout.NORTH);
        card.add(tableScroll,  BorderLayout.CENTER);
        card.add(selectedBookLabel, BorderLayout.SOUTH);
        return card;
    }

    // ── Public refresh ────────────────────────────────────────────────────────

    public void refresh() {
        selectedMember = null;
        selectedBook   = null;
        selectedMemberLabel.setText("No member selected");
        selectedMemberLabel.setForeground(AppTheme.TEXT_SECONDARY);
        selectedBookLabel.setText("No book selected");
        selectedBookLabel.setForeground(AppTheme.TEXT_SECONDARY);
        statusLabel.setText(" ");
        memberModel.setRowCount(0);
        bookModel.setRowCount(0);
        memberSearchField.setText("");
        bookSearchField.setText("");
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void searchMembers() {
        String kw = memberSearchField.getText().trim();
        if (kw.isEmpty()) {
            showStatus("Enter a name or email to search.", AppTheme.WARNING);
            return;
        }
        DataLoaderWorker.execute(
            () -> memberDAO.searchMembers(kw),
            members -> {
                memberModel.setRowCount(0);
                for (Member m : members) {
                    memberModel.addRow(new Object[]{
                        m.getMemberId(), m.getFullName(), m.getEmail(),
                        m.getMembershipType().name(),
                        m.getActiveLoans() + "/" + m.getMaxLoans()
                    });
                }
                if (members.isEmpty())
                    showStatus("No members found for \"" + kw + "\".", AppTheme.TEXT_SECONDARY);
                else
                    statusLabel.setText(" ");
            },
            err -> JOptionPane.showMessageDialog(this,
                "Search error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void searchBooks() {
        String kw = bookSearchField.getText().trim();
        if (kw.isEmpty()) {
            showStatus("Enter a title, author, or ISBN to search.", AppTheme.WARNING);
            return;
        }
        DataLoaderWorker.execute(
            () -> bookDAO.searchBooks(kw),
            books -> {
                bookModel.setRowCount(0);
                for (Book b : books) {
                    bookModel.addRow(new Object[]{
                        b.getBookId(), b.getTitle(), b.getAuthor(), b.getAvailableCopies()
                    });
                }
                if (books.isEmpty())
                    showStatus("No books found for \"" + kw + "\".", AppTheme.TEXT_SECONDARY);
                else
                    statusLabel.setText(" ");
            },
            err -> JOptionPane.showMessageDialog(this,
                "Search error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    // ── Selection handlers ────────────────────────────────────────────────────

    private void onMemberSelected() {
        int row = memberTable.getSelectedRow();
        if (row < 0) {
            selectedMember = null;
            selectedMemberLabel.setText("No member selected");
            selectedMemberLabel.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }
        int memberId = (int) memberModel.getValueAt(row, 0);
        DataLoaderWorker.execute(
            () -> memberDAO.getMemberById(memberId),
            m -> {
                selectedMember = m;
                if (m != null) {
                    boolean ok = m.isActive() && m.canBorrow();
                    selectedMemberLabel.setText(
                        "✔ Selected: " + m.getFullName()
                        + "  |  Loans: " + m.getActiveLoans() + "/" + m.getMaxLoans()
                        + (m.isActive() ? "" : "  [INACTIVE]"));
                    selectedMemberLabel.setForeground(ok ? AppTheme.ACCENT : AppTheme.DANGER);
                }
            },
            err -> {}, null
        );
    }

    private void onBookSelected() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            selectedBook = null;
            selectedBookLabel.setText("No book selected");
            selectedBookLabel.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }
        int bookId = (int) bookModel.getValueAt(row, 0);
        DataLoaderWorker.execute(
            () -> bookDAO.getBookById(bookId),
            b -> {
                selectedBook = b;
                if (b != null) {
                    selectedBookLabel.setText(
                        "✔ Selected: " + b.getTitle()
                        + "  |  Available: " + b.getAvailableCopies());
                    selectedBookLabel.setForeground(
                        b.isAvailable() ? AppTheme.ACCENT : AppTheme.DANGER);
                }
            },
            err -> {}, null
        );
    }

    // ── Issue loan ────────────────────────────────────────────────────────────

    private void issueLoan() {
        if (selectedMember == null) { showStatus("Please select a member.", AppTheme.DANGER); return; }
        if (selectedBook   == null) { showStatus("Please select a book.",   AppTheme.DANGER); return; }
        if (!selectedMember.isActive()) {
            showStatus("Member account is inactive.", AppTheme.DANGER); return;
        }
        if (!selectedMember.canBorrow()) {
            showStatus("Member has reached their loan limit (" + selectedMember.getMaxLoans() + ").",
                AppTheme.DANGER);
            return;
        }
        if (!selectedBook.isAvailable()) {
            showStatus("No copies available for this book.", AppTheme.DANGER); return;
        }

        int       days    = (int) loanDaysSpinner.getValue();
        LocalDate dueDate = LocalDate.now().plusDays(days);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Issue loan?\n\nMember : %s\nBook   : %s\nDue    : %s",
                selectedMember.getFullName(), selectedBook.getTitle(), dueDate),
            "Confirm Loan", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        borrowBtn.setEnabled(false);
        final String memberName = selectedMember.getFullName();
        final String bookTitle  = selectedBook.getTitle();
        final int    bookId     = selectedBook.getBookId();
        final int    memberId   = selectedMember.getMemberId();

        DataLoaderWorker.execute(
            () -> {
                Loan loan = new Loan(bookId, memberId,
                    librarian.getLibrarianId(), LocalDate.now(), dueDate);
                int loanId = loanDAO.addLoan(loan);
                if (loanId > 0) bookDAO.decrementAvailableCopies(bookId);
                return loanId;
            },
            loanId -> {
                borrowBtn.setEnabled(true);
                if (loanId > 0) {
                    MainFrame.notificationServer.broadcast(
                        "📋 Book borrowed: \"" + bookTitle + "\" by " + memberName
                        + " (due " + dueDate + ")");
                    showStatus("✔ Loan issued! Loan ID: " + loanId, AppTheme.ACCENT);
                    globalRefresh();
                    refresh();
                } else {
                    showStatus("Failed to issue loan. Please try again.", AppTheme.DANGER);
                }
            },
            err -> {
                borrowBtn.setEnabled(true);
                showStatus("Error: " + err.getMessage(), AppTheme.DANGER);
            },
            null
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void globalRefresh() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.refreshAll();
    }
}
