package com.example.backup.repository;

import com.example.backup.model.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // Helper: map row (JOIN with users)
    // -----------------------------------------------------------------------
    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setMovieId(rs.getInt("movie_id"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUsername(rs.getString("username"));
        return c;
    }

    // -----------------------------------------------------------------------
    // findByMovieId — JOIN users to get username
    // -----------------------------------------------------------------------
    public List<Comment> findByMovieId(int movieId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c "
                   + "INNER JOIN users u ON c.user_id = u.id "
                   + "WHERE c.movie_id = ? "
                   + "ORDER BY c.created_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) comments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // -----------------------------------------------------------------------
    // save
    // -----------------------------------------------------------------------
    public boolean save(Comment comment) {
        String sql = "INSERT INTO comments (user_id, movie_id, content, created_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, comment.getUserId());
            ps.setInt(2, comment.getMovieId());
            ps.setString(3, comment.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // delete — only the owner can delete their own comment
    // -----------------------------------------------------------------------
    public boolean delete(int commentId, int userId) {
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
