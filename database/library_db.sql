-- ============================================================
-- Smart Library System - MySQL Database Schema
-- library_db | 3NF Normalized | Full Relational Design
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ============================================================
-- TABLE: categories
-- Stores book categories/genres (1NF, 2NF, 3NF compliant)
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: books
-- Stores all book records. category_id is FK to categories.
-- ============================================================
CREATE TABLE IF NOT EXISTS books (
    book_id       INT AUTO_INCREMENT PRIMARY KEY,
    isbn          VARCHAR(20)  NOT NULL UNIQUE,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(255) NOT NULL,
    publisher     VARCHAR(255),
    publish_year  YEAR,
    category_id   INT,
    total_copies  INT          NOT NULL DEFAULT 1,
    available_copies INT       NOT NULL DEFAULT 1,
    shelf_location VARCHAR(50),
    description   TEXT,
    cover_url     VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_copies CHECK (available_copies >= 0 AND total_copies >= 0)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: librarians
-- Stores librarian/admin accounts (separate from members)
-- ============================================================
CREATE TABLE IF NOT EXISTS librarians (
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
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: members
-- Stores library member records
-- ============================================================
CREATE TABLE IF NOT EXISTS members (
    member_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    address       TEXT,
    membership_type ENUM('STUDENT','FACULTY','PUBLIC') NOT NULL DEFAULT 'STUDENT',
    membership_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    expiry_date   DATE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    max_loans     INT NOT NULL DEFAULT 3,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: loans
-- Tracks book borrowing. References books and members.
-- ============================================================
CREATE TABLE IF NOT EXISTS loans (
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
    CONSTRAINT fk_loan_book      FOREIGN KEY (book_id)      REFERENCES books(book_id)           ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_member    FOREIGN KEY (member_id)    REFERENCES members(member_id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_loan_librarian FOREIGN KEY (librarian_id) REFERENCES librarians(librarian_id)  ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_books_title      ON books(title);
CREATE INDEX idx_books_author     ON books(author);
CREATE INDEX idx_books_isbn       ON books(isbn);
CREATE INDEX idx_loans_status     ON loans(status);
CREATE INDEX idx_loans_due_date   ON loans(due_date);
CREATE INDEX idx_members_email    ON members(email);

-- ============================================================
-- SEED DATA — Categories (Ethiopian university context)
-- ============================================================
INSERT INTO categories (name, description) VALUES
('Computer Science',        'Programming, algorithms, software engineering, ICT'),
('Mathematics',             'Pure and applied mathematics, statistics'),
('Physics',                 'Classical and modern physics, applied sciences'),
('Ethiopian Literature',    'Amharic fiction, poetry, Ge''ez manuscripts, oral tradition'),
('Ethiopian History',       'Ancient Axum, medieval kingdoms, modern Ethiopia'),
('Biology & Agriculture',   'Life sciences, ecology, Ethiopian flora and fauna'),
('Chemistry',               'Organic, inorganic, and industrial chemistry'),
('Civil Engineering',       'Construction, infrastructure, hydraulic engineering'),
('Economics & Development', 'Ethiopian economy, development studies, finance'),
('Law & Governance',        'Ethiopian law, constitution, public administration');

-- ============================================================
-- SEED DATA — Librarians
-- Default password for all accounts: Admin@123
-- ============================================================
INSERT INTO librarians (full_name, email, username, password_hash, phone, role) VALUES
('Abebe Girma',      'abebe.girma@aastu.edu.et',   'admin',    'Admin@123', '+251-911-001001', 'ADMIN'),
('Tigist Haile',     'tigist.haile@aastu.edu.et',  'tigisth',  'Admin@123', '+251-911-002002', 'LIBRARIAN'),
('Yonas Tesfaye',    'yonas.tesfaye@aastu.edu.et', 'yonast',   'Admin@123', '+251-911-003003', 'LIBRARIAN');

-- ============================================================
-- SEED DATA — Members
-- Ethiopian names, universities, cities, and phone numbers
-- ============================================================
INSERT INTO members (full_name, email, phone, address, membership_type, membership_date, expiry_date, max_loans) VALUES
('Selam Bekele',      'selam.bekele@student.aastu.edu.et',   '+251-912-101101', 'Addis Ababa, Bole Sub-City',         'STUDENT', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 3),
('Dawit Mengistu',    'dawit.mengistu@student.aastu.edu.et', '+251-912-102102', 'Addis Ababa, Kirkos Sub-City',       'STUDENT', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 3),
('Dr. Meron Tadesse', 'meron.tadesse@faculty.aastu.edu.et',  '+251-911-203203', 'Addis Ababa, Yeka Sub-City',         'FACULTY', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR), 5),
('Biruk Alemu',       'biruk.alemu@student.aastu.edu.et',    '+251-912-104104', 'Addis Ababa, Akaki-Kaliti Sub-City', 'STUDENT', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 3),
('Hiwot Girma',       'hiwot.girma@gmail.com',               '+251-913-105105', 'Adama, Oromia Region',               'PUBLIC',  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 2),
('Prof. Tesfaye Wolde','tesfaye.wolde@faculty.aastu.edu.et', '+251-911-206206', 'Addis Ababa, Gulele Sub-City',       'FACULTY', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR), 5);

-- ============================================================
-- SEED DATA — Books
-- Mix of Ethiopian authors, African studies, and core academic
-- books used in Ethiopian universities
-- ============================================================
INSERT INTO books (isbn, title, author, publisher, publish_year, category_id, total_copies, available_copies, shelf_location, description) VALUES

-- Computer Science
('978-99944-2-000-1', 'Introduction to Programming with Python',         'Getnet Asefa',          'Addis Ababa University Press', 2020, 1, 4, 4, 'CS-A1', 'A beginner-friendly Python programming textbook written for Ethiopian university students.'),
('978-99944-2-001-2', 'Database Systems: An Ethiopian Perspective',      'Yohannes Berhane',      'Ethiopian Books Centre',       2019, 1, 3, 3, 'CS-A2', 'Covers relational databases, SQL, and real-world Ethiopian case studies.'),
('978-0-262-03384-8', 'Introduction to Algorithms (CLRS)',               'Thomas H. Cormen',      'MIT Press',                    2009, 1, 5, 5, 'CS-B1', 'The standard algorithms textbook used across Ethiopian universities.'),
('978-99944-2-002-3', 'Computer Networks and Internet Technologies',     'Mulugeta Kebede',       'Addis Ababa University Press', 2021, 1, 3, 3, 'CS-B2', 'Networking fundamentals tailored for the Ethiopian ICT curriculum.'),
('978-99944-2-003-4', 'Software Engineering Principles',                 'Hailemariam Desta',     'Ethiopian Books Centre',       2018, 1, 2, 2, 'CS-C1', 'Software development lifecycle, design patterns, and project management.'),

-- Mathematics
('978-99944-2-010-0', 'Calculus for Ethiopian University Students',      'Tadesse Bekele',        'Addis Ababa University Press', 2017, 2, 6, 6, 'MA-A1', 'Differential and integral calculus with Ethiopian engineering applications.'),
('978-99944-2-011-1', 'Linear Algebra and Matrix Theory',                'Girma Woldemichael',    'Mega Publishing',              2016, 2, 4, 4, 'MA-A2', 'Vectors, matrices, and linear transformations for science and engineering.'),
('978-99944-2-012-2', 'Statistics and Probability for Engineers',        'Almaz Hailu',           'Addis Ababa University Press', 2019, 2, 3, 3, 'MA-B1', 'Applied statistics with examples from Ethiopian industry and agriculture.'),

-- Physics
('978-99944-2-020-8', 'University Physics for Ethiopian Students',       'Bekele Seyoum',         'Addis Ababa University Press', 2018, 3, 4, 4, 'PH-A1', 'Mechanics, thermodynamics, and electromagnetism for first-year students.'),
('978-99944-2-021-9', 'Applied Physics in Ethiopian Industries',         'Teshome Alemu',         'Ethiopian Books Centre',       2020, 3, 2, 2, 'PH-B1', 'Practical physics applications in Ethiopian manufacturing and energy sectors.'),

-- Ethiopian Literature
('978-99944-0-100-5', 'Fikir Eske Meqabir (Love unto Crypt)',            'Haddis Alemayehu',      'Mega Publishing',              1966, 4, 5, 5, 'LT-A1', 'The most celebrated Amharic novel, a timeless Ethiopian love story.'),
('978-99944-0-101-6', 'Oromay',                                          'Bealu Girma',           'Kuraz Publishing Agency',      1983, 4, 4, 4, 'LT-A2', 'A powerful political novel set during the Derg era in Ethiopia.'),
('978-99944-0-102-7', 'Ye Burka Zimita (The Silence of the Hyena)',      'Daniachew Worku',       'Oxford University Press',      1974, 4, 3, 3, 'LT-B1', 'A landmark Ethiopian novel exploring tradition and modernity.'),
('978-99944-0-103-8', 'Kadmas Bashager',                                 'Sebhat Gebre-Egziabher','Mega Publishing',              1992, 4, 2, 2, 'LT-B2', 'A celebrated collection of Amharic short stories.'),

-- Ethiopian History
('978-99944-0-200-2', 'The History of Ethiopia',                         'Bahru Zewde',           'Ohio University Press',        2001, 5, 4, 4, 'HI-A1', 'The definitive modern history of Ethiopia from the 19th century to today.'),
('978-99944-0-201-3', 'Ancient Ethiopia: Axum and Its Neighbors',        'Taddesse Tamrat',       'Addis Ababa University Press', 1972, 5, 3, 3, 'HI-A2', 'Scholarly study of the Axumite Empire and its regional influence.'),
('978-99944-0-202-4', 'The Battle of Adwa: African Victory',             'Paulos Milkias',        'Algora Publishing',            2005, 5, 3, 3, 'HI-B1', 'Comprehensive account of Ethiopia''s historic 1896 victory over Italy.'),

-- Biology & Agriculture
('978-99944-2-030-8', 'Ethiopian Flora and Biodiversity',                'Sebsebe Demissew',      'Addis Ababa University Press', 2015, 6, 3, 3, 'BI-A1', 'Survey of Ethiopia''s unique plant species and conservation challenges.'),
('978-99944-2-031-9', 'Agricultural Development in Ethiopia',            'Workneh Negatu',        'Ethiopian Books Centre',       2018, 6, 4, 4, 'BI-A2', 'Modern farming techniques, food security, and rural development in Ethiopia.'),

-- Civil Engineering
('978-99944-2-040-7', 'Hydraulic Engineering for Ethiopian Rivers',      'Asfaw Kebede',          'Addis Ababa University Press', 2017, 8, 3, 3, 'EN-A1', 'Water resource management and dam engineering in the Ethiopian highlands.'),
('978-99944-2-041-8', 'Construction Materials and Methods in Ethiopia',  'Mesfin Haile',          'Mega Publishing',              2019, 8, 2, 2, 'EN-A2', 'Local construction materials, standards, and building practices in Ethiopia.'),

-- Economics & Development
('978-99944-0-300-6', 'The Ethiopian Economy: Structure and Performance', 'Alemayehu Geda',        'Addis Ababa University Press', 2008, 9, 4, 4, 'EC-A1', 'Macroeconomic analysis of Ethiopia''s growth, trade, and development.'),
('978-99944-0-301-7', 'Poverty and Development in Sub-Saharan Africa',   'Mulu Gebreeyesus',      'Ethiopian Books Centre',       2016, 9, 3, 3, 'EC-A2', 'Development economics with focus on Ethiopia and the Horn of Africa.'),

-- Law & Governance
('978-99944-0-400-9', 'The Constitution of the Federal Democratic Republic of Ethiopia', 'Federal Negarit Gazeta', 'Berhanena Selam', 1995, 10, 5, 5, 'LW-A1', 'The 1995 FDRE Constitution — the supreme law of Ethiopia.'),
('978-99944-0-401-0', 'Ethiopian Civil Law',                             'Krzeczunowicz',         'Addis Ababa University Press', 1983, 10, 3, 3, 'LW-A2', 'Commentary on the Ethiopian Civil Code and its application.');

-- ============================================================
-- SEED DATA — Sample Loans
-- ============================================================
INSERT INTO loans (book_id, member_id, librarian_id, loan_date, due_date, status) VALUES
(1,  1, 1, CURDATE(),                          DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'ACTIVE'),
(3,  2, 1, CURDATE(),                          DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'ACTIVE'),
(11, 3, 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY),  'OVERDUE'),
(15, 4, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 9 DAY),  'ACTIVE'),
(22, 5, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_ADD(CURDATE(), INTERVAL 11 DAY), 'ACTIVE');

-- Update available copies to reflect the active loans above
UPDATE books SET available_copies = available_copies - 1 WHERE book_id IN (1, 3, 11, 15, 22);
