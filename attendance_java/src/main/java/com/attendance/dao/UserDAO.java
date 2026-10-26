package com.attendance.dao;

import com.attendance.db.DBConnection;
import com.attendance.model.User;
import com.attendance.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
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

    /** Returns true if registration succeeded, false if the email is already used. */
    public boolean register(String name, String email, String plainPassword, String role,
                             String studentId, String department) {
        if (findByEmail(email) != null) {
            return false;
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);

        String sql = "INSERT INTO users (name, email, password_hash, salt, role, student_id, department) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, role);
            ps.setString(6, "STUDENT".equals(role) ? studentId : null);
            ps.setString(7, department);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Returns the logged-in User if credentials are valid, otherwise null. */
    public User login(String email, String plainPassword) {
        User user = findByEmail(email);
        if (user == null) return null;
        if (PasswordUtil.verify(plainPassword, user.getSalt(), user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public void updateProfile(int userId, String name, String department) {
        String sql = "UPDATE users SET name = ?, department = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, department);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (user == null || !PasswordUtil.verify(currentPassword, user.getSalt(), user.getPasswordHash())) {
            return false;
        }
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hash(newPassword, newSalt);
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setInt(3, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setRole(rs.getString("role"));
        u.setStudentId(rs.getString("student_id"));
        u.setDepartment(rs.getString("department"));
        return u;
    }
}
