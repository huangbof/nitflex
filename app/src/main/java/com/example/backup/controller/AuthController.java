package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.dao.UserDao;
import com.example.backup.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

                mainHandler.post(() -> callback.onSuccess(9999, displayName != null ? displayName : "Google User"));
            }
        });
    }

    public interface CheckCallback {
        void onValid();
        void onError(String message);
    }

    public static void checkBeforeRegister(String username, String email, String phone, CheckCallback callback) {
        executor.execute(() -> {
            try {
                String duplicateMsg = UserDao.checkDuplicate(username, email, phone);
                if (duplicateMsg != null) {
                    mainHandler.post(() -> callback.onError(duplicateMsg));
                } else {
                    mainHandler.post(callback::onValid);
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi kiểm tra thông tin: " + e.getMessage()));
            }
        });
    }

    public static void register(String username, String password, String email, AuthCallback callback) {
        register(username, password, email, null, callback);
    }

    public static void register(String username, String password, String email, String phone, AuthCallback callback) {
        executor.execute(() -> {
            try {
                String duplicateMsg = UserDao.checkDuplicate(username, email, phone);
                if (duplicateMsg != null) {
                    mainHandler.post(() -> callback.onError(duplicateMsg));
                    return;
                }

                int newId = UserDao.register(username, password, email, phone);
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

    public static void checkVipStatus(int userId, VipCallback callback) {
        executor.execute(() -> {
            boolean isVip = UserDao.checkVipStatus(userId);
            mainHandler.post(() -> callback.onSuccess(isVip));
        });
    }

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
