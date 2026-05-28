package model;

import java.time.LocalDateTime;

public class Librarian {

    public enum Role { ADMIN, LIBRARIAN }

    private int librarianId;
    private String fullName;
    private String email;
    private String username;
    private String passwordHash;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public Librarian() {}

    public Librarian(int librarianId, String fullName, String email,
                     String username, Role role, boolean active) {
        this.librarianId = librarianId;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.active = active;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getLibrarianId()                       { return librarianId; }
    public void setLibrarianId(int id)                { this.librarianId = id; }

    public String getFullName()                       { return fullName; }
    public void setFullName(String fullName)          { this.fullName = fullName; }

    public String getEmail()                          { return email; }
    public void setEmail(String email)                { this.email = email; }

    public String getUsername()                       { return username; }
    public void setUsername(String username)          { this.username = username; }

    public String getPasswordHash()                   { return passwordHash; }
    public void setPasswordHash(String hash)          { this.passwordHash = hash; }

    public String getPhone()                          { return phone; }
    public void setPhone(String phone)                { this.phone = phone; }

    public Role getRole()                             { return role; }
    public void setRole(Role role)                    { this.role = role; }

    public boolean isActive()                         { return active; }
    public void setActive(boolean active)             { this.active = active; }

    public LocalDateTime getLastLogin()               { return lastLogin; }
    public void setLastLogin(LocalDateTime t)         { this.lastLogin = t; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }

    public boolean isAdmin()                          { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return fullName + " [" + role + "]";
    }
}
