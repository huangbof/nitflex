package com.example.backup.repository;

import com.example.backup.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WatchHistoryRepository {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // Helper: map movie columns from JOIN result
    // -----------------------------------------------------------------------
    private Movie mapMovieRow(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setOriginalTitle(rs.getString("original_title"));
        m.setOverview(rs.getString("overview"));
        m.setPosterPath(rs.getString("poster_path"));
        m.setBackdropPath(rs.getString("backdrop_path"));
        m.setReleaseDate(rs.getString("release_date"));
        m.setVoteAverage(rs.getDouble("vote_average"));
        m.setVoteCount(rs.getInt("vote_count"));
        m.setPopularity(rs.getDouble("popularity"));
        m.setOriginalLanguage(rs.getString("original_language"));
        m.setAdult(rs.getBoolean("adult"));
        m.setVideoUrl(rs.getString("video_url"));
        m.setGenreId(rs.getInt("genre_id"));
        return m;
    }

    // -----------------------------------------------------------------------
    // findByUserId — returns movies from watch history (most recent first)
    // -----------------------------------------------------------------------
    public List<Movie> findByUserId(int userId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.* FROM movies m "
                   + "INNER JOIN watch_history wh ON m.id = wh.movie_id "
                   + "WHERE wh.user_id = ? "
                   + "ORDER BY wh.watched_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) movies.add(mapMovieRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    // -----------------------------------------------------------------------
    // saveOrUpdate — INSERT if not exists, UPDATE progress+watched_at if exists
    // -----------------------------------------------------------------------
    public boolean saveOrUpdate(int userId, int movieId, int progress) {
        // Try UPDATE first
        String updateSql = "UPDATE watch_history SET progress = ?, watched_at = NOW() "
                         + "WHERE user_id = ? AND movie_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(updateSql)) {
            ps.setInt(1, progress);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // No existing row — INSERT
        String insertSql = "INSERT INTO watch_history (user_id, movie_id, watched_at, progress) "
                         + "VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement ps = getConn().prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setInt(3, progress);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
