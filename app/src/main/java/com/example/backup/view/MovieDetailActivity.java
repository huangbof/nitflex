package com.example.backup.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.adapter.CastAdapter;
import com.example.backup.adapter.CommentAdapter;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;
import com.example.backup.util.VipDialogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imgBackdrop, btnPlayOverlay;
    private ImageButton btnBack;
    private TextView tvTitle, tvRating, tvYear, tvViews, tvOverview;
    private TextView tvEmptyComments, tvDetailVipBadge;
    private Button btnWatch, btnFavorite, btnSendComment;
    private EditText etComment;
    private RecyclerView rvComments;
    private RecyclerView rvCast;
    private RatingBar ratingBarUser;
    private TextView tvUserRatingStatus, tvCommunityRating;
    private boolean isUpdatingRatingUI = false;

    private Movie movie;
    private int movieId;
    private SessionManager session;
    private CommentAdapter commentAdapter;
    private final List<Comment> commentList = new ArrayList<>();
    private CastAdapter castAdapter;
    private final List<Cast> castList = new ArrayList<>();
    private boolean isFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        session = new SessionManager(this);
        movieId = getIntent().getIntExtra("movie_id", -1);
        movie = (Movie) getIntent().getSerializableExtra("movie");

        initViews();

        if (movie != null) {
            bindMovieData(movie);
            loadCasts(movie.getId());
        } else if (movieId != -1) {
            loadMovieDetail(movieId);
            loadCasts(movieId);
        }

        loadComments();
        checkFavoriteStatus();
        loadRatingInfo();
    }

    private void initViews() {
        imgBackdrop    = findViewById(R.id.imgBackdrop);
        btnPlayOverlay = findViewById(R.id.btnPlayOverlay);
        btnBack        = findViewById(R.id.btnBack);
        tvTitle        = findViewById(R.id.tvDetailTitle);
        tvRating       = findViewById(R.id.tvDetailRating);
        tvYear         = findViewById(R.id.tvDetailYear);
        tvViews        = findViewById(R.id.tvDetailViews);
        tvOverview     = findViewById(R.id.tvDetailOverview);
        btnWatch       = findViewById(R.id.btnWatchMovie);
        btnFavorite    = findViewById(R.id.btnFavorite);
        btnSendComment = findViewById(R.id.btnSendComment);
        etComment      = findViewById(R.id.etCommentInput);
        rvComments     = findViewById(R.id.rvComments);
        tvEmptyComments = findViewById(R.id.tvEmptyComments);
        tvDetailVipBadge = findViewById(R.id.tvDetailVipBadge);
        ratingBarUser  = findViewById(R.id.ratingBarUser);
        tvUserRatingStatus = findViewById(R.id.tvUserRatingStatus);
        tvCommunityRating  = findViewById(R.id.tvCommunityRating);

        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) btnBack.getLayoutParams();
            lp.topMargin = insets.top + (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            btnBack.setLayoutParams(lp);
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setAdapter(commentAdapter);

        rvCast = findViewById(R.id.rvCast);
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        castAdapter = new CastAdapter(this, castList);
        rvCast.setAdapter(castAdapter);

        View.OnClickListener playListener = v -> {
            if (movie != null) {

                if (movie.isVip() && !session.isVip()) {
                    VipDialogHelper.showUpgradeDialog(MovieDetailActivity.this, movie, () -> {
                        startPlayback();
                    });
                    return;
                }
                startPlayback();
            }
        };

        btnWatch.setOnClickListener(playListener);
        btnPlayOverlay.setOnClickListener(playListener);

        btnFavorite.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để lưu phim yêu thích!", Toast.LENGTH_SHORT).show();
                return;
            }
            final int targetMovieId = (movie != null && movie.getId() > 0) ? movie.getId() : movieId;

            isFav = !isFav;
            if (isFav) {
                session.addLocalFavorite(targetMovieId);
                if (movie != null && movie.getTmdbId() > 0) {
                    session.addLocalFavorite(movie.getTmdbId());
                }
                Toast.makeText(MovieDetailActivity.this, "Đã thêm vào danh sách Yêu Thích ❤️", Toast.LENGTH_SHORT).show();
            } else {
                session.removeLocalFavorite(targetMovieId);
                if (movie != null && movie.getTmdbId() > 0) {
                    session.removeLocalFavorite(movie.getTmdbId());
                }
                Toast.makeText(MovieDetailActivity.this, "Đã bỏ khỏi danh sách Yêu Thích", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonUI();

            MovieController.toggleFavorite(session.getUserId(), targetMovieId, new MovieController.SimpleCallback() {
                @Override
                public void onSuccess(String status) {

                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("MovieDetail", "Lỗi sync yêu thích CSDL: " + error);
                }
            });
        });

        btnSendComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!session.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendComment.setEnabled(false);
            final String userDisplayName = session.getUsername().isEmpty() ? "Khán giả" : session.getUsername();

            session.addLocalComment(movieId, userDisplayName, content);

            MovieController.addComment(session.getUserId(), movieId, content, new MovieController.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    btnSendComment.setEnabled(true);
                    etComment.setText("");
                    Toast.makeText(MovieDetailActivity.this, "Đã đăng bình luận!", Toast.LENGTH_SHORT).show();
                    loadComments();
                }

                @Override
                public void onError(String error) {
                    btnSendComment.setEnabled(true);
                    etComment.setText("");
                    Toast.makeText(MovieDetailActivity.this, "Đã lưu bình luận!", Toast.LENGTH_SHORT).show();
                    loadComments();
                }
            });
        });

        ratingBarUser.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser || isUpdatingRatingUI) return;

            if (!session.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để đánh giá phim!", Toast.LENGTH_SHORT).show();
                loadRatingInfo();
                return;
            }

            if (rating < 1.0f) {
                bar.setRating(1.0f);
                rating = 1.0f;
            }

            float finalRating = rating;
            session.saveLocalRating(movieId, finalRating);
            tvUserRatingStatus.setText("Bạn đã chấm " + (int) finalRating + "★");

            MovieController.submitMovieRating(session.getUserId(), movieId, finalRating, new MovieController.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(MovieDetailActivity.this, "Cảm ơn bạn đã đánh giá " + (int) finalRating + "★!", Toast.LENGTH_SHORT).show();
                    loadRatingInfo();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MovieDetailActivity.this, "Đã lưu đánh giá " + (int) finalRating + "★!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadRatingInfo() {
        if (movieId <= 0) return;

        float localRating = session.getLocalRating(movieId);
        if (localRating > 0) {
            isUpdatingRatingUI = true;
            ratingBarUser.setRating(localRating);
            tvUserRatingStatus.setText("Bạn đã chấm " + (int) localRating + "★");
            isUpdatingRatingUI = false;
        }

        MovieController.getMovieRatingInfo(session.getUserId(), movieId, (userRating, avgRating, totalVotes) -> {
            isUpdatingRatingUI = true;
            if (userRating > 0) {
                ratingBarUser.setRating(userRating);
                tvUserRatingStatus.setText("Bạn đã chấm " + (int) userRating + "★");
                session.saveLocalRating(movieId, userRating);
            } else if (localRating <= 0) {
                tvUserRatingStatus.setText("Chưa đánh giá");
            }

            if (totalVotes > 0) {
                tvCommunityRating.setText(String.format(java.util.Locale.getDefault(),
                        "Điểm cộng đồng Nit Flex: %.1f★ (%d lượt đánh giá)", avgRating, totalVotes));
            } else {
                tvCommunityRating.setText("Chưa có lượt đánh giá nào • Chạm để chấm điểm đầu tiên!");
            }
            isUpdatingRatingUI = false;
        });
    }

    private void updateFavoriteButtonUI() {
        if (isFav) {
            btnFavorite.setText("❤️ Đã Thích");
            btnFavorite.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_red));
        } else {
            btnFavorite.setText("♡ Yêu thích");
            btnFavorite.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card));
        }
    }

    private void checkFavoriteStatus() {
        if (!session.isLoggedIn()) return;
        final int targetMovieId = (movie != null && movie.getId() > 0) ? movie.getId() : movieId;
        if (targetMovieId <= 0) return;

        if (session.isLocalFavorite(targetMovieId) || (movie != null && session.isLocalFavorite(movie.getTmdbId()))) {
            isFav = true;
            updateFavoriteButtonUI();
        }

        MovieController.getFavorites(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (movies != null) {
                    boolean foundInDb = false;
                    for (Movie m : movies) {
                        if (m.getId() == targetMovieId
                                || (movie != null && m.getTmdbId() > 0 && m.getTmdbId() == movie.getTmdbId())
                                || (m.getTmdbId() > 0 && m.getTmdbId() == targetMovieId)) {
                            foundInDb = true;
                            break;
                        }
                    }
                    if (foundInDb) {
                        isFav = true;
                        session.addLocalFavorite(targetMovieId);
                    } else if (!session.isLocalFavorite(targetMovieId)) {
                        isFav = false;
                    }
                    updateFavoriteButtonUI();
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWatchProgress();
    }

    private void checkWatchProgress() {
        if (movie == null) return;
        int targetId = movie.getId() > 0 ? movie.getId() : movieId;
        int localProg = session.getLocalWatchProgress(targetId);
        if (localProg > 0) {
            movie.setWatchProgress(localProg);
            btnWatch.setText("▶ Tiếp Tục Xem (" + movie.getFormattedWatchProgress() + ")");
        }

        if (session.isLoggedIn() && targetId > 0) {
            MovieController.getWatchProgress(session.getUserId(), targetId, progressSec -> {
                if (progressSec > 0) {
                    movie.setWatchProgress(progressSec);
                    btnWatch.setText("▶ Tiếp Tục Xem (" + movie.getFormattedWatchProgress() + ")");
                }
            });
        }
    }

    private void startPlayback() {
        if (movie != null) {
            Intent intent = new Intent(MovieDetailActivity.this, PlayerActivity.class);
            intent.putExtra("video_url", movie.getVideoUrl());
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie_title", movie.getTitle());
            if (movie.getWatchProgress() > 0) {
                intent.putExtra("start_position", movie.getWatchProgress() * 1000);
            }
            startActivity(intent);
        }
    }

    private void bindMovieData(Movie m) {
        this.movie = m;
        this.movieId = m.getId();

        tvTitle.setText(m.getTitle());
        tvRating.setText(String.format(Locale.US, "★ %.1f TMDB", m.getVoteAverage()));
        if (m.getReleaseDate() != null && m.getReleaseDate().length() >= 4) {
            tvYear.setText(m.getReleaseDate().substring(0, 4));
        }
        tvViews.setText("👁 " + m.getViewCount() + " lượt xem");
        tvOverview.setText(m.getOverview() != null && !m.getOverview().isEmpty() ? m.getOverview() : "Đang cập nhật nội dung phim...");

        if (tvDetailVipBadge != null) {
            tvDetailVipBadge.setVisibility(m.isVip() ? View.VISIBLE : View.GONE);
        }

        checkWatchProgress();

        String imageToShow = (m.getBackdropPath() != null && !m.getBackdropPath().isEmpty())
                ? m.getBackdropPath() : m.getPosterPath();

        Glide.with(this)
                .load(imageToShow)
                .placeholder(R.drawable.gradient_banner_overlay)
                .into(imgBackdrop);
    }

    private void loadMovieDetail(int id) {
        MovieController.getMovieDetail(id, new MovieController.MovieDetailCallback() {
            @Override
            public void onSuccess(Movie m) {
                bindMovieData(m);
                loadCasts(m.getId());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MovieDetailActivity.this, "Không thể tải chi tiết: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        if (movieId <= 0) return;
        MovieController.getComments(movieId, new MovieController.CommentListCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                commentList.clear();
                if (comments != null) {
                    commentList.addAll(comments);
                }

                List<Comment> localList = session.getLocalComments(movieId);
                for (Comment localC : localList) {
                    boolean exists = false;
                    for (Comment c : commentList) {
                        if (c.getContent() != null && c.getContent().equals(localC.getContent())
                                && c.getUsername() != null && c.getUsername().equals(localC.getUsername())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        commentList.add(0, localC);
                    }
                }
                updateCommentsUI();
            }

            @Override
            public void onError(String error) {
                commentList.clear();
                List<Comment> localList = session.getLocalComments(movieId);
                commentList.addAll(localList);
                updateCommentsUI();
            }
        });
    }

    private void updateCommentsUI() {
        commentAdapter.notifyDataSetChanged();
        if (tvEmptyComments != null) {
            tvEmptyComments.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void loadCasts(int id) {
        if (id <= 0) return;
        MovieController.getMovieCasts(id, new MovieController.CastListCallback() {
            @Override
            public void onSuccess(List<Cast> casts) {
                castList.clear();
                if (casts != null && !casts.isEmpty()) {
                    castList.addAll(casts);
                }
                castAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {}
        });
    }
}
