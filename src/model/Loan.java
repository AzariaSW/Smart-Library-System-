package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, RETURNED, OVERDUE }

    private int loanId;
    private int bookId;
    private int memberId;
    private int librarianId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;
    private BigDecimal fineAmount;
    private boolean finePaid;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields (not stored in loans table)
    private String bookTitle;
    private String bookIsbn;
    private String memberName;
    private String librarianName;

    // Fine rate per day (Ethiopian Birr)
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    public Loan() {}

    public Loan(int bookId, int memberId, int librarianId,
                LocalDate loanDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.librarianId = librarianId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = Status.ACTIVE;
        this.fineAmount = BigDecimal.ZERO;
        this.finePaid = false;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getLoanId()                            { return loanId; }
    public void setLoanId(int loanId)                 { this.loanId = loanId; }

    public int getBookId()                            { return bookId; }
    public void setBookId(int bookId)                 { this.bookId = bookId; }

    public int getMemberId()                          { return memberId; }
    public void setMemberId(int memberId)             { this.memberId = memberId; }

    public int getLibrarianId()                       { return librarianId; }
    public void setLibrarianId(int id)                { this.librarianId = id; }

    public LocalDate getLoanDate()                    { return loanDate; }
    public void setLoanDate(LocalDate d)              { this.loanDate = d; }

    public LocalDate getDueDate()                     { return dueDate; }
    public void setDueDate(LocalDate d)               { this.dueDate = d; }

    public LocalDate getReturnDate()                  { return returnDate; }
    public void setReturnDate(LocalDate d)            { this.returnDate = d; }

    public Status getStatus()                         { return status; }
    public void setStatus(Status status)              { this.status = status; }

    public BigDecimal getFineAmount()                 { return fineAmount; }
    public void setFineAmount(BigDecimal amount)      { this.fineAmount = amount; }

    public boolean isFinePaid()                       { return finePaid; }
    public void setFinePaid(boolean finePaid)         { this.finePaid = finePaid; }

    public String getNotes()                          { return notes; }
    public void setNotes(String notes)                { this.notes = notes; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)         { this.updatedAt = t; }

    public String getBookTitle()                      { return bookTitle; }
    public void setBookTitle(String title)            { this.bookTitle = title; }

    public String getBookIsbn()                       { return bookIsbn; }
    public void setBookIsbn(String isbn)              { this.bookIsbn = isbn; }

    public String getMemberName()                     { return memberName; }
    public void setMemberName(String name)            { this.memberName = name; }

    public String getLibrarianName()                  { return librarianName; }
    public void setLibrarianName(String name)         { this.librarianName = name; }

    /**
     * Calculates the overdue fine based on days past due date.
     */
    public BigDecimal calculateFine() {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();
        if (checkDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, checkDate);
            return FINE_PER_DAY.multiply(new BigDecimal(daysOverdue));
        }
        return BigDecimal.ZERO;
    }

    public boolean isOverdue() {
        return status == Status.OVERDUE ||
               (status == Status.ACTIVE && LocalDate.now().isAfter(dueDate));
    }

    @Override
    public String toString() {
        return "Loan[" + loanId + "] " + bookTitle + " → " + memberName;
    }
}
