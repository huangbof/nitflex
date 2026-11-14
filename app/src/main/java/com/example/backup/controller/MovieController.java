package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.Genre;
import com.example.backup.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface MovieListCallback {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }

    public interface ProgressCallback {
        void onProgressLoaded(int progressSeconds);
    }

    public interface MovieDetailCallback {
        void onSuccess(Movie movie);
        void onError(String error);
    }

    public interface GenreListCallback {
        void onSuccess(List<Genre> genres);
        void onError(String error);
    }

    public interface CommentListCallback {
        void onSuccess(List<Comment> comments);
        void onError(String error);
    }

    public interface RatingCallback {
        void onRatingLoaded(float userRating, float averageRating, int totalVotes);
    }

    public interface CastListCallback {
        void onSuccess(List<Cast> casts);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Map row từ ResultSet sang Movie model
    private static Movie mapMovie(ResultSet rs) throws SQLException {
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

    // Helper resolve movieId (chuyển đổi tmdb_id sang id khóa chính trong CSDL nếu cần)
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

    // Helper resolve userId (đảm bảo userId luôn tồn tại trong bảng users để tránh lỗi Foreign Key)
    public static int resolveValidUserId(Connection conn, int userId) {
        if (conn == null) return userId > 0 ? userId : 1;
        // 1. Kiểm tra userId hiện tại đã có trong bảng users chưa
        if (userId > 0) {
            String checkSql = "SELECT id FROM users WHERE id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Nếu chưa có (ví dụ 9999 từ Google fallback hoặc tài khoản cũ), tự động tạo bản ghi dự phòng
        int targetId = userId > 0 ? userId : 9999;
        String insertSql = "INSERT INTO users (id, username, password_hash) VALUES (?, ?, 'nitflex_pass') "
                         + "ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, targetId);
            ps.setString(2, "user_" + targetId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return rs.getInt(1);
                }
            }
            return targetId;
        } catch (Exception ignored) {}

        // 3. Fallback lấy ID người dùng đầu tiên có sẵn trong bảng users (ví dụ admin id = 1)
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users ORDER BY id ASC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception ignored) {}

        return 1;
    }


    // 1. Lấy danh sách phim (tất cả hoặc theo type: top_rated, popular, latest, all)
    public static void getMovies(String type, MovieListCallback callback) {
        executor.execute(() -> {
            String sql;
            if ("top_rated".equalsIgnoreCase(type)) {
                sql = "SELECT * FROM movies ORDER BY vote_average DESC LIMIT 30";
            } else if ("latest".equalsIgnoreCase(type)) {
                sql = "SELECT * FROM movies ORDER BY release_date DESC LIMIT 30";
            } else if ("all".equalsIgnoreCase(type)) {
                sql = "SELECT * FROM movies ORDER BY id ASC";
            } else {
                sql = "SELECT * FROM movies ORDER BY popularity DESC LIMIT 30";
            }

            List<Movie> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL XAMPP!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMovie(rs));
                    }
                    mainHandler.post(() -> callback.onSuccess(list));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 1b. Lấy toàn bộ phim (tiện ích cho tìm kiếm & lịch sử xem)
    public static void getAllMovies(MovieListCallback callback) {
        getMovies("all", callback);
    }

    // 2. Lấy phim theo thể loại
    public static void getMoviesByGenre(int genreId, MovieListCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT * FROM movies WHERE genre_id = ? ORDER BY popularity DESC";
            List<Movie> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, genreId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapMovie(rs));
                        }
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 3. Lấy chi tiết 1 phim + Tăng lượt xem
    public static void getMovieDetail(int id, MovieDetailCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT * FROM movies WHERE id = ?";
            String updateViewSql = "UPDATE movies SET view_count = view_count + 1 WHERE id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                // Tăng view
                try (PreparedStatement psUp = conn.prepareStatement(updateViewSql)) {
                    psUp.setInt(1, id);
                    psUp.executeUpdate();
                }
                // Lấy chi tiết
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Movie movie = mapMovie(rs);
                            mainHandler.post(() -> callback.onSuccess(movie));
                        } else {
                            mainHandler.post(() -> callback.onError("Không tìm thấy phim"));
                        }
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 4. Tìm kiếm phim theo tên
    public static void searchMovies(String keyword, MovieListCallback callback) {
        searchMoviesAdvanced(keyword, 0, "popularity", callback);
    }

    // 4b. Tìm kiếm phim kết hợp Lọc thể loại & Sắp xếp nâng cao
    public static void searchMoviesAdvanced(String keyword, int genreId, String sortType, MovieListCallback callback) {
        executor.execute(() -> {
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
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
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
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


        // 4c. Tìm kiếm phim Đa Tiêu Chí (Từ khóa, Thể loại, Sắp xếp, VIP/Free, Thời kỳ phát hành, Điểm tối thiểu)
    public static void searchMoviesMultiFilter(String keyword, int genreId, String sortType,
                                              String vipFilter, String yearFilter, double minRating,
                                              MovieListCallback callback) {
        executor.execute(() -> {
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
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
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
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 5. Lấy danh sách thể loại
    public static void getGenres(GenreListCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT * FROM genres ORDER BY name ASC";
            List<Genre> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Genre(rs.getInt("id"), rs.getString("name")));
                    }
                    mainHandler.post(() -> callback.onSuccess(list));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 6. Lấy danh sách phim yêu thích
    public static void getFavorites(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            List<Movie> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                String sql = "SELECT m.* FROM movies m "
                           + "INNER JOIN favorites f ON m.id = f.movie_id "
                           + "WHERE f.user_id = ? ORDER BY f.added_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapMovie(rs));
                        }
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 7. Toggle yêu thích (nếu có thì xóa, chưa có thì thêm) - Đảm bảo Foreign Key an toàn 100%
    public static void toggleFavorite(int userId, int movieId, SimpleCallback callback) {
        executor.execute(() -> {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                int realMovieId = resolveInternalMovieId(conn, movieId);

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
                        mainHandler.post(() -> callback.onSuccess("removed"));
                    }
                } else {
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, validUserId);
                        psIns.setInt(2, realMovieId);
                        psIns.executeUpdate();
                        mainHandler.post(() -> callback.onSuccess("added"));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 8. Lấy bình luận của 1 phim
    public static void getComments(int movieId, CommentListCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT c.*, u.username FROM comments c "
                       + "INNER JOIN users u ON c.user_id = u.id "
                       + "WHERE c.movie_id = ? ORDER BY c.created_at DESC";
            List<Comment> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
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
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 9. Thêm bình luận
    public static void addComment(int userId, int movieId, String content, SimpleCallback callback) {
        executor.execute(() -> {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                int realMovieId = resolveInternalMovieId(conn, movieId);

                String sql = "INSERT INTO comments (user_id, movie_id, content) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    ps.setInt(2, realMovieId);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã thêm bình luận"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 9b. Gửi đánh giá sao của người dùng (1 - 5 sao)
    public static void submitMovieRating(int userId, int movieId, float rating, SimpleCallback callback) {
        executor.execute(() -> {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                int realMovieId = resolveInternalMovieId(conn, movieId);

                String sql = "INSERT INTO movie_ratings (user_id, movie_id, rating) VALUES (?, ?, ?) "
                           + "ON DUPLICATE KEY UPDATE rating = VALUES(rating), created_at = CURRENT_TIMESTAMP";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    ps.setInt(2, realMovieId);
                    ps.setFloat(3, rating);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã lưu đánh giá!"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 9c. Lấy thông tin đánh giá của phim (Điểm của bạn + Điểm trung bình cộng đồng)
    public static void getMovieRatingInfo(int userId, int movieId, RatingCallback callback) {
        executor.execute(() -> {
            float userRating = 0f;
            float avgRating = 0f;
            int totalVotes = 0;

            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    int validUserId = resolveValidUserId(conn, userId);
                    int realMovieId = resolveInternalMovieId(conn, movieId);
                    // 1. Lấy điểm user đã chấm (nếu đã đăng nhập)
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

                    // 2. Lấy điểm trung bình và tổng số lượt đánh giá
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

            final float finalUserRating = userRating;
            final float finalAvgRating = avgRating;
            final int finalTotalVotes = totalVotes;
            mainHandler.post(() -> callback.onRatingLoaded(finalUserRating, finalAvgRating, finalTotalVotes));
        });
    }

    // 10. Lưu tiến độ xem (tự động lưu cả vị trí hiện tại và tổng thời lượng)
    public static void saveWatchProgress(int userId, int movieId, int progressSeconds) {
        saveWatchProgress(userId, movieId, progressSeconds, 0);
    }

    public static void saveWatchProgress(int userId, int movieId, int progressSeconds, int durationSeconds) {
        executor.execute(() -> {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                int validUserId = resolveValidUserId(conn, userId);
                int realMovieId = resolveInternalMovieId(conn, movieId);

                // Đảm bảo cột duration tồn tại trong watch_history
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE watch_history ADD COLUMN duration INT DEFAULT 0");
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
        });
    }

    // 10b. Lấy tiến độ đã xem trước đó (để tiếp tục phát)
    public static void getWatchProgress(int userId, int movieId, ProgressCallback callback) {
        executor.execute(() -> {
            int progress = 0;
            String sql = "SELECT progress FROM watch_history WHERE user_id = ? AND movie_id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    int validUserId = resolveValidUserId(conn, userId);
                    int realMovieId = resolveInternalMovieId(conn, movieId);
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
            final int finalProgress = progress;
            mainHandler.post(() -> callback.onProgressLoaded(finalProgress));
        });
    }

    // 11. Lấy toàn bộ lịch sử xem phim (kèm tiến độ xem & thời lượng)
    public static void getWatchHistory(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT m.*, wh.progress as watch_progress, IFNULL(wh.duration, 0) as watch_duration FROM movies m "
                       + "INNER JOIN watch_history wh ON m.id = wh.movie_id "
                       + "WHERE wh.user_id = ? ORDER BY wh.watched_at DESC";
            List<Movie> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapMovie(rs));
                        }
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 11b. Lấy danh sách phim đang xem dở (Tiếp tục xem - progress >= 10s)
    public static void getContinueWatchingMovies(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            String sql = "SELECT m.*, wh.progress as watch_progress, IFNULL(wh.duration, 0) as watch_duration FROM movies m "
                       + "INNER JOIN watch_history wh ON m.id = wh.movie_id "
                       + "WHERE wh.user_id = ? AND wh.progress >= 10 "
                       + "ORDER BY wh.watched_at DESC LIMIT 15";
            List<Movie> list = new ArrayList<>();
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapMovie(rs));
                        }
                        mainHandler.post(() -> callback.onSuccess(list));
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 12. Xóa 1 phim khỏi lịch sử xem
    public static void deleteWatchHistoryItem(int userId, int movieId, SimpleCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM watch_history WHERE user_id = ? AND movie_id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                int realMovieId = resolveInternalMovieId(conn, movieId);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    ps.setInt(2, realMovieId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã xóa khỏi lịch sử"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 12b. Xóa toàn bộ lịch sử xem
    public static void clearWatchHistory(int userId, SimpleCallback callback) {
        executor.execute(() -> {
            String sql = "DELETE FROM watch_history WHERE user_id = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                int validUserId = resolveValidUserId(conn, userId);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, validUserId);
                    ps.executeUpdate();
                    mainHandler.post(() -> callback.onSuccess("Đã xóa toàn bộ lịch sử"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }


    // 13. Lấy danh sách diễn viên của phim trực tiếp từ CSDL MySQL (bảng movie_casts)
    public static void getMovieCasts(int movieId, CastListCallback callback) {
        executor.execute(() -> {
            List<Cast> list = new ArrayList<>();
            String sql = "SELECT * FROM movie_casts "
                       + "WHERE movie_id = ? "
                       + "   OR movie_id IN (SELECT id FROM movies WHERE tmdb_id = ?) "
                       + "ORDER BY id ASC";

            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    mainHandler.post(() -> callback.onError("Không thể kết nối MySQL!"));
                    return;
                }
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, movieId);
                    stmt.setInt(2, movieId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Cast c = new Cast();
                            c.setId(rs.getInt("id"));
                            c.setMovieId(rs.getInt("movie_id"));
                            c.setActorName(rs.getString("actor_name"));
                            c.setCharacterName(rs.getString("character_name"));
                            c.setProfilePath(rs.getString("profile_path"));
                            list.add(c);
                        }
                    }
                }
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL khi truy vấn diễn viên: " + e.getMessage()));
            }
        });
    }
}
