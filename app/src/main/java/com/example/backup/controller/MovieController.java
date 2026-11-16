package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.backup.dao.CastDao;
import com.example.backup.dao.CommentDao;
import com.example.backup.dao.FavoriteDao;
import com.example.backup.dao.GenreDao;
import com.example.backup.dao.MovieDao;
import com.example.backup.dao.MovieRatingDao;
import com.example.backup.dao.UserDao;
import com.example.backup.dao.WatchHistoryDao;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.Genre;
import com.example.backup.model.Movie;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller điều phối luồng dữ liệu & tương tác người dùng của phân hệ Phim
 * Tuân thủ DAO Pattern: ủy nhiệm toàn bộ truy vấn CSDL cho các lớp DAO chuyên trách
 */
public class MovieController {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Các giao diện Callback trả kết quả về UI Thread
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

    // Helper backward compatibility
    public static int resolveInternalMovieId(Connection conn, int movieId) {
        return MovieDao.resolveInternalMovieId(conn, movieId);
    }

    public static int resolveValidUserId(Connection conn, int userId) {
        return UserDao.resolveValidUserId(conn, userId);
    }

    // 1. Lấy danh sách phim theo type: top_rated, popular, latest, all
    public static void getMovies(String type, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = MovieDao.getMovies(type);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 2. Lấy tất cả phim
    public static void getAllMovies(MovieListCallback callback) {
        getMovies("all", callback);
    }

    // 3. Lấy phim theo thể loại
    public static void getMoviesByGenre(int genreId, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = MovieDao.getMoviesByGenre(genreId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 4. Lấy chi tiết một phim
    public static void getMovieDetail(int id, MovieDetailCallback callback) {
        executor.execute(() -> {
            try {
                Movie m = MovieDao.getMovieDetail(id);
                if (m != null) {
                    mainHandler.post(() -> callback.onSuccess(m));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy phim"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 4a. Tìm kiếm phim cơ bản
    public static void searchMovies(String keyword, MovieListCallback callback) {
        searchMoviesAdvanced(keyword, 0, "popularity", callback);
    }

    // 4b. Tìm kiếm phim kết hợp Lọc thể loại & Sắp xếp nâng cao
    public static void searchMoviesAdvanced(String keyword, int genreId, String sortType, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = MovieDao.searchMoviesAdvanced(keyword, genreId, sortType);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 4c. Tìm kiếm phim Đa Tiêu Chí
    public static void searchMoviesMultiFilter(String keyword, int genreId, String sortType,
                                              String vipFilter, String yearFilter, double minRating,
                                              MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = MovieDao.searchMoviesMultiFilter(keyword, genreId, sortType, vipFilter, yearFilter, minRating);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 5. Lấy danh sách thể loại
    public static void getGenres(GenreListCallback callback) {
        executor.execute(() -> {
            try {
                List<Genre> list = GenreDao.getAllGenres();
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 6. Lấy danh sách phim yêu thích
    public static void getFavorites(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = FavoriteDao.getFavorites(userId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 7. Toggle yêu thích
    public static void toggleFavorite(int userId, int movieId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                String result = FavoriteDao.toggleFavorite(userId, movieId);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 8. Lấy bình luận của 1 phim
    public static void getComments(int movieId, CommentListCallback callback) {
        executor.execute(() -> {
            try {
                List<Comment> list = CommentDao.getComments(movieId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 9. Thêm bình luận
    public static void addComment(int userId, int movieId, String content, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = CommentDao.addComment(userId, movieId, content);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã thêm bình luận"));
                } else {
                    mainHandler.post(() -> callback.onError("Thêm bình luận thất bại"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 9b. Gửi đánh giá sao của người dùng
    public static void submitMovieRating(int userId, int movieId, float rating, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                boolean success = MovieRatingDao.submitMovieRating(userId, movieId, rating);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess("Đã lưu đánh giá!"));
                } else {
                    mainHandler.post(() -> callback.onError("Không thể lưu đánh giá"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 9c. Lấy thông tin đánh giá của phim
    public static void getMovieRatingInfo(int userId, int movieId, RatingCallback callback) {
        executor.execute(() -> {
            float[] info = MovieRatingDao.getMovieRatingInfo(userId, movieId);
            float finalUserRating = info[0];
            float finalAvgRating = info[1];
            int finalTotalVotes = (int) info[2];
            mainHandler.post(() -> callback.onRatingLoaded(finalUserRating, finalAvgRating, finalTotalVotes));
        });
    }

    // 10. Lưu tiến độ xem
    public static void saveWatchProgress(int userId, int movieId, int progressSeconds) {
        saveWatchProgress(userId, movieId, progressSeconds, 0);
    }

    public static void saveWatchProgress(int userId, int movieId, int progressSeconds, int durationSeconds) {
        executor.execute(() -> {
            WatchHistoryDao.saveWatchProgress(userId, movieId, progressSeconds, durationSeconds);
        });
    }

    // 10b. Lấy tiến độ đã xem trước đó
    public static void getWatchProgress(int userId, int movieId, ProgressCallback callback) {
        executor.execute(() -> {
            int progress = WatchHistoryDao.getWatchProgress(userId, movieId);
            mainHandler.post(() -> callback.onProgressLoaded(progress));
        });
    }

    // 11. Lấy toàn bộ lịch sử xem phim
    public static void getWatchHistory(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = WatchHistoryDao.getWatchHistory(userId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 11b. Lấy danh sách phim Đang Xem Dở (Tiếp tục xem)
    public static void getContinueWatchingMovies(int userId, MovieListCallback callback) {
        executor.execute(() -> {
            try {
                List<Movie> list = WatchHistoryDao.getContinueWatchingMovies(userId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 12. Xóa 1 phim khỏi lịch sử xem
    public static void deleteWatchHistoryItem(int userId, int movieId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                WatchHistoryDao.deleteWatchHistoryItem(userId, movieId);
                mainHandler.post(() -> callback.onSuccess("Đã xóa khỏi lịch sử"));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 12b. Xóa toàn bộ lịch sử xem
    public static void clearWatchHistory(int userId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                WatchHistoryDao.clearWatchHistory(userId);
                mainHandler.post(() -> callback.onSuccess("Đã xóa toàn bộ lịch sử"));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL: " + e.getMessage()));
            }
        });
    }

    // 13. Lấy danh sách diễn viên của phim
    public static void getMovieCasts(int movieId, CastListCallback callback) {
        executor.execute(() -> {
            try {
                List<Cast> list = CastDao.getMovieCasts(movieId);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi CSDL khi truy vấn diễn viên: " + e.getMessage()));
            }
        });
    }

    // 14. Tự động tăng lượt xem của phim khi người dùng phát video
    public static void incrementMovieViewCount(int movieId) {
        executor.execute(() -> {
            MovieDao.incrementMovieViewCount(movieId);
        });
    }
}
