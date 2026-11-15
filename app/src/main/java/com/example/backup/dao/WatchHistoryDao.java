package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object chuyên xử lý truy vấn bảng watch_history trong CSDL MySQL
 */
public class WatchHistoryDao {

    public static void saveWatchProgress(int userId, int movieId, int progressSeconds, int durationSeconds) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE watch_history ADD COLUMN IF NOT EXISTS duration INT DEFAULT 0");
            } catch (Exception ignored) {}

            String sql = "INSERT INTO watch_history (user_id, movie_id, progress, duration) VALUES (?, ?, ?, ?) "
                       + "ON DUPLICATE KEY UPDATE progress = VALUES(progress), "
                       + "duration = CASE WHEN VALUES(duration) > 0 THEN VALUES(duration) ELSE duration END, "
                       + "watched_at = CURRENT_TIMESTAMP";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                ps.setInt(2, realMovieId);
                ps.setInt(3, progressSeconds);
                ps.setInt(4, durationSeconds);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    public static int getWatchProgress(int userId, int movieId) {
        int progress = 0;
        String sql = "SELECT progress FROM watch_history WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                int validUserId = UserDao.resolveValidUserId(conn, userId);
                int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    ps.setInt(2, realMovieId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            progress = rs.getInt("progress");
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return progress;
    }

    public static List<Movie> getWatchHistory(int userId) throws SQLException {
        String sql = "SELECT m.*, wh.progress as watch_progress, IFNULL(wh.duration, 0) as watch_duration FROM movies m "
                   + "INNER JOIN watch_history wh ON m.id = wh.movie_id "
                   + "WHERE wh.user_id = ? ORDER BY wh.watched_at DESC";
        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(MovieDao.mapMovie(rs));
                    }
                }
            }
        }
        return list;
    }

    public static List<Movie> getContinueWatchingMovies(int userId) throws SQLException {
        String sql = "SELECT m.*, wh.progress as watch_progress, IFNULL(wh.duration, 0) as watch_duration FROM movies m "
                   + "INNER JOIN watch_history wh ON m.id = wh.movie_id "
                   + "WHERE wh.user_id = ? AND wh.progress >= 10 "
                   + "  AND (wh.duration <= 0 OR wh.progress < (wh.duration - 15)) "
                   + "ORDER BY wh.watched_at DESC LIMIT 15";
        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(MovieDao.mapMovie(rs));
                    }
                }
            }
        }
        return list;
    }

    public static boolean deleteWatchHistoryItem(int userId, int movieId) throws SQLException {
        String sql = "DELETE FROM watch_history WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                ps.setInt(2, realMovieId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean clearWatchHistory(int userId) throws SQLException {
        String sql = "DELETE FROM watch_history WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
