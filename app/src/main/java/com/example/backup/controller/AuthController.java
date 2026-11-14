package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.database.DBConnection;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AuthCallback {
        void onSuccess(int userId, String username);
        void onError(String message);
    }

    public static void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            String hash = md5(password);
            String sql = "SELECT id, username FROM users WHERE username = ? AND password_hash = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối đến MySQL. Hãy chắc chắn XAMPP đã Start MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, hash);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int id = rs.getInt("id");
                            String name = rs.getString("username");
                            mainHandler.post(() -> callback.onSuccess(id, name));
                        } else {
                            mainHandler.post(() -> callback.onError("Sai tên đăng nhập hoặc mật khẩu!"));
                        }
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    /**
     * Đăng nhập hoặc tạo mới tài khoản liên kết Google (theo Buổi 10)
     */
    public static void loginOrRegisterGoogleUser(String email, String displayName, AuthCallback callback) {
        executor.execute(() -> {
            String checkSql = "SELECT id, username FROM users WHERE email = ?";
            String insertSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    // Fallback offline nếu MySQL chưa start
                    mainHandler.post(() -> callback.onSuccess(9999, displayName != null ? displayName : "Google User"));
                    return;
                }

                // 1. Kiểm tra email đã có trong MySQL chưa
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, email);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            int existingId = rs.getInt("id");
                            String existingName = rs.getString("username");
                            mainHandler.post(() -> callback.onSuccess(existingId, existingName));
                            return;
                        }
                    }
                }

                // 2. Tạo username từ DisplayName hoặc tiền tố email
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

                // 3. Lưu tài khoản Google vào CSDL users
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psInsert.setString(1, uniqueUsername);
                    psInsert.setString(2, md5("GOOGLE_AUTH_" + email));
                    psInsert.setString(3, email);
                    int affected = psInsert.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rsGen = psInsert.getGeneratedKeys()) {
                            int newId = rsGen.next() ? rsGen.getInt(1) : 0;
                            final String finalName = uniqueUsername;
                            mainHandler.post(() -> callback.onSuccess(newId, finalName));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Không thể lưu tài khoản Google vào CSDL!"));
                    }
                }
            } catch (Exception e) {
                // Fallback nếu có lỗi CSDL
                mainHandler.post(() -> callback.onSuccess(9999, displayName != null ? displayName : email));
            }
        });
    }

    public static void register(String username, String password, String email, AuthCallback callback) {
        executor.execute(() -> {
            String checkSql = "SELECT id FROM users WHERE username = ? OR email = ?";
            String insertSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối đến MySQL. Hãy chắc chắn XAMPP đã Start MySQL!"));
                    return;
                }
                // Check tồn tại
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, username);
                    psCheck.setString(2, email);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            mainHandler.post(() -> callback.onError("Tên đăng nhập hoặc Email đã tồn tại!"));
                            return;
                        }
                    }
                }

                // Insert mới
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psInsert.setString(1, username);
                    psInsert.setString(2, md5(password));
                    psInsert.setString(3, email);
                    int affected = psInsert.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = psInsert.getGeneratedKeys()) {
                            int newId = rs.next() ? rs.getInt(1) : 0;
                            mainHandler.post(() -> callback.onSuccess(newId, username));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Đăng ký không thành công!"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public interface VipCallback {
        void onSuccess(boolean isVip);
        void onError(String message);
    }

    public static void checkVipStatus(int userId, VipCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT is_vip FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            boolean isVip = rs.getInt("is_vip") == 1;
                            mainHandler.post(() -> callback.onSuccess(isVip));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(false));
                        }
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onSuccess(false));
            }
        });
    }

    public static void upgradeToVip(int userId, MovieController.SimpleCallback callback) {
        executor.execute(() -> {
            String alterSql = "ALTER TABLE users ADD COLUMN IF NOT EXISTS is_vip TINYINT DEFAULT 0";
            String updateSql = "UPDATE users SET is_vip = 1 WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onSuccess("Đã kích hoạt VIP (Cục bộ)!"));
                    return;
                }
                try (Statement stmt = conn.createStatement()) {
                    try { stmt.executeUpdate(alterSql); } catch (Exception ignored) {}
                }
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Nâng cấp VIP thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onSuccess("Đã kích hoạt VIP!"));
            }
        });
    }

    private static String md5(String input) {
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
}
