package model;

import java.time.LocalDateTime;

public class Category {

    private int categoryId;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public Category() {}

    public Category(int categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public int getCategoryId()                    { return categoryId; }
    public void setCategoryId(int id)             { this.categoryId = id; }

    public String getName()                       { return name; }
    public void setName(String name)              { this.name = name; }

    public String getDescription()                { return description; }
    public void setDescription(String desc)       { this.description = desc; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    @Override
    public String toString()                      { return name; }
}
