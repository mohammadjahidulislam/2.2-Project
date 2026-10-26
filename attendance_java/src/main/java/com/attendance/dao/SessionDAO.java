package com.attendance.dao;

import com.attendance.db.DBConnection;
import com.attendance.model.ClassSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public ClassSession createSession(int courseId, String topic, String qrToken, int validSeconds) {
        String sql = "INSERT INTO class_sessions (course_id, session_date, topic, qr_token, qr_expires_at, active) " +
                "VALUES (?, CURDATE(), ?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND), TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, topic);
            ps.setString(3, qrToken);
            ps.setInt(4, validSeconds);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Refreshes the QR token and expiry for an active session (called periodically by the teacher's screen). */
    public void refreshToken(int sessionId, String newToken, int validSeconds) {
        String sql = "UPDATE class_sessions SET qr_token = ?, qr_expires_at = DATE_ADD(NOW(), INTERVAL ? SECOND) WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newToken);
            ps.setInt(2, validSeconds);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeSession(int sessionId) {
        String sql = "UPDATE class_sessions SET active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ClassSession findById(int id) {
        String sql = "SELECT * FROM class_sessions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ClassSession> findByCourse(int courseId) {
        List<ClassSession> list = new ArrayList<>();
        String sql = "SELECT * FROM class_sessions WHERE course_id = ? ORDER BY session_date, id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Finds the latest active session for a course (used on the student's scan screen). */
    public ClassSession findActiveSessionForCourse(int courseId) {
        String sql = "SELECT * FROM class_sessions WHERE course_id = ? AND active = TRUE " +
                "ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ClassSession mapRow(ResultSet rs) throws SQLException {
        ClassSession s = new ClassSession();
        s.setId(rs.getInt("id"));
        s.setCourseId(rs.getInt("course_id"));
        s.setSessionDate(rs.getDate("session_date"));
        s.setTopic(rs.getString("topic"));
        s.setQrToken(rs.getString("qr_token"));
        s.setQrExpiresAt(rs.getTimestamp("qr_expires_at"));
        s.setActive(rs.getBoolean("active"));
        return s;
    }
}
