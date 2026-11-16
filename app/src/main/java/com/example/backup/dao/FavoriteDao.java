package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    public static List<Movie> getFavorites(int userId) throws SQLException {
        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            String sql = "SELECT m.* FROM movies m "
                       + "INNER JOIN favorites f ON m.id = f.movie_id "
                       + "WHERE f.user_id = ? ORDER BY f.added_at DESC";
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

    public static String toggleFavorite(int userId, int movieId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);

            String checkSql = "SELECT id FROM favorites WHERE user_id = ? AND movie_id = ?";
            String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND movie_id = ?";
            String insertSql = "INSERT INTO favorites (user_id, movie_id) VALUES (?, ?)";

            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, validUserId);
                psCheck.setInt(2, realMovieId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) exists = true;
                }
            }

            if (exists) {
                try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                    psDel.setInt(1, validUserId);
                    psDel.setInt(2, realMovieId);
                    psDel.executeUpdate();
                    return "removed";
                }
            } else {
                try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                    psIns.setInt(1, validUserId);
                    psIns.setInt(2, realMovieId);
                    psIns.executeUpdate();
                    return "added";
                }
            }
        }
    }
}
