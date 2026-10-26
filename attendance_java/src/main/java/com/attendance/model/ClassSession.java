package com.attendance.model;

import java.sql.Date;
import java.sql.Timestamp;

public class ClassSession {
    private int id;
    private int courseId;
    private Date sessionDate;
    private String topic;
    private String qrToken;
    private Timestamp qrExpiresAt;
    private boolean active;

    public ClassSession() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public Timestamp getQrExpiresAt() { return qrExpiresAt; }
    public void setQrExpiresAt(Timestamp qrExpiresAt) { this.qrExpiresAt = qrExpiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
