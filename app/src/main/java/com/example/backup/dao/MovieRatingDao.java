package com.example.backup.dao;

import com.example.backup.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object chuyên xử lý truy vấn bảng movie_ratings trong CSDL MySQL
 */
public class MovieRatingDao {

    public static boolean submitMovieRating(int userId, int movieId, float rating) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);

            String sql = "INSERT INTO movie_ratings (user_id, movie_id, rating) VALUES (?, ?, ?) "
                       + "ON DUPLICATE KEY UPDATE rating = VALUES(rating), created_at = CURRENT_TIMESTAMP";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                ps.setInt(2, realMovieId);
                ps.setFloat(3, rating);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static float[] getMovieRatingInfo(int userId, int movieId) {
        float userRating = 0f;
        float avgRating = 0f;
        int totalVotes = 0;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                int validUserId = UserDao.resolveValidUserId(conn, userId);
                int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);

                // 1. Điểm của user nếu đã đăng nhập
                if (validUserId > 0) {
                    String userSql = "SELECT rating FROM movie_ratings WHERE user_id = ? AND movie_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                        ps.setInt(1, validUserId);
                        ps.setInt(2, realMovieId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) userRating = rs.getFloat("rating");
                        }
                    }
                }

                // 2. Điểm trung bình và tổng lượt vote
                String avgSql = "SELECT AVG(rating) as avg_rating, COUNT(id) as total_votes FROM movie_ratings WHERE movie_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(avgSql)) {
                    ps.setInt(1, realMovieId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            avgRating = rs.getFloat("avg_rating");
                            totalVotes = rs.getInt("total_votes");
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return new float[] { userRating, avgRating, (float) totalVotes };
    }
}
