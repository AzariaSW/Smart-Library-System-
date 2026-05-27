# Smart Library System — Project Report
**Course:** Advanced Programming  
**Project:** Smart Library System  
**Version:** 1.0.0

---

## Table of Contents
1. [Introduction](#1-introduction)
2. [ER Diagram Description](#2-er-diagram-description)
3. [Database Schema (CREATE TABLE Statements)](#3-database-schema)
4. [Normalization Analysis](#4-normalization-analysis)
5. [System Functions](#5-system-functions)
6. [Technical Architecture](#6-technical-architecture)
7. [Conclusion](#7-conclusion)

---

## 1. Introduction

The **Smart Library System** is a full-featured Java desktop application designed to digitize and streamline the operations of a university library. It replaces manual, paper-based processes with an efficient, modern software solution.

### Key Capabilities
- **Book Management** — Add, edit, delete, and search books with category classification
- **Member Management** — Register and manage library members (students, faculty, public)
- **Loan Processing** — Issue and return books with automatic fine calculation
- **Overdue Tracking** — Automated background detection of overdue loans
- **Remote Queries** — RMI-based remote method invocation for distributed queries
- **Live Notifications** — Socket-based real-time event broadcasting to all connected clients

### Technology Stack
| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| GUI Framework | Java Swing + FlatLaf 3.x |
| Database | MySQL 8.x |
| DB Connectivity | JDBC (MySQL Connector/J 8.x) |
| Remote Invocation | Java RMI |
| Networking | Java Sockets (TCP) |
| Concurrency | SwingWorker, ScheduledExecutorService |

---

## 2. ER Diagram Description

### Entities and Attributes

#### `categories`
- **category_id** (PK) — Auto-increment integer
- name — Unique category name (e.g., "Computer Science")
- description — Optional text description
- created_at — Timestamp

#### `books`
- **book_id** (PK) — Auto-increment integer
- isbn — Unique ISBN-13 identifier
- title — Book title
- author — Author name(s)
- publisher — Publishing house
- publish_year — Year of publication
- **category_id** (FK → categories) — Book genre/category
- total_copies — Total physical copies owned
- available_copies — Currently available for borrowing
- shelf_location — Physical shelf code (e.g., "CS-A1")
- description — Optional synopsis
- cover_url — Optional cover image URL
- created_at, updated_at — Timestamps

#### `librarians`
- **librarian_id** (PK) — Auto-increment integer
- full_name — Librarian's full name
- email — Unique email address
- username — Unique login username
- password_hash — Hashed password
- phone — Contact number
- role — ENUM: ADMIN or LIBRARIAN
- is_active — Boolean account status
- last_login — Last login timestamp
- created_at — Account creation timestamp

#### `members`
- **member_id** (PK) — Auto-increment integer
- full_name — Member's full name
- email — Unique email address
- phone — Contact number
- address — Physical address
- membership_type — ENUM: STUDENT, FACULTY, PUBLIC
- membership_date — Date joined
- expiry_date — Membership expiry date
- is_active — Boolean account status
- max_loans — Maximum concurrent loans allowed
- created_at, updated_at — Timestamps

#### `loans`
- **loan_id** (PK) — Auto-increment integer
- **book_id** (FK → books) — Which book was borrowed
- **member_id** (FK → members) — Who borrowed it
- **librarian_id** (FK → librarians) — Who processed the loan
- loan_date — Date issued
- due_date — Expected return date
- return_date — Actual return date (NULL if not returned)
- status — ENUM: ACTIVE, RETURNED, OVERDUE
- fine_amount — Calculated fine in dollars
- fine_paid — Boolean fine payment status
- notes — Optional notes
- created_at, updated_at — Timestamps

### Relationships

| Relationship | Type | Description |
|---|---|---|
| books → categories | Many-to-One | Each book belongs to one category; a category has many books |
| loans → books | Many-to-One | Each loan references one book; a book can have many loans |
| loans → members | Many-to-One | Each loan belongs to one member; a member can have many loans |
| loans → librarians | Many-to-One | Each loan is processed by one librarian |

---

## 3. Database Schema

```sql
-- Database
CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Categories
CREATE TABLE categories (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books
CREATE TABLE books (
    book_id          INT AUTO_INCREMENT PRIMARY KEY,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    publisher        VARCHAR(255),
    publish_year     YEAR,
    category_id      INT,
    total_copies     INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    shelf_location   VARCHAR(50),
    description      TEXT,
    cover_url        VARCHAR(500),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Librarians
CREATE TABLE librarians (
    librarian_id  INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    role          ENUM('ADMIN','LIBRARIAN') NOT NULL DEFAULT 'LIBRARIAN',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    last_login    TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Members
CREATE TABLE members (
    member_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    address         TEXT,
    membership_type ENUM('STUDENT','FACULTY','PUBLIC') NOT NULL DEFAULT 'STUDENT',
    membership_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    expiry_date     DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    max_loans       INT NOT NULL DEFAULT 3,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Loans
CREATE TABLE loans (
    loan_id       INT AUTO_INCREMENT PRIMARY KEY,
    book_id       INT NOT NULL,
    member_id     INT NOT NULL,
    librarian_id  INT,
    loan_date     DATE NOT NULL DEFAULT (CURRENT_DATE),
    due_date      DATE NOT NULL,
    return_date   DATE NULL,
    status        ENUM('ACTIVE','RETURNED','OVERDUE') NOT NULL DEFAULT 'ACTIVE',
    fine_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fine_paid     BOOLEAN NOT NULL DEFAULT FALSE,
    notes         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id)      REFERENCES books(book_id)          ON DELETE RESTRICT,
    FOREIGN KEY (member_id)    REFERENCES members(member_id)       ON DELETE RESTRICT,
    FOREIGN KEY (librarian_id) REFERENCES librarians(librarian_id) ON DELETE SET NULL
);
```

---

## 4. Normalization Analysis

### First Normal Form (1NF)
**Rule:** All attributes must be atomic (no repeating groups or arrays).

All five tables satisfy 1NF:
- Every column holds a single, indivisible value
- No column stores comma-separated lists or arrays
- Each row is uniquely identified by its primary key
- Example: `books.author` stores one author string; multiple authors would require a separate `book_authors` junction table (extension point)

### Second Normal Form (2NF)
**Rule:** Must be in 1NF, and every non-key attribute must be fully functionally dependent on the entire primary key (no partial dependencies — relevant only for composite keys).

All tables use single-column surrogate primary keys (auto-increment integers), so partial dependency is impossible by definition. All tables are in 2NF.

- `books`: All attributes (title, author, publisher, etc.) depend entirely on `book_id`
- `loans`: All attributes (loan_date, due_date, status, etc.) depend entirely on `loan_id`
- `members`: All attributes depend entirely on `member_id`

### Third Normal Form (3NF)
**Rule:** Must be in 2NF, and no non-key attribute should transitively depend on the primary key through another non-key attribute.

**`books` table analysis:**
- `category_name` was NOT stored in `books` — instead, `category_id` (FK) references the `categories` table
- This eliminates the transitive dependency: `book_id → category_id → category_name`
- Category data is stored once in `categories` and referenced by FK

**`loans` table analysis:**
- Member details (name, email) are NOT stored in `loans` — only `member_id` (FK)
- Book details (title, ISBN) are NOT stored in `loans` — only `book_id` (FK)
- This eliminates transitive dependencies like: `loan_id → member_id → member_name`

**`librarians` table analysis:**
- Role permissions are not stored as separate attributes — role is an ENUM
- No transitive dependencies exist

**Conclusion:** All five tables (`categories`, `books`, `librarians`, `members`, `loans`) are in **Third Normal Form (3NF)**.

---

## 5. System Functions

### Authentication
| Function | Description |
|---|---|
| Login | Authenticates librarian by username/password |
| Logout | Ends session and returns to login screen |
| Role-based access | Admin vs Librarian role distinction |

### Book Management
| Function | Description |
|---|---|
| Add Book | Creates a new book record with all details |
| Edit Book | Updates existing book information |
| Delete Book | Removes a book (with referential integrity check) |
| Search Books | Full-text search by title, author, or ISBN |
| View Book Details | Shows complete book information |
| Track Availability | Real-time available copies counter |

### Member Management
| Function | Description |
|---|---|
| Add Member | Registers a new library member |
| Edit Member | Updates member information |
| Delete Member | Removes a member account |
| Search Members | Search by name, email, or phone |
| View Member Loans | Shows all loans for a specific member |
| Membership Types | STUDENT (3 loans), FACULTY (5 loans), PUBLIC (2 loans) |

### Loan Management
| Function | Description |
|---|---|
| Issue Loan | Borrows a book for a member with configurable loan period |
| Return Book | Processes book return and calculates fine |
| Calculate Fine | $0.50/day for overdue books |
| Mark Fine Paid | Records fine payment |
| Loan Validation | Checks member limits and book availability before issuing |

### Overdue Management
| Function | Description |
|---|---|
| View Overdue Loans | Lists all currently overdue loans |
| Update Statuses | Manually triggers overdue status update |
| Auto-check | Background thread runs every 24 hours automatically |
| Fine Summary | Shows total outstanding fines |

### Dashboard
| Function | Description |
|---|---|
| Statistics Cards | Total books, available copies, members, active loans, overdue count |
| Real-time Clock | Live clock display in sidebar |
| Quick Actions | One-click navigation to common tasks |
| Date Display | Current date shown on dashboard |

### RMI Remote Queries
| Function | Description |
|---|---|
| searchBook(title) | Remote book search by title keyword |
| checkAvailability(bookId) | Remote availability check for a specific book |
| getMemberLoans(memberId) | Remote retrieval of member's active loans |
| ping() | Server health check |

### Socket Notifications
| Function | Description |
|---|---|
| Broadcast | Server sends message to all connected clients |
| Book Added | Notification when new book is added |
| Book Returned | Notification when book is returned |
| Overdue Alert | Notification when overdue loans are detected |
| Live Panel | GUI panel shows notifications in real-time |

---

## 6. Technical Architecture

### Package Structure
```
src/
├── Main.java                    — Application entry point
├── gui/                         — All Swing UI screens
│   ├── AppTheme.java            — Color palette and fonts
│   ├── UIComponents.java        — Reusable styled components
│   ├── SplashScreen.java        — Animated startup screen
│   ├── LoginScreen.java         — Authentication screen
│   ├── MainFrame.java           — Main window with sidebar
│   ├── DashboardPanel.java      — Statistics dashboard
│   ├── BooksPanel.java          — Book CRUD management
│   ├── MembersPanel.java        — Member CRUD management
│   ├── BorrowPanel.java         — Issue loans
│   ├── ReturnPanel.java         — Process returns
│   ├── LoanHistoryPanel.java    — Full loan history
│   ├── OverduePanel.java        — Overdue loans view
│   ├── RMIPanel.java            — RMI query interface
│   └── NotificationPanel.java  — Live notifications
├── db/                          — JDBC data access layer
│   ├── DatabaseConnection.java  — Singleton connection manager
│   ├── BookDAO.java             — Book CRUD operations
│   ├── MemberDAO.java           — Member CRUD operations
│   ├── LoanDAO.java             — Loan CRUD operations
│   ├── LibrarianDAO.java        — Librarian auth & management
│   └── CategoryDAO.java         — Category operations
├── model/                       — Domain model classes
│   ├── Book.java
│   ├── Member.java
│   ├── Librarian.java
│   ├── Loan.java
│   └── Category.java
├── rmi/                         — Remote Method Invocation
│   ├── LibraryService.java      — Remote interface
│   ├── LibraryServiceImpl.java  — Server implementation
│   ├── RMIServer.java           — Registry and binding
│   └── RMIClient.java           — Client stub
├── network/                     — Socket programming
│   ├── NotificationServer.java  — Multi-client TCP server
│   └── NotificationClient.java  — TCP client with callback
└── threads/                     — Background workers
    ├── OverdueCheckerThread.java — 24-hour scheduled checker
    └── DataLoaderWorker.java    — Generic SwingWorker wrapper
```

### Threading Model
- **EDT (Event Dispatch Thread)** — All GUI updates
- **SwingWorker** — All database operations (via `DataLoaderWorker`)
- **ScheduledExecutorService** — 24-hour overdue checker
- **Daemon threads** — RMI server, notification server, notification client listener
- **CachedThreadPool** — One thread per connected notification client

### Security Considerations
- All SQL queries use `PreparedStatement` (no string concatenation)
- Passwords stored as plaintext in this demo (production: use BCrypt)
- Input validation on all forms before database operations
- Foreign key constraints prevent orphaned records

---

## 7. Conclusion

The Smart Library System successfully demonstrates the integration of multiple advanced Java programming concepts into a cohesive, production-quality application:

- **JDBC** with PreparedStatements provides secure, efficient database access
- **Java Swing + FlatLaf** delivers a modern, professional user interface
- **SwingWorker threads** ensure the GUI never freezes during data operations
- **Java RMI** enables distributed remote method invocation for library queries
- **Java Sockets** provide real-time event broadcasting across the network
- **ScheduledExecutorService** automates overdue loan detection every 24 hours
- **3NF database design** ensures data integrity and eliminates redundancy

The system is extensible — future enhancements could include BCrypt password hashing, email notifications, barcode scanning integration, and a web-based interface using the same database layer.
