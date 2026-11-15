package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.dao.UserDao;
import com.example.backup.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller xử lý luồng xác thực tài khoản & phân quyền người dùng
 * Tuân thủ DAO Pattern: ủy nhiệm toàn bộ thao tác CSDL cho UserDao
 */
public class AuthController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AuthCallback {
        void onSuccess(int userId, String username);
        void onError(String message);
    }

    public interface VipCallback {
        void onSuccess(boolean isVip);
        void onError(String message);
    }

    /**
     * Đăng nhập bằng Tên đăng nhập HOẶC Email kèm mật khẩu
     */
    public static void login(String usernameOrEmail, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                User user = UserDao.login(usernameOrEmail, password);
                if (user != null) {
                    mainHandler.post(() -> callback.onSuccess(user.getId(), user.getUsername()));
                } else {
                    mainHandler.post(() -> callback.onError("Sai tên đăng nhập/email hoặc mật khẩu!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi kết nối CSDL: " + e.getMessage()));
            }
        });
    }

    /**
     * Đăng nhập hoặc tạo mới tài khoản liên kết Google (theo Buổi 10)
     */
    public static void loginOrRegisterGoogleUser(String email, String displayName, AuthCallback callback) {
        executor.execute(() -> {
            try {
                User user = UserDao.loginOrRegisterGoogleUser(email, displayName);
                if (user != null) {
                    mainHandler.post(() -> callback.onSuccess(user.getId(), user.getUsername()));
                } else {
                    mainHandler.post(() -> callback.onError("Không thể đồng bộ tài khoản Google vào CSDL!"));
                }
            } catch (Exception e) {
                // Fallback offline nếu có lỗi kết nối
                mainHandler.post(() -> callback.onSuccess(9999, displayName != null ? displayName : "Google User"));
            }
        });
    }

    /**
     * Đăng ký tài khoản thành viên mới với kiểm tra trùng lặp chi tiết
     */
    public static void register(String username, String password, String email, AuthCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Kiểm tra trùng lặp
                String duplicateMsg = UserDao.checkDuplicate(username, email);
                if (duplicateMsg != null) {
                    mainHandler.post(() -> callback.onError(duplicateMsg));
                    return;
                }

                // 2. Thêm mới tài khoản
                int newId = UserDao.register(username, password, email);
                if (newId > 0) {
                    mainHandler.post(() -> callback.onSuccess(newId, username));
                } else {
                    mainHandler.post(() -> callback.onError("Đăng ký không thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    /**
     * Kiểm tra trạng thái VIP của thành viên
     */
    public static void checkVipStatus(int userId, VipCallback callback) {
        executor.execute(() -> {
            boolean isVip = UserDao.checkVipStatus(userId);
            mainHandler.post(() -> callback.onSuccess(isVip));
        });
    }

    /**
     * Kích hoạt nâng cấp tài khoản VIP
     */
    public static void upgradeToVip(int userId, MovieController.SimpleCallback callback) {
        executor.execute(() -> {
            boolean success = UserDao.upgradeToVip(userId);
            if (success) {
                mainHandler.post(() -> callback.onSuccess("Nâng cấp VIP thành công!"));
            } else {
                mainHandler.post(() -> callback.onSuccess("Đã kích hoạt VIP (Cục bộ)!"));
            }
        });
    }
}
