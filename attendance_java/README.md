# QR Code Based Student Attendance Management System

A desktop application built with **Java 21, JavaFX, MySQL 8, JDBC, Maven, and ZXing**, for university project submission.

## Features
- Student and Teacher accounts (registration and login)
- Teacher starts a class session and a QR code is displayed, which **automatically refreshes every 25 seconds** to prevent proxy attendance
- Student scans the QR code using the laptop/webcam and attendance is recorded instantly
- Dashboard: student sees attendance percentage per course, teacher manages courses and sessions
- Attendance sheet (date-wise present/absent grid) for each course
- One-click **CSV export** for end-of-month or end-of-course reporting
- Settings screen: edit profile, change password

## Technology Stack
| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI Framework | JavaFX |
| Database | MySQL 8 |
| Database Access | JDBC (plain, no ORM) |
| Build Tool | Maven |
| QR Code Generation & Scanning | ZXing ("Zebra Crossing") |
| Webcam Access | webcam-capture (Sarxos) |

---

## Part 1 — One-Time Setup

### Step 1: Install required software
1. **Java Development Kit (JDK) 21** — download from [Adoptium](https://adoptium.net) or Oracle. Verify with:
   ```
   java -version
   ```
2. **Maven** — download from [maven.apache.org](https://maven.apache.org/download.cgi). Verify with:
   ```
   mvn -version
   ```
3. **MySQL Server 8** — install MySQL Community Server and MySQL Workbench (or use XAMPP/WAMP which bundles MySQL).
4. **An IDE** (recommended for beginners) — IntelliJ IDEA (Community Edition) or Eclipse. This makes running and demonstrating the project much easier than the command line.

### Step 2: Create the database
1. Open MySQL Workbench (or your MySQL client / phpMyAdmin).
2. Open the file `sql/schema.sql` from this project and run the entire script. This creates the database `attendance_system` and all required tables.

### Step 3: Configure the database connection
Open this file:
```
src/main/java/com/attendance/db/DBConnection.java
```
Update these two lines with your own MySQL username and password:
```java
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### Step 4: Open the project
- **In IntelliJ IDEA:** File → Open → select the `attendance_java` folder (the one containing `pom.xml`). IntelliJ will automatically detect it as a Maven project and download all dependencies (needs internet the first time).
- **In Eclipse:** File → Import → Maven → Existing Maven Projects → select the folder.

Wait for Maven to finish downloading dependencies (JavaFX, MySQL driver, ZXing, webcam-capture) — this only happens once.

---

## Part 2 — Running the Application

### Option A: Run from the IDE (recommended for demo)
Open `src/main/java/com/attendance/MainApp.java` and click the **Run** button (green play icon) next to the `main` method.

### Option B: Run from the terminal using Maven
From the project's root folder (where `pom.xml` is located):
```
mvn clean javafx:run
```

The login window should open within a few seconds.

---

## Part 3 — How to Demonstrate This to Your Teacher

Follow this exact sequence for a smooth, convincing live demo:

1. **Register a Teacher account**
   - Click "Register" → select role **TEACHER** → fill in name, email, password → Register.
2. **Login as the teacher** and create a course
   - Click "+ New Course" → enter a course code (e.g. `CSE-401`) and name → Create.
3. **Register a Student account** (open a second run of the app, or logout and register again)
   - Select role **STUDENT** → fill in Student ID, name, email, password.
4. **Login as the student** and enroll in the course
   - Click "Browse / Enroll Courses" → click "Enroll" next to the course the teacher created.
5. **Login back as the teacher**
   - Select the course from the table → click **"Start New Class Session (QR)"**.
   - A QR code appears full-screen. Point this screen toward your webcam (or use a second device/monitor) — this simulates a projector showing the QR code in a real classroom.
6. **Login as the student in a separate window/second run** (or logout/login)
   - Click "Scan QR for Attendance" → select the course → click "Start Camera".
   - Point the webcam at the QR code shown on the teacher's screen. Attendance is marked automatically within a second or two.
7. **Go back to the teacher's dashboard**
   - Click "View Attendance Sheet" — the student now shows as **P (Present)** for today's session.
   - Click "Export CSV" to show the downloadable report.

### Talking points for your viva (what to explain to your teacher)
- **Why QR refreshes every 25 seconds**: explain this is an anti-proxy-attendance mechanism — a screenshot of the QR code becomes useless after 25 seconds, so a student cannot mark attendance for a friend using an old screenshot.
- **Architecture**: explain the 3-layer structure — `model` (data classes), `dao` (database access using JDBC/PreparedStatement, which also prevents SQL injection), `controller` (JavaFX UI logic), tied together with FXML view files.
- **Database design**: show `sql/schema.sql` and explain the relationships — a teacher owns courses, students enroll in courses, a course has many class sessions, and attendance links a student to a specific session.
- **Security**: passwords are never stored in plain text — explain the salted SHA-256 hashing in `PasswordUtil.java`.

---

## Project Structure
```
attendance_java/
├── pom.xml                        # Maven build file (all dependencies listed here)
├── sql/schema.sql                 # Run this once in MySQL to create the database
├── README.md
└── src/main/
    ├── java/com/attendance/
    │   ├── MainApp.java            # Application entry point
    │   ├── db/DBConnection.java     # MySQL connection settings
    │   ├── model/                    # Plain data classes (User, Course, ClassSession, AttendanceRecord)
    │   ├── dao/                      # Database access (JDBC) classes
    │   ├── util/                     # Password hashing, QR generation/decoding, scene switching
    │   └── controller/               # One controller class per screen
    └── resources/com/attendance/
        ├── fxml/                     # One FXML layout file per screen
        └── css/style.css             # Application styling
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Communications link failure` on login | MySQL server is not running, or wrong host/port in `DBConnection.java`. Start MySQL and confirm it's on port 3306. |
| `Access denied for user` | Wrong username/password in `DBConnection.java`. |
| Webcam does not open / black screen | Close any other application using the camera (Zoom, Teams, etc.) and try again. Make sure only one instance of the app is trying to open the webcam. |
| QR code does not scan | Make sure there's good lighting and the QR code is not too small/far from the camera. Hold steady for a second. |
| `mvn javafx:run` fails with dependency errors | Ensure you have an internet connection the first time you build, so Maven can download JavaFX, MySQL driver, ZXing, and webcam-capture from the Maven repository. |
| JavaFX runtime components missing (when running the built JAR directly) | Always run via `mvn javafx:run` or from the IDE — this automatically wires up the JavaFX runtime through the javafx-maven-plugin. |

## Possible Extensions (mention these if your teacher asks "what else could be added")
- Face recognition as a second attendance method (using OpenCV Java bindings)
- Geo-fencing so attendance can only be marked from inside the classroom Wi-Fi/location
- Email/SMS alerts for students falling below a minimum attendance percentage
- An admin panel to manage multiple teachers and departments
