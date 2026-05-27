# Smart Library System — Setup Guide

## Prerequisites
- Java 17+ (Java 21 recommended)
- MySQL 8.x
- Two JAR files in the `lib/` folder (see Step 2)

---

## Step 1: Set Up the Database

1. Open MySQL Workbench or your MySQL client
2. Run the SQL script:
   ```
   source database/library_db.sql
   ```
   Or paste the contents of `database/library_db.sql` into your MySQL client.

3. Verify the database was created:
   ```sql
   USE library_db;
   SHOW TABLES;
   -- Should show: books, categories, librarians, loans, members
   ```

---

## Step 2: Download Required JARs

Place both JARs in the `lib/` folder:

### FlatLaf (Modern UI)
Download from: https://github.com/JFormDesigner/FlatLaf/releases
- File: `flatlaf-3.x.jar`

### MySQL Connector/J
Download from: https://dev.mysql.com/downloads/connector/j/
- File: `mysql-connector-j-8.x.jar`
- Choose "Platform Independent" → ZIP → extract the JAR

---

## Step 3: Configure Database Connection

Edit `src/db/DatabaseConnection.java`:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/library_db?...";
private static final String USER     = "root";       // ← your MySQL username
private static final String PASSWORD = "root";       // ← your MySQL password
```

---

## Step 4: Build and Run

### Windows:
```
build.bat
run.bat
```

### Linux/Mac:
```bash
chmod +x build.sh run.sh
./build.sh
./run.sh
```

### Manual compile:
```bash
javac -cp "lib/*" -d out -sourcepath src src/Main.java src/model/*.java src/db/*.java src/rmi/*.java src/network/*.java src/threads/*.java src/gui/*.java
java -cp "out;lib/*" Main   # Windows
java -cp "out:lib/*" Main   # Linux/Mac
```

---

## Default Login Credentials

| Username | Password   | Role      |
|----------|-----------|-----------|
| admin    | Admin@123 | ADMIN     |
| tigisth   | Admin@123 | LIBRARIAN |
| yonast| Admin@123 | LIBRARIAN |

---

## Ports Used

| Service | Port |
|---------|------|
| MySQL   | 3306 |
| RMI Registry | 1099 |
| Notification Server | 9090 |

---

## Troubleshooting

**"MySQL JDBC Driver not found"**
→ Make sure `mysql-connector-j-*.jar` is in the `lib/` folder

**"FlatLaf not available"**
→ Make sure `flatlaf-*.jar` is in the `lib/` folder. App will fall back to system L&F.

**"RMI Server not connected"**
→ The RMI server starts automatically. Click "Reconnect" in the RMI panel after a few seconds.

**"Connection refused" on login**
→ Check MySQL is running and credentials in `DatabaseConnection.java` are correct.
