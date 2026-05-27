package gui;

import db.BookDAO;
import db.CategoryDAO;
import model.Book;
import model.Category;
import model.Librarian;
import threads.DataLoaderWorker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Books management screen — full CRUD with role-based access control.
 * ADMIN: add, edit, delete.  LIBRARIAN: add, edit only.
 */
public class BooksPanel extends JPanel {

    private final Librarian librarian;
    private final BookDAO     bookDAO     = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         searchField;
    private JLabel             statusLabel;
    private JButton            deleteBtn;
    private List<Book>         currentBooks;
    private List<Category>     categories;

    private static final String[] COLUMNS = {
        "ID", "ISBN", "Title", "Author", "Publisher", "Year",
        "Category", "Total", "Available", "Shelf"
    };

    public BooksPanel(Librarian librarian) {
        this.librarian = librarian;
        setBackground(AppTheme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        initComponents();
        loadCategories();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIComponents.titleLabel("📖  Manage Books");
        statusLabel  = UIComponents.secondaryLabel("Loading...");

        JPanel headerLeft = new JPanel(new BorderLayout(0, 4));
        headerLeft.setOpaque(false);
        headerLeft.add(title,       BorderLayout.NORTH);
        headerLeft.add(statusLabel, BorderLayout.SOUTH);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        searchField = UIComponents.searchField("🔍  Search by title, author, ISBN...");
        JButton searchBtn  = UIComponents.primaryButton("Search");
        JButton addBtn     = UIComponents.successButton("+ Add Book");
        JButton refreshBtn = UIComponents.outlineButton("⟳ Refresh");

        searchBtn.addActionListener(e  -> searchBooks());
        searchField.addActionListener(e -> searchBooks());
        addBtn.addActionListener(e     -> showAddEditDialog(null));
        refreshBtn.addActionListener(e -> refresh());

        headerRight.add(searchField);
        headerRight.add(searchBtn);
        headerRight.add(addBtn);
        headerRight.add(refreshBtn);

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 5 || c == 7 || c == 8) ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);

        int[] widths = {50, 130, 220, 160, 130, 60, 110, 60, 80, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Available copies coloring
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel && val instanceof Integer avail) {
                    c.setForeground(avail == 0 ? AppTheme.DANGER :
                                    avail <= 1 ? AppTheme.WARNING : AppTheme.ACCENT);
                    setFont(AppTheme.FONT_TABLE.deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Action row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton editBtn   = UIComponents.outlineButton("✏ Edit");
        deleteBtn         = UIComponents.dangerButton("🗑 Delete");
        JButton viewBtn   = UIComponents.outlineButton("👁 View Details");

        // Role-based: only ADMIN can delete
        deleteBtn.setEnabled(librarian.isAdmin());
        deleteBtn.setToolTipText(librarian.isAdmin() ? "Delete selected book" : "Admin only");

        editBtn.addActionListener(e   -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.addActionListener(e   -> viewSelected());

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);
        actionRow.add(viewBtn);

        if (!librarian.isAdmin()) {
            JLabel roleNote = UIComponents.secondaryLabel("  🔒 Delete requires Admin role");
            actionRow.add(roleNote);
        }

        add(header,                              BorderLayout.NORTH);
        add(UIComponents.styledScrollPane(table), BorderLayout.CENTER);
        add(actionRow,                           BorderLayout.SOUTH);
    }

    private void loadCategories() {
        DataLoaderWorker.execute(
            () -> categoryDAO.getAllCategories(),
            cats -> this.categories = cats,
            err  -> {}, null
        );
    }

    public void refresh() {
        statusLabel.setText("Loading...");
        loadCategories();
        DataLoaderWorker.execute(
            () -> bookDAO.getAllBooks(),
            books -> {
                currentBooks = books;
                populateTable(books);
                statusLabel.setText(books.size() + " books found");
            },
            err -> {
                statusLabel.setText("Error loading books");
                JOptionPane.showMessageDialog(this,
                    "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            },
            null
        );
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { refresh(); return; }
        statusLabel.setText("Searching...");
        DataLoaderWorker.execute(
            () -> bookDAO.searchBooks(keyword),
            books -> {
                currentBooks = books;
                populateTable(books);
                statusLabel.setText(books.size() + " results for \"" + keyword + "\"");
            },
            err -> JOptionPane.showMessageDialog(this,
                "Search error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE),
            null
        );
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                b.getBookId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                b.getPublisher(), b.getPublishYear(), b.getCategoryName(),
                b.getTotalCopies(), b.getAvailableCopies(), b.getShelfLocation()
            });
        }
    }

    private void showAddEditDialog(Book existing) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add New Book" : "Edit Book",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 580);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridwidth = 2;

        JTextField isbnField      = UIComponents.styledTextField("e.g. 978-0-13-468599-1");
        JTextField titleField     = UIComponents.styledTextField("Book title");
        JTextField authorField    = UIComponents.styledTextField("Author name");
        JTextField publisherField = UIComponents.styledTextField("Publisher");
        JTextField yearField      = UIComponents.styledTextField("e.g. 2023");
        JTextField copiesField    = UIComponents.styledTextField("Number of copies");
        JTextField shelfField     = UIComponents.styledTextField("e.g. PR-A1");
        JTextArea  descArea       = UIComponents.styledTextArea();
        descArea.setRows(3);

        // Category combo — built from DB
        String[] catNames = {"-- Select Category --"};
        if (categories != null && !categories.isEmpty()) {
            catNames = new String[categories.size() + 1];
            catNames[0] = "-- Select Category --";
            for (int i = 0; i < categories.size(); i++) catNames[i + 1] = categories.get(i).getName();
        }
        JComboBox<String> categoryCombo = UIComponents.styledComboBox(catNames);

        if (existing != null) {
            isbnField.setText(existing.getIsbn());
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            publisherField.setText(existing.getPublisher());
            yearField.setText(String.valueOf(existing.getPublishYear()));
            copiesField.setText(String.valueOf(existing.getTotalCopies()));
            shelfField.setText(existing.getShelfLocation());
            descArea.setText(existing.getDescription());
            if (categories != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCategoryId() == existing.getCategoryId()) {
                        categoryCombo.setSelectedIndex(i + 1);
                        break;
                    }
                }
            }
        }

        String[]     fieldLabels = {"ISBN *", "Title *", "Author *", "Publisher",
                                     "Year", "Category", "Total Copies", "Shelf Location", "Description"};
        JComponent[] inputs      = {isbnField, titleField, authorField, publisherField,
                                     yearField, categoryCombo, copiesField, shelfField,
                                     new JScrollPane(descArea)};

        for (int i = 0; i < inputs.length; i++) {
            gbc.gridy = i * 2;
            JLabel lbl = new JLabel(fieldLabels[i]);
            lbl.setFont(AppTheme.FONT_BODY);
            lbl.setForeground(AppTheme.TEXT_PRIMARY);
            panel.add(lbl, gbc);
            gbc.gridy = i * 2 + 1;
            inputs[i].setPreferredSize(new Dimension(460, inputs[i] instanceof JScrollPane ? 70 : 36));
            panel.add(inputs[i], gbc);
        }

        JButton saveBtn   = UIComponents.primaryButton(existing == null ? "Add Book" : "Save Changes");
        JButton cancelBtn = UIComponents.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String isbn   = isbnField.getText().trim();
            String title  = titleField.getText().trim();
            String author = authorField.getText().trim();
            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "ISBN, Title, and Author are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Book book = existing != null ? existing : new Book();
                book.setIsbn(isbn);
                book.setTitle(title);
                book.setAuthor(author);
                book.setPublisher(publisherField.getText().trim());
                book.setPublishYear(yearField.getText().isEmpty() ? 0
                    : Integer.parseInt(yearField.getText().trim()));
                int copies = copiesField.getText().isEmpty() ? 1
                    : Integer.parseInt(copiesField.getText().trim());
                book.setTotalCopies(copies);
                if (existing == null) book.setAvailableCopies(copies);
                book.setShelfLocation(shelfField.getText().trim());
                book.setDescription(descArea.getText().trim());
                int catIdx = categoryCombo.getSelectedIndex();
                if (catIdx > 0 && categories != null)
                    book.setCategoryId(categories.get(catIdx - 1).getCategoryId());

                boolean success = existing == null
                    ? bookDAO.addBook(book) : bookDAO.updateBook(book);

                if (success) {
                    String notifMsg = existing == null
                        ? "📖 New book added: \"" + title + "\" by " + author
                        : "✏ Book updated: \"" + title + "\" by " + author;
                    MainFrame.notificationServer.broadcast(notifMsg);
                    dialog.dispose();
                    refresh();
                    globalRefresh();
                    JOptionPane.showMessageDialog(this,
                        existing == null ? "Book added successfully!" : "Book updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Year and Copies must be valid numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = inputs.length * 2 + 1;
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
            JOptionPane.showMessageDialog(this, "Please select a book to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        Book book = currentBooks.stream()
            .filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book != null) showAddEditDialog(book);
    }

    private void deleteSelected() {
        if (!librarian.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Only Admins can delete books.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    modelRow = table.convertRowIndexToModel(row);
        String title    = (String) tableModel.getValueAt(modelRow, 2);
        int    bookId   = (int)    tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete book \"" + title + "\"?\nThis cannot be undone.", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (bookDAO.deleteBook(bookId)) {
                MainFrame.notificationServer.broadcast("🗑 Book deleted: \"" + title + "\"");
                refresh();
                globalRefresh();
                JOptionPane.showMessageDialog(this, "Book deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int  bookId = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        Book book   = currentBooks.stream()
            .filter(b -> b.getBookId() == bookId).findFirst().orElse(null);
        if (book == null) return;

        String info = String.format(
            "<html><b>Title:</b> %s<br><b>Author:</b> %s<br><b>ISBN:</b> %s<br>" +
            "<b>Publisher:</b> %s<br><b>Year:</b> %d<br><b>Category:</b> %s<br>" +
            "<b>Copies:</b> %d total, %d available<br><b>Shelf:</b> %s<br><br>" +
            "<b>Description:</b><br>%s</html>",
            book.getTitle(), book.getAuthor(), book.getIsbn(),
            book.getPublisher(), book.getPublishYear(), book.getCategoryName(),
            book.getTotalCopies(), book.getAvailableCopies(), book.getShelfLocation(),
            book.getDescription() != null ? book.getDescription() : "N/A"
        );
        JOptionPane.showMessageDialog(this, info, "Book Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void globalRefresh() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) mf.refreshAll();
    }
}
