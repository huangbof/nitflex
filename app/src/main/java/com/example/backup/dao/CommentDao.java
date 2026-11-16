package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {

    public static List<Comment> getComments(int movieId) throws SQLException {
        String sql = "SELECT c.*, u.username FROM comments c "
                   + "INNER JOIN users u ON c.user_id = u.id "
                   + "WHERE c.movie_id = ? ORDER BY c.created_at DESC";
        List<Comment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
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
        }
        return list;
    }

    public static boolean addComment(int userId, int movieId, String content) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối MySQL!");
            int validUserId = UserDao.resolveValidUserId(conn, userId);
            int realMovieId = MovieDao.resolveInternalMovieId(conn, movieId);

            String sql = "INSERT INTO comments (user_id, movie_id, content) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, validUserId);
                ps.setInt(2, realMovieId);
                ps.setString(3, content);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
