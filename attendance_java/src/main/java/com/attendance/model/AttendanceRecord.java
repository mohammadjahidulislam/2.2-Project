package com.attendance.model;

import java.sql.Timestamp;

public class AttendanceRecord {
    private int id;
    private int studentId;
    private int courseId;
    private int sessionId;
    private Timestamp markedAt;
    private String status;

    public AttendanceRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public Timestamp getMarkedAt() { return markedAt; }
    public void setMarkedAt(Timestamp markedAt) { this.markedAt = markedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
