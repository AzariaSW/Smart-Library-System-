package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum MembershipType { STUDENT, FACULTY, PUBLIC }

    private int memberId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private MembershipType membershipType;
    private LocalDate membershipDate;
    private LocalDate expiryDate;
    private boolean active;
    private int maxLoans;
    private int activeLoans; // transient — computed from loans table
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Member() {}

    public Member(int memberId, String fullName, String email, String phone,
                  MembershipType membershipType, LocalDate membershipDate,
                  LocalDate expiryDate, boolean active, int maxLoans) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.membershipType = membershipType;
        this.membershipDate = membershipDate;
        this.expiryDate = expiryDate;
        this.active = active;
        this.maxLoans = maxLoans;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getMemberId()                          { return memberId; }
    public void setMemberId(int memberId)             { this.memberId = memberId; }

    public String getFullName()                       { return fullName; }
    public void setFullName(String fullName)          { this.fullName = fullName; }

    public String getEmail()                          { return email; }
    public void setEmail(String email)                { this.email = email; }

    public String getPhone()                          { return phone; }
    public void setPhone(String phone)                { this.phone = phone; }

    public String getAddress()                        { return address; }
    public void setAddress(String address)            { this.address = address; }

    public MembershipType getMembershipType()         { return membershipType; }
    public void setMembershipType(MembershipType t)   { this.membershipType = t; }

    public LocalDate getMembershipDate()              { return membershipDate; }
    public void setMembershipDate(LocalDate d)        { this.membershipDate = d; }

    public LocalDate getExpiryDate()                  { return expiryDate; }
    public void setExpiryDate(LocalDate d)            { this.expiryDate = d; }

    public boolean isActive()                         { return active; }
    public void setActive(boolean active)             { this.active = active; }

    public int getMaxLoans()                          { return maxLoans; }
    public void setMaxLoans(int maxLoans)             { this.maxLoans = maxLoans; }

    public int getActiveLoans()                       { return activeLoans; }
    public void setActiveLoans(int n)                 { this.activeLoans = n; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime t)         { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)         { this.updatedAt = t; }

    public boolean canBorrow()                        { return active && activeLoans < maxLoans; }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
