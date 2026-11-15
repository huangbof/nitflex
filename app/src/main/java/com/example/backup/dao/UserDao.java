package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.User;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object chuyên xử lý truy vấn bảng users và xác thực trong CSDL MySQL
 */
public class UserDao {

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    public static User login(String usernameOrEmail, String password) throws SQLException {
        String hash = md5(password);
        String sql = "SELECT id, username, email, is_vip, role FROM users WHERE (username = ? OR email = ?) AND password_hash = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL. Hãy chắc chắn XAMPP đã Start MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usernameOrEmail);
                ps.setString(2, usernameOrEmail);
                ps.setString(3, hash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        try { user.setVip(rs.getInt("is_vip") == 1); } catch (Exception ignored) {}
                        try { user.setRole(rs.getString("role")); } catch (Exception ignored) {}
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public static String checkDuplicate(String username, String email) throws SQLException {
        String checkSql = "SELECT id, username, email FROM users WHERE username = ? OR email = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, username);
                ps.setString(2, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String foundU = rs.getString("username");
                        if (username.equalsIgnoreCase(foundU)) {
                            return "Tên đăng nhập '" + username + "' đã được sử dụng!";
                        } else {
                            return "Địa chỉ Email '" + email + "' đã được đăng ký tài khoản!";
                        }
                    }
                }
            }
        }
        return null; // Không trùng
    }

    public static int register(String username, String password, String email) throws SQLException {
        String insertSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, md5(password));
                ps.setString(3, email);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        return rs.next() ? rs.getInt(1) : 0;
                    }
                }
            }
        }
        return 0;
    }

    public static User loginOrRegisterGoogleUser(String email, String displayName) throws SQLException {
        String checkSql = "SELECT id, username FROM users WHERE email = ? LIMIT 1";
        String insertSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                User u = new User();
                u.setId(9999);
                u.setUsername(displayName != null ? displayName : "Google User");
                return u;
            }

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, email);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        return u;
                    }
                }
            }

            String baseName = (displayName != null && !displayName.trim().isEmpty())
                    ? displayName.trim()
                    : (email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : "google_user");

            String uniqueUsername = baseName;
            int suffix = 1;
            while (true) {
                try (PreparedStatement psU = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                    psU.setString(1, uniqueUsername);
                    try (ResultSet rsU = psU.executeQuery()) {
                        if (!rsU.next()) break;
                        uniqueUsername = baseName + "_" + suffix++;
                    }
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setString(1, uniqueUsername);
                psInsert.setString(2, md5("google_oauth_" + System.currentTimeMillis()));
                psInsert.setString(3, email);
                psInsert.executeUpdate();
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    int newId = rs.next() ? rs.getInt(1) : 0;
                    User u = new User();
                    u.setId(newId);
                    u.setUsername(uniqueUsername);
                    return u;
                }
            }
        }
    }

    public static boolean checkVipStatus(int userId) {
        String sql = "SELECT is_vip FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("is_vip") == 1;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean upgradeToVip(int userId) {
        String alterSql = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_vip TINYINT DEFAULT 0";
        String updateSql = "UPDATE users SET is_vip = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return true;
            try (Statement stmt = conn.createStatement()) {
                try { stmt.executeUpdate(alterSql); } catch (Exception ignored) {}
            }
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public static int resolveValidUserId(Connection conn, int userId) {
        if (conn == null) return userId > 0 ? userId : 1;
        if (userId > 0) {
            String checkSql = "SELECT id FROM users WHERE id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            } catch (Exception ignored) {}
        }

        int targetId = userId > 0 ? userId : 9999;
        String insertSql = "INSERT INTO users (id, username, password_hash) VALUES (?, ?, 'nitflex_pass') "
                         + "ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, targetId);
            ps.setString(2, "user_" + targetId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return rs.getInt(1);
                }
            }
            return targetId;
        } catch (Exception ignored) {}

        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users ORDER BY id ASC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception ignored) {}

        return 1;
    }
}
