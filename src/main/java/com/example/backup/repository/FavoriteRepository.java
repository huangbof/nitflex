package com.example.backup.repository;

import com.example.backup.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // Helper: map a ResultSet row to a Movie (JOIN result)
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
    // findByUserId — returns movies that the user has favourited
    // -----------------------------------------------------------------------
    public List<Movie> findByUserId(int userId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.* FROM movies m "
                   + "INNER JOIN favorites f ON m.id = f.movie_id "
                   + "WHERE f.user_id = ? "
                   + "ORDER BY f.added_at DESC";
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
    // isFavorite
    // -----------------------------------------------------------------------
    public boolean isFavorite(int userId, int movieId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // add
    // -----------------------------------------------------------------------
    public boolean add(int userId, int movieId) {
        String sql = "INSERT INTO favorites (user_id, movie_id, added_at) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // remove
    // -----------------------------------------------------------------------
    public boolean remove(int userId, int movieId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
