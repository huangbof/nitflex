package com.example.backup.dao;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieDao {

    public static Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getInt("id"));
        m.setTmdbId(rs.getInt("tmdb_id"));
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
        m.setViewCount(rs.getInt("view_count"));
        try {
            m.setWatchProgress(rs.getInt("watch_progress"));
            m.setWatchDuration(rs.getInt("watch_duration"));
        } catch (Exception ignored) {}
        return m;
    }

    public static int resolveInternalMovieId(Connection conn, int movieId) {
        if (movieId <= 0 || conn == null) return movieId;
        String sql = "SELECT id FROM movies WHERE id = ? OR tmdb_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception ignored) {}
        return movieId;
    }

    public static List<Movie> getMovies(String type) throws SQLException {
        String sql;
        if ("top_rated".equalsIgnoreCase(type)) {
            sql = "SELECT * FROM movies ORDER BY vote_average DESC LIMIT 20";
        } else if ("popular".equalsIgnoreCase(type)) {
            sql = "SELECT * FROM movies ORDER BY popularity DESC LIMIT 20";
        } else if ("latest".equalsIgnoreCase(type)) {
            sql = "SELECT * FROM movies ORDER BY release_date DESC LIMIT 20";
        } else {
            sql = "SELECT * FROM movies ORDER BY id ASC";
        }

        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        }
        return list;
    }

    public static List<Movie> getAllMovies() throws SQLException {
        return getMovies("all");
    }

    public static List<Movie> getMoviesByGenre(int genreId) throws SQLException {
        String sql = "SELECT * FROM movies WHERE genre_id = ? ORDER BY popularity DESC";
        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, genreId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMovie(rs));
                    }
                }
            }
        }
        return list;
    }

    public static Movie getMovieDetail(int id) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ? OR tmdb_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setInt(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Movie m = mapMovie(rs);
                        try (PreparedStatement psView = conn.prepareStatement("UPDATE movies SET view_count = view_count + 1 WHERE id = ?")) {
                            psView.setInt(1, m.getId());
                            psView.executeUpdate();
                        } catch (Exception ignored) {}
                        return m;
                    }
                }
            }
        }
        return null;
    }

    public static List<Movie> searchMoviesAdvanced(String keyword, int genreId, String sortType) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR original_title LIKE ?) ");
            String param = "%" + keyword.trim() + "%";
            params.add(param);
            params.add(param);
        }

        if (genreId > 0) {
            sql.append("AND genre_id = ? ");
            params.add(genreId);
        }

        if ("rating".equalsIgnoreCase(sortType)) {
            sql.append("ORDER BY vote_average DESC ");
        } else if ("latest".equalsIgnoreCase(sortType)) {
            sql.append("ORDER BY release_date DESC ");
        } else {
            sql.append("ORDER BY popularity DESC ");
        }
        sql.append("LIMIT 50");

        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    Object p = params.get(i);
                    if (p instanceof Integer) {
                        ps.setInt(i + 1, (Integer) p);
                    } else {
                        ps.setString(i + 1, String.valueOf(p));
                    }
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMovie(rs));
                    }
                }
            }
        }
        return list;
    }

    public static List<Movie> searchMoviesMultiFilter(String keyword, int genreId, String sortType,
                                                      String vipFilter, String yearFilter, double minRating) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR original_title LIKE ?) ");
            String param = "%" + keyword.trim() + "%";
            params.add(param);
            params.add(param);
        }

        if (genreId > 0) {
            sql.append("AND genre_id = ? ");
            params.add(genreId);
        }

        if ("vip".equalsIgnoreCase(vipFilter)) {
            sql.append("AND vote_average > 8.0 ");
        } else if ("free".equalsIgnoreCase(vipFilter)) {
            sql.append("AND vote_average <= 8.0 ");
        }

        if ("2020s".equalsIgnoreCase(yearFilter)) {
            sql.append("AND YEAR(release_date) >= 2020 ");
        } else if ("2010s".equalsIgnoreCase(yearFilter)) {
            sql.append("AND YEAR(release_date) BETWEEN 2010 AND 2019 ");
        } else if ("classic".equalsIgnoreCase(yearFilter)) {
            sql.append("AND YEAR(release_date) < 2010 ");
        }

        if (minRating > 0.0) {
            sql.append("AND vote_average >= ? ");
            params.add(minRating);
        }

        if ("rating".equalsIgnoreCase(sortType)) {
            sql.append("ORDER BY vote_average DESC ");
        } else if ("latest".equalsIgnoreCase(sortType)) {
            sql.append("ORDER BY release_date DESC ");
        } else if ("views".equalsIgnoreCase(sortType)) {
            sql.append("ORDER BY view_count DESC ");
        } else {
            sql.append("ORDER BY popularity DESC ");
        }
        sql.append("LIMIT 60");

        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) throw new SQLException("Không thể kết nối đến MySQL!");
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    Object p = params.get(i);
                    if (p instanceof Integer) {
                        ps.setInt(i + 1, (Integer) p);
                    } else if (p instanceof Double) {
                        ps.setDouble(i + 1, (Double) p);
                    } else {
                        ps.setString(i + 1, String.valueOf(p));
                    }
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMovie(rs));
                    }
                }
            }
        }
        return list;
    }

    public static void incrementMovieViewCount(int movieId) {
        if (movieId <= 0) return;
        String sql = "UPDATE movies SET view_count = view_count + 1 WHERE id = ? OR tmdb_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, movieId);
                    ps.setInt(2, movieId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }
}
