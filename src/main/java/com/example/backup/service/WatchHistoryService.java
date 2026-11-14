package com.example.backup.service;

import com.example.backup.model.Movie;
import com.example.backup.model.WatchHistory;
import com.example.backup.repository.DBConnection;
import com.example.backup.repository.WatchHistoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchHistoryService {

    private final WatchHistoryRepository repo = new WatchHistoryRepository();

    public List<WatchHistory> getHistoryByUser(int userId) {
        List<WatchHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM watch_history WHERE user_id = ? ORDER BY watched_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WatchHistory wh = new WatchHistory(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("movie_id"),
                            rs.getTimestamp("watched_at"),
                            rs.getInt("progress")
                    );
                    list.add(wh);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Movie> getWatchedMovies(int userId) {
        return repo.findByUserId(userId);
    }

    public boolean saveProgress(int userId, int movieId, int progress) {
        return repo.saveOrUpdate(userId, movieId, progress);
    }
}
