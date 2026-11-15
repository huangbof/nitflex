package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.DashboardStats;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object chuyên xử lý các thao tác quản trị hệ thống (CRUD) trong CSDL MySQL
 */
public class AdminDao {

    private static String md5(String input) {
        return UserDao.md5(input);
    }

    private static void ensureVipTable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_vip TINYINT DEFAULT 0");
            st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'user'");
        } catch (Exception ignored) {}
    }

    public static boolean addMovie(Movie m) throws SQLException {
        String sql = "INSERT INTO movies (title, original_title, overview, poster_path, backdrop_path, "
                + "release_date, vote_average, popularity, video_url, genre_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, m.getTitle());
                ps.setString(2, m.getOriginalTitle());
                ps.setString(3, m.getOverview());
                ps.setString(4, m.getPosterPath());
                ps.setString(5, m.getBackdropPath());
                ps.setString(6, m.getReleaseDate());
                ps.setDouble(7, m.getVoteAverage());
                ps.setDouble(8, m.getPopularity() > 0 ? m.getPopularity() : 50.0);
                ps.setString(9, m.getVideoUrl());
                ps.setInt(10, m.getGenreId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean updateMovie(Movie m) throws SQLException {
        String sql = "UPDATE movies SET title = ?, original_title = ?, overview = ?, poster_path = ?, "
                + "backdrop_path = ?, release_date = ?, vote_average = ?, video_url = ?, genre_id = ? "
                + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
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
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean deleteMovie(int movieId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM favorites WHERE movie_id = ?")) {
                ps.setInt(1, movieId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM comments WHERE movie_id = ?")) {
                ps.setInt(1, movieId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM watch_history WHERE movie_id = ?")) {
                ps.setInt(1, movieId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM movie_casts WHERE movie_id = ?")) {
                ps.setInt(1, movieId);
                ps.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM movies WHERE id = ?")) {
                ps.setInt(1, movieId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, email, avatar_url, is_vip, "
                + "IFNULL(created_at, NOW()) as created_at, IFNULL(role, 'user') as role "
                + "FROM users ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            ensureVipTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setAvatarUrl(rs.getString("avatar_url"));
                    u.setVip(rs.getInt("is_vip") == 1);
                    u.setRole(rs.getString("role"));
                    u.setCreatedAt(rs.getString("created_at"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    public static boolean toggleUserVip(int userId, boolean newVipStatus) throws SQLException {
        String sql = "UPDATE users SET is_vip = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            ensureVipTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newVipStatus ? 1 : 0);
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean resetUserPassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, md5(newPassword));
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean deleteUser(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM favorites WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM comments WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM watch_history WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean addCast(Cast c) throws SQLException {
        String sql = "INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, c.getMovieId());
                ps.setString(2, c.getActorName());
                ps.setString(3, c.getCharacterName());
                ps.setString(4, c.getProfilePath());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean deleteCast(int castId) throws SQLException {
        String sql = "DELETE FROM movie_casts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, castId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static List<VipPackage> getVipPackages() throws SQLException {
        return VipPackageDao.getAllVipPackages();
    }

    public static boolean addVipPackage(VipPackage pkg) throws SQLException {
        String sql = "INSERT INTO vip_packages (name, code, price, duration_days, description, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pkg.getName());
                ps.setString(2, pkg.getName().replaceAll("\\s+", "_").toUpperCase());
                ps.setDouble(3, pkg.getPrice());
                ps.setInt(4, pkg.getDurationDays());
                ps.setString(5, pkg.getDescription());
                ps.setBoolean(6, pkg.isActive());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean updateVipPackage(VipPackage pkg) throws SQLException {
        String sql = "UPDATE vip_packages SET name = ?, price = ?, duration_days = ?, description = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pkg.getName());
                ps.setDouble(2, pkg.getPrice());
                ps.setInt(3, pkg.getDurationDays());
                ps.setString(4, pkg.getDescription());
                ps.setBoolean(5, pkg.isActive());
                ps.setInt(6, pkg.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean deleteVipPackage(int packageId) throws SQLException {
        String sql = "DELETE FROM vip_packages WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, packageId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
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

            // 2. Thống kê phim
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

            // 3. Thống kê tương tác
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM comments");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.setTotalComments(rs.getInt(1));
            } catch (Exception ignored) {}

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM movie_ratings");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.setTotalRatings(rs.getInt(1));
            } catch (Exception ignored) {}

            // 4. Doanh thu ước tính
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
                    "SELECT id, tmdb_id, title, original_title, overview, poster_path, backdrop_path, "
                    + "release_date, vote_average, vote_count, popularity, original_language, "
                    + "adult, video_url, genre_id, view_count FROM movies ORDER BY view_count DESC LIMIT 5");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topMovies.add(MovieDao.mapMovie(rs));
                }
            } catch (Exception ignored) {}
            stats.setTopMovies(topMovies);
        }
        return stats;
    }

    public static List<Comment> getAllComments() throws SQLException {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.movie_id, c.content, c.created_at, "
                + "COALESCE(u.username, 'Ẩn danh') as username, "
                + "COALESCE(m.title, 'Phim đã xóa') as movie_title "
                + "FROM comments c "
                + "LEFT JOIN users u ON c.user_id = u.id "
                + "LEFT JOIN movies m ON c.movie_id = m.id "
                + "ORDER BY c.created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
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
                    list.add(c);
                }
            }
        }
        return list;
    }

    public static boolean deleteComment(int commentId) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối CSDL MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, commentId);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
