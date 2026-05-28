package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int publishYear;
    private int categoryId;
    private String categoryName;
    private int totalCopies;
    private int availableCopies;
    private String shelfLocation;
    private String description;
    private String coverUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Book() {}

    public Book(int bookId, String isbn, String title, String author,
                String publisher, int publishYear, int categoryId,
                int totalCopies, int availableCopies, String shelfLocation) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.categoryId = categoryId;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.shelfLocation = shelfLocation;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getBookId()                        { return bookId; }
    public void setBookId(int bookId)             { this.bookId = bookId; }

    public String getIsbn()                       { return isbn; }
    public void setIsbn(String isbn)              { this.isbn = isbn; }

    public String getTitle()                      { return title; }
    public void setTitle(String title)            { this.title = title; }

    public String getAuthor()                     { return author; }
    public void setAuthor(String author)          { this.author = author; }

    public String getPublisher()                  { return publisher; }
    public void setPublisher(String publisher)    { this.publisher = publisher; }

    public int getPublishYear()                   { return publishYear; }
    public void setPublishYear(int publishYear)   { this.publishYear = publishYear; }

    public int getCategoryId()                    { return categoryId; }
    public void setCategoryId(int categoryId)     { this.categoryId = categoryId; }

    public String getCategoryName()               { return categoryName; }
    public void setCategoryName(String name)      { this.categoryName = name; }

    public int getTotalCopies()                   { return totalCopies; }
    public void setTotalCopies(int totalCopies)   { this.totalCopies = totalCopies; }

    public int getAvailableCopies()               { return availableCopies; }
    public void setAvailableCopies(int n)         { this.availableCopies = n; }

    public String getShelfLocation()              { return shelfLocation; }
    public void setShelfLocation(String loc)      { this.shelfLocation = loc; }

    public String getDescription()                { return description; }
    public void setDescription(String desc)       { this.description = desc; }

    public String getCoverUrl()                   { return coverUrl; }
    public void setCoverUrl(String url)           { this.coverUrl = url; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)     { this.updatedAt = t; }

    public boolean isAvailable()                  { return availableCopies > 0; }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
