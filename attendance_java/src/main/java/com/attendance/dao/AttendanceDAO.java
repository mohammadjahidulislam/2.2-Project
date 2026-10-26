package com.attendance.dao;

import com.attendance.db.DBConnection;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class AttendanceDAO {

    public enum MarkResult { SUCCESS, ALREADY_MARKED, ERROR }

    public boolean alreadyMarked(int studentId, int sessionId) {
        String sql = "SELECT id FROM attendance WHERE student_id = ? AND session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MarkResult markAttendance(int studentId, int courseId, int sessionId) {
        if (alreadyMarked(studentId, sessionId)) {
            return MarkResult.ALREADY_MARKED;
        }
        String sql = "INSERT INTO attendance (student_id, course_id, session_id, status) VALUES (?, ?, ?, 'PRESENT')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
            return MarkResult.SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            return MarkResult.ERROR;
        }
    }

    /** Returns the set of session IDs a student has been marked present for, within a course. */
    public Set<Integer> getAttendedSessionIds(int studentId, int courseId) {
        Set<Integer> set = new HashSet<>();
        String sql = "SELECT session_id FROM attendance WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getInt("session_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    public int countAttendedSessions(int studentId, int courseId) {
        return getAttendedSessionIds(studentId, courseId).size();
    }
}
