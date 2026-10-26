-- QR Code Based Student Attendance Management System
-- Run this script once in MySQL before starting the application.

CREATE DATABASE IF NOT EXISTS attendance_system;
USE attendance_system;

CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120)  NOT NULL,
    email           VARCHAR(120)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    salt            VARCHAR(64)   NOT NULL,
    role            ENUM('STUDENT', 'TEACHER') NOT NULL,
    student_id      VARCHAR(50)   NULL UNIQUE,
    department      VARCHAR(120)  NULL,
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    teacher_id  INT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    course_id   INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE KEY uq_enrollment (student_id, course_id)
);

CREATE TABLE IF NOT EXISTS class_sessions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    course_id       INT NOT NULL,
    session_date    DATE NOT NULL,
    topic           VARCHAR(200),
    qr_token        VARCHAR(64),
    qr_expires_at   DATETIME,
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS attendance (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    course_id   INT NOT NULL,
    session_id  INT NOT NULL,
    marked_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(10) DEFAULT 'PRESENT',
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (session_id) REFERENCES class_sessions(id),
    UNIQUE KEY uq_attendance (student_id, session_id)
);
