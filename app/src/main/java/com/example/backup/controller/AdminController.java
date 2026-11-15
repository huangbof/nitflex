package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.dao.AdminDao;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.DashboardStats;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller xử lý luồng thao tác Quản trị viên (Admin Dashboard)
 * Tuân thủ DAO Pattern: ủy nhiệm toàn bộ truy vấn CSDL cho AdminDao
 */
public class AdminController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AdminCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String message);
    }

    public interface VipPackageListCallback {
        void onSuccess(List<VipPackage> packages);
        void onError(String message);
    }

    public interface DashboardStatsCallback {
        void onSuccess(DashboardStats stats);
        void onError(String message);
    }

    public interface CommentListCallback {
        void onSuccess(List<Comment> comments);
        void onError(String message);
    }

    // 1. Phim (Thêm / Sửa / Xóa)
    public static void addMovie(Movie m, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.addMovie(m);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Thêm phim thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Thêm phim thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void updateMovie(Movie m, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.updateMovie(m);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Cập nhật phim thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật phim thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void deleteMovie(int movieId, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.deleteMovie(movieId);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã xóa phim!"));
                } else {
                    mainHandler.post(() -> callback.onError("Xóa phim thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 2. Quản lý Người Dùng
    public static void getAllUsers(UserListCallback callback) {
        executor.execute(() -> {
            try {
                List<User> list = AdminDao.getAllUsers();
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void toggleUserVip(int userId, boolean newVipStatus, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.toggleUserVip(userId, newVipStatus);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã cập nhật trạng thái VIP!"));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void resetUserPassword(int userId, String newPassword, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.resetUserPassword(userId, newPassword);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đặt lại mật khẩu thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void deleteUser(int userId, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.deleteUser(userId);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã xóa người dùng!"));
                } else {
                    mainHandler.post(() -> callback.onError("Xóa thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 3. Diễn viên
    public static void addCast(Cast c, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.addCast(c);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Thêm diễn viên thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Thêm diễn viên thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void deleteCast(int castId, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.deleteCast(castId);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã xóa diễn viên!"));
                } else {
                    mainHandler.post(() -> callback.onError("Xóa thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 4. Gói VIP
    public static void getVipPackages(VipPackageListCallback callback) {
        executor.execute(() -> {
            try {
                List<VipPackage> list = AdminDao.getVipPackages();
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void addVipPackage(VipPackage pkg, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.addVipPackage(pkg);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Thêm gói VIP thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Thêm gói VIP thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void updateVipPackage(VipPackage pkg, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.updateVipPackage(pkg);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Cập nhật gói VIP thành công!"));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void deleteVipPackage(int packageId, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.deleteVipPackage(packageId);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã xóa gói VIP!"));
                } else {
                    mainHandler.post(() -> callback.onError("Xóa thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 5. Thống Kê Tổng Quan Dashboard
    public static void getDashboardStats(DashboardStatsCallback callback) {
        executor.execute(() -> {
            try {
                DashboardStats stats = AdminDao.getDashboardStats();
                mainHandler.post(() -> callback.onSuccess(stats));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 6. Quản Lý Bình Luận
    public static void getAllComments(CommentListCallback callback) {
        executor.execute(() -> {
            try {
                List<Comment> list = AdminDao.getAllComments();
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    public static void deleteComment(int commentId, AdminCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = AdminDao.deleteComment(commentId);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã xóa bình luận!"));
                } else {
                    mainHandler.post(() -> callback.onError("Xóa thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }
}
