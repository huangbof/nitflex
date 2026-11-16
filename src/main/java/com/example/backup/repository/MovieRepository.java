package com.example.backup.repository;

import com.example.backup.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieRepository {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    private Movie mapRow(ResultSet rs) throws SQLException {
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

    public List<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY popularity DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) movies.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> findByGenre(int genreId) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE genre_id = ? ORDER BY popularity DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, genreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) movies.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public Movie findById(int id) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Movie> search(String keyword) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title LIKE ? ORDER BY popularity DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) movies.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> findTopRated() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY vote_average DESC LIMIT 20";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) movies.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> findPopular() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY popularity DESC LIMIT 20";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) movies.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> findLatest() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY release_date DESC LIMIT 20";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) movies.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public void incrementViewCount(int movieId) {
        String sql = "UPDATE movies SET view_count = view_count + 1 WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
