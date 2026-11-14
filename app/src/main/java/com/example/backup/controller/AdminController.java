package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.DashboardStats;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AdminCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface VipPackageListCallback {
        void onSuccess(List<VipPackage> packages);
        void onError(String error);
    }

    public interface DashboardStatsCallback {
        void onSuccess(DashboardStats stats);
        void onError(String error);
    }

    public interface CommentListCallback {
        void onSuccess(List<Comment> comments);
        void onError(String error);
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(0xFF & b);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    // =========================================================================
    // 1. QUẢN LÝ PHIM (MOVIES)
    // =========================================================================

    public static void addMovie(Movie m, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "INSERT INTO movies (title, original_title, overview, poster_path, backdrop_path, "
                       + "release_date, vote_average, vote_count, popularity, original_language, video_url, genre_id) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, m.getTitle());
                    ps.setString(2, m.getOriginalTitle() != null ? m.getOriginalTitle() : m.getTitle());
                    ps.setString(3, m.getOverview() != null ? m.getOverview() : "");
                    ps.setString(4, m.getPosterPath() != null ? m.getPosterPath() : "");
                    ps.setString(5, m.getBackdropPath() != null ? m.getBackdropPath() : m.getPosterPath());
                    ps.setString(6, m.getReleaseDate() != null ? m.getReleaseDate() : "2024-01-01");
                    ps.setDouble(7, m.getVoteAverage());
                    ps.setInt(8, m.getVoteCount() > 0 ? m.getVoteCount() : 100);
                    ps.setDouble(9, m.getPopularity() > 0 ? m.getPopularity() : 100.0);
                    ps.setString(10, m.getOriginalLanguage() != null ? m.getOriginalLanguage() : "vi");
                    ps.setString(11, m.getVideoUrl() != null ? m.getVideoUrl() : "");
                    ps.setInt(12, m.getGenreId() > 0 ? m.getGenreId() : 28);

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        mainHandler.post(() -> callback.onSuccess("Đã thêm phim mới thành công!"));
                    } else {
                        mainHandler.post(() -> callback.onError("Không thể thêm phim!"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi thêm phim: " + e.getMessage()));
            }
        });
    }

    public static void updateMovie(Movie m, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "UPDATE movies SET title = ?, original_title = ?, overview = ?, poster_path = ?, "
                       + "backdrop_path = ?, release_date = ?, vote_average = ?, video_url = ?, genre_id = ? "
                       + "WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, m.getTitle());
                    ps.setString(2, m.getOriginalTitle());
                    ps.setString(3, m.getOverview());
                    ps.setString(4, m.getPosterPath());
                    ps.setString(5, m.getBackdropPath());
                    ps.setString(6, m.getReleaseDate());
                    ps.setDouble(7, m.getVoteAverage());
                    ps.setString(8, m.getVideoUrl());
                    ps.setInt(9, m.getGenreId());
                    ps.setInt(10, m.getId());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        mainHandler.post(() -> callback.onSuccess("Đã cập nhật thông tin phim!"));
                    } else {
                        mainHandler.post(() -> callback.onError("Không tìm thấy phim để cập nhật!"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi cập nhật phim: " + e.getMessage()));
            }
        });
    }

    public static void deleteMovie(int movieId, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM movies WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, movieId);
                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        mainHandler.post(() -> callback.onSuccess("Đã xóa phim thành công!"));
                    } else {
                        mainHandler.post(() -> callback.onError("Không tìm thấy phim để xóa!"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi xóa phim: " + e.getMessage()));
            }
        });
    }

    // =========================================================================
    // 2. QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG (USERS)
    // =========================================================================

    public static void getAllUsers(UserListCallback callback) {
        executor.execute(() -> {
            List<User> list = new ArrayList<>();
            // Đảm bảo có cột role và is_vip
            String sql = "SELECT id, username, email, avatar_url, is_vip, "
                       + "IFNULL(created_at, NOW()) as created_at FROM users ORDER BY id ASC";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        u.setAvatarUrl(rs.getString("avatar_url"));
                        u.setVip(rs.getInt("is_vip") == 1);
                        u.setRole(u.getUsername().equalsIgnoreCase("admin") ? "admin" : "user");
                        u.setCreatedAt(rs.getString("created_at"));
                        list.add(u);
                    }
                    mainHandler.post(() -> callback.onSuccess(list));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tải danh sách người dùng: " + e.getMessage()));
            }
        });
    }

    public static void toggleUserVip(int userId, boolean newVipStatus, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "UPDATE users SET is_vip = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, newVipStatus ? 1 : 0);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess(newVipStatus ? "Đã nâng cấp VIP cho tài khoản!" : "Đã hủy gói VIP của tài khoản!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi cập nhật VIP: " + e.getMessage()));
            }
        });
    }

    public static void resetUserPassword(int userId, String newPassword, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, md5(newPassword));
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã đặt lại mật khẩu thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi reset mật khẩu: " + e.getMessage()));
            }
        });
    }

    public static void deleteUser(int userId, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã xóa tài khoản thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi xóa tài khoản: " + e.getMessage()));
            }
        });
    }

    // =========================================================================
    // 3. QUẢN LÝ DIỄN VIÊN (CASTS)
    // =========================================================================

    public static void addCast(Cast c, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                int realMovieId = MovieController.resolveInternalMovieId(conn, c.getMovieId());
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, realMovieId);
                    ps.setString(2, c.getActorName());
                    ps.setString(3, c.getCharacterName() != null ? c.getCharacterName() : "");
                    ps.setString(4, c.getProfilePath() != null ? c.getProfilePath() : "");
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã thêm diễn viên vào phim!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi thêm diễn viên: " + e.getMessage()));
            }
        });
    }

    public static void deleteCast(int castId, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM movie_casts WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, castId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã xóa diễn viên!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi xóa diễn viên: " + e.getMessage()));
            }
        });
    }

    // =========================================================================
    // 4. QUẢN LÝ CÁC GÓI VIP (VIP PACKAGES)
    // =========================================================================

    private static void ensureVipTable(Connection conn) {
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS vip_packages ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "code VARCHAR(50) UNIQUE NOT NULL, "
                    + "price DECIMAL(10, 2) NOT NULL, "
                    + "duration_days INT NOT NULL, "
                    + "description VARCHAR(255), "
                    + "is_active TINYINT DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            // Kiểm tra nếu bảng chưa có gói nào thì chèn các gói mẫu mặc định
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM vip_packages")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.execute("INSERT INTO vip_packages (name, code, price, duration_days, description, is_active) VALUES "
                            + "('VIP Tháng', 'VIP_1M', 59000, 30, 'Mở khóa toàn bộ phim điểm cao > 8.0 TMDB trong 30 ngày', 1), "
                            + "('VIP 6 Tháng', 'VIP_6M', 299000, 180, 'Tiết kiệm 15% • Thưởng thức phim 4K không quảng cáo 6 tháng', 1), "
                            + "('VIP Năm Hoàng Gia', 'VIP_1Y', 499000, 365, 'Đặc quyền tối thượng • Phim bom tấn > 8.0 & Âm thanh vòm Dolby', 1)");
                }
            }
        } catch (Exception ignored) {}
    }

    public static void getVipPackages(VipPackageListCallback callback) {
        executor.execute(() -> {
            List<VipPackage> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                ensureVipTable(conn);
                String sql = "SELECT * FROM vip_packages ORDER BY price ASC";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        VipPackage p = new VipPackage();
                        p.setId(rs.getInt("id"));
                        p.setName(rs.getString("name"));
                        p.setCode(rs.getString("code"));
                        p.setPrice(rs.getDouble("price"));
                        p.setDurationDays(rs.getInt("duration_days"));
                        p.setDescription(rs.getString("description"));
                        p.setActive(rs.getInt("is_active") == 1);
                        p.setCreatedAt(rs.getString("created_at"));
                        list.add(p);
                    }
                    mainHandler.post(() -> callback.onSuccess(list));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tải gói VIP: " + e.getMessage()));
            }
        });
    }

    public static void addVipPackage(VipPackage pkg, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "INSERT INTO vip_packages (name, code, price, duration_days, description, is_active) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                ensureVipTable(conn);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, pkg.getName());
                    ps.setString(2, pkg.getCode());
                    ps.setDouble(3, pkg.getPrice());
                    ps.setInt(4, pkg.getDurationDays());
                    ps.setString(5, pkg.getDescription() != null ? pkg.getDescription() : "");
                    ps.setInt(6, pkg.isActive() ? 1 : 0);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã thêm gói VIP mới thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi thêm gói VIP: " + e.getMessage()));
            }
        });
    }

    public static void updateVipPackage(VipPackage pkg, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "UPDATE vip_packages SET name = ?, price = ?, duration_days = ?, description = ?, is_active = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                ensureVipTable(conn);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, pkg.getName());
                    ps.setDouble(2, pkg.getPrice());
                    ps.setInt(3, pkg.getDurationDays());
                    ps.setString(4, pkg.getDescription());
                    ps.setInt(5, pkg.isActive() ? 1 : 0);
                    ps.setInt(6, pkg.getId());
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã cập nhật gói VIP!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi cập nhật gói VIP: " + e.getMessage()));
            }
        });
    }

    public static void deleteVipPackage(int packageId, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM vip_packages WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, packageId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã xóa gói VIP thành công!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi xóa gói VIP: " + e.getMessage()));
            }
        });
    }

    // =========================================================================
    // 5. THỐNG KÊ TỔNG QUAN & DOANH THU (DASHBOARD ANALYTICS)
    // =========================================================================

    public static void getDashboardStats(DashboardStatsCallback callback) {
        executor.execute(() -> {
            DashboardStats stats = new DashboardStats();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL MySQL!"));
                    return;
                }
                ensureVipTable(conn);

                // 1. Thống kê người dùng (Tổng & VIP)
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) as total, SUM(CASE WHEN is_vip = 1 THEN 1 ELSE 0 END) as vips FROM users");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalUsers(rs.getInt("total"));
                        stats.setVipUsers(rs.getInt("vips"));
                    }
                }

                // 2. Thống kê phim (Tổng, Phim VIP > 8.0, Lượt xem tích lũy)
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) as total, "
                        + "SUM(CASE WHEN vote_average > 8.0 THEN 1 ELSE 0 END) as vips, "
                        + "IFNULL(SUM(view_count), 0) as views FROM movies");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalMovies(rs.getInt("total"));
                        stats.setVipMovies(rs.getInt("vips"));
                        stats.setTotalViews(rs.getInt("views"));
                    }
                }

                // 3. Thống kê tương tác (Bình luận & Đánh giá)
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM comments");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.setTotalComments(rs.getInt(1));
                } catch (Exception ignored) {}

                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM movie_ratings");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stats.setTotalRatings(rs.getInt(1));
                } catch (Exception ignored) {}

                // 4. Doanh thu ước tính (Dựa trên số VIP user và giá gói trung bình)
                double avgPkgPrice = 179000.0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IFNULL(AVG(price), 179000) FROM vip_packages WHERE is_active = 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double p = rs.getDouble(1);
                        if (p > 0) avgPkgPrice = p;
                    }
                } catch (Exception ignored) {}
                stats.setEstimatedRevenue(stats.getVipUsers() * avgPkgPrice);

                // 5. Top 5 phim có lượt xem cao nhất
                List<Movie> topMovies = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, title, original_title, poster_path, vote_average, view_count, release_date "
                        + "FROM movies ORDER BY view_count DESC, vote_average DESC LIMIT 5");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movie m = new Movie();
                        m.setId(rs.getInt("id"));
                        m.setTitle(rs.getString("title"));
                        m.setOriginalTitle(rs.getString("original_title"));
                        m.setPosterPath(rs.getString("poster_path"));
                        m.setVoteAverage(rs.getDouble("vote_average"));
                        m.setViewCount(rs.getInt("view_count"));
                        m.setReleaseDate(rs.getString("release_date"));
                        topMovies.add(m);
                    }
                } catch (Exception ignored) {}
                stats.setTopMovies(topMovies);

                mainHandler.post(() -> callback.onSuccess(stats));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi tải thống kê: " + e.getMessage()));
            }
        });
    }

    public static void getAllComments(CommentListCallback callback) {
        executor.execute(() -> {
            List<Comment> list = new ArrayList<>();
            String sql = "SELECT c.id, c.user_id, c.movie_id, c.content, c.created_at, "
                    + "COALESCE(u.username, 'Ẩn danh') AS username, "
                    + "COALESCE(m.title, 'Phim đã xóa') AS movie_title "
                    + "FROM comments c "
                    + "LEFT JOIN users u ON c.user_id = u.id "
                    + "LEFT JOIN movies m ON c.movie_id = m.id "
                    + "ORDER BY c.created_at DESC";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Comment c = new Comment();
                        c.setId(rs.getInt("id"));
                        c.setUserId(rs.getInt("user_id"));
                        c.setMovieId(rs.getInt("movie_id"));
                        c.setContent(rs.getString("content"));
                        c.setCreatedAt(rs.getString("created_at"));
                        c.setUsername(rs.getString("username"));
                        c.setMovieTitle(rs.getString("movie_title"));
                        list.add(c);
                    }
                    mainHandler.post(() -> callback.onSuccess(list));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi lấy danh sách bình luận: " + e.getMessage()));
            }
        });
    }

    public static void deleteComment(int commentId, AdminCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM comments WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối CSDL"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, commentId);
                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        mainHandler.post(() -> callback.onSuccess("Đã xóa bình luận thành công"));
                    } else {
                        mainHandler.post(() -> callback.onError("Không tìm thấy bình luận cần xóa"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi xóa bình luận: " + e.getMessage()));
            }
        });
    }
}
