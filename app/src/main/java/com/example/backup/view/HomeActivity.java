package com.example.backup.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.adapter.ContinueWatchingAdapter;
import com.example.backup.adapter.GenreAdapter;
import com.example.backup.adapter.MovieAdapter;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Genre;
import com.example.backup.model.Movie;
import com.example.backup.service.NotificationHelper;
import com.example.backup.service.SessionManager;
import com.example.backup.util.VipDialogHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvGenres, rvMovies;
    private ProgressBar progressBar;
    private ImageView btnTopNotification, btnTopSearch, btnTopProfile;
    private BottomNavigationView bottomNav;
    private View headerLayout;

    private View bannerCard;
    private ImageView imgBanner;
    private TextView tvBannerTitle, tvBannerRating, tvSectionTitle;
    private Button btnBannerPlay;
    private Movie featuredMovie;
    private SessionManager session;
    private boolean hasNotifiedStartup = false;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private View layoutContinueWatching;
    private RecyclerView rvContinueWatching;
    private TextView btnHomeViewHistory;
    private ContinueWatchingAdapter continueWatchingAdapter;
    private final List<Movie> continueWatchingList = new ArrayList<>();

    private MovieAdapter movieAdapter;
    private GenreAdapter genreAdapter;
    private final List<Movie> movieList = new ArrayList<>();
    private final List<Genre> genreList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        NotificationHelper.createNotificationChannel(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted && featuredMovie != null) {
                        NotificationHelper.showMovieNotification(HomeActivity.this, featuredMovie);
                        Toast.makeText(HomeActivity.this, "🔔 Đã bật thông báo phim mới!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        session = new SessionManager(this);
        initViews();
        handleWindowInsets();
        setupRecyclerViews();
        setupBottomNavigation();
        loadGenres();
        loadMovies(null);
    }

    private void initViews() {
        headerLayout = findViewById(R.id.headerLayout);
        rvGenres = findViewById(R.id.rvGenres);
        rvMovies = findViewById(R.id.rvMovies);
        progressBar = findViewById(R.id.progressBar);
        btnTopNotification = findViewById(R.id.btnTopNotification);
        btnTopSearch = findViewById(R.id.btnTopSearch);
        btnTopProfile = findViewById(R.id.btnTopProfile);
        bottomNav = findViewById(R.id.bottomNavigation);

        bannerCard = findViewById(R.id.bannerCard);
        imgBanner = findViewById(R.id.imgBanner);
        tvBannerTitle = findViewById(R.id.tvBannerTitle);
        tvBannerRating = findViewById(R.id.tvBannerRating);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        btnBannerPlay = findViewById(R.id.btnBannerPlay);

        if (btnTopNotification != null) {
            btnTopNotification.setOnClickListener(v -> {
                if (featuredMovie != null) {
                    triggerMovieNotification(featuredMovie, true);
                } else {
                    Toast.makeText(this, "Đang tải dữ liệu phim...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnTopSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnTopProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        View btnTopAi = findViewById(R.id.btnTopAiChat);
        if (btnTopAi != null) {
            btnTopAi.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ChatAiActivity.class)));
        }

        btnBannerPlay.setOnClickListener(v -> {
            if (featuredMovie != null) {
                if (featuredMovie.isVip() && !session.isVip()) {
                    VipDialogHelper.showUpgradeDialog(HomeActivity.this, featuredMovie, () -> {
                        startBannerPlayback();
                    });
                    return;
                }
                startBannerPlayback();
            }
        });

        bannerCard.setOnClickListener(v -> {
            if (featuredMovie != null) {
                Intent intent = new Intent(HomeActivity.this, MovieDetailActivity.class);
                intent.putExtra("movie_id", featuredMovie.getId());
                intent.putExtra("movie", featuredMovie);
                startActivity(intent);
            }
        });

        layoutContinueWatching = findViewById(R.id.layoutContinueWatching);
        rvContinueWatching     = findViewById(R.id.rvContinueWatching);
        btnHomeViewHistory     = findViewById(R.id.btnHomeViewHistory);
        if (btnHomeViewHistory != null) {
            btnHomeViewHistory.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        }
    }

    private void handleWindowInsets() {
        View root = findViewById(R.id.homeRoot);
        int padLeft = headerLayout.getPaddingLeft();
        int padRight = headerLayout.getPaddingRight();
        int padBottom = headerLayout.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraTop = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            headerLayout.setPadding(padLeft, insets.top + extraTop, padRight, padBottom);
            bottomNav.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {

        if (rvContinueWatching != null) {
            rvContinueWatching.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            continueWatchingAdapter = new ContinueWatchingAdapter(this, continueWatchingList, new ContinueWatchingAdapter.OnContinueWatchingListener() {
                @Override
                public void onPlay(Movie movie) {
                    if (movie.isVip() && !session.isVip()) {
                        VipDialogHelper.showUpgradeDialog(HomeActivity.this, movie, () -> {
                            launchPlayerForMovie(movie);
                        });
                        return;
                    }
                    launchPlayerForMovie(movie);
                }

                @Override
                public void onRemove(Movie movie) {
                    session.removeWatchedMovie(movie.getId());
                    if (session.isLoggedIn()) {
                        MovieController.deleteWatchHistoryItem(session.getUserId(), movie.getId(), new MovieController.SimpleCallback() {
                            @Override public void onSuccess(String message) {}
                            @Override public void onError(String error) {}
                        });
                    }
                    continueWatchingList.remove(movie);
                    continueWatchingAdapter.notifyDataSetChanged();
                    if (continueWatchingList.isEmpty()) {
                        layoutContinueWatching.setVisibility(View.GONE);
                    }
                    Toast.makeText(HomeActivity.this, "Đã xóa khỏi Tiếp Tục Xem", Toast.LENGTH_SHORT).show();
                }
            });
            rvContinueWatching.setAdapter(continueWatchingAdapter);
        }

        rvGenres.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genreAdapter = new GenreAdapter(this, genreList, genre -> {
            if (genre.getId() == 0) {
                tvSectionTitle.setText("Danh Sách Phim Đặc Sắc");
                loadMovies(null);
            } else {
                tvSectionTitle.setText("Thể loại: " + genre.getName());
                loadMoviesByGenre(genre.getId());
            }
        });
        rvGenres.setAdapter(genreAdapter);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(this, movieList, movie -> {
            Intent intent = new Intent(HomeActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvMovies.setAdapter(movieAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContinueWatchingMovies();
    }

    private void launchPlayerForMovie(Movie movie) {
        Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
        intent.putExtra("movie_id", movie.getId());
        intent.putExtra("video_url", movie.getVideoUrl());
        intent.putExtra("movie_title", movie.getTitle());
        intent.putExtra("start_position", movie.getWatchProgress() * 1000);
        startActivity(intent);
    }

    private void loadContinueWatchingMovies() {
        if (session.isLoggedIn()) {
            MovieController.getContinueWatchingMovies(session.getUserId(), new MovieController.MovieListCallback() {
                @Override
                public void onSuccess(List<Movie> movies) {
                    continueWatchingList.clear();
                    if (movies != null && !movies.isEmpty()) {
                        continueWatchingList.addAll(movies);
                        layoutContinueWatching.setVisibility(View.VISIBLE);
                        if (continueWatchingAdapter != null) continueWatchingAdapter.notifyDataSetChanged();
                    } else {
                        loadLocalContinueWatchingFallback();
                    }
                }

                @Override
                public void onError(String error) {
                    loadLocalContinueWatchingFallback();
                }
            });
        } else {
            loadLocalContinueWatchingFallback();
        }
    }

    private void loadLocalContinueWatchingFallback() {
        List<Integer> watchedIds = session.getWatchedMovieIds();
        if (watchedIds == null || watchedIds.isEmpty()) {
            if (layoutContinueWatching != null) layoutContinueWatching.setVisibility(View.GONE);
            return;
        }

        MovieController.getAllMovies(new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> all) {
                continueWatchingList.clear();
                if (all != null) {
                    for (int wId : watchedIds) {
                        int localProg = session.getLocalWatchProgress(wId);
                        int localDur = session.getLocalWatchDuration(wId);
                        if (localDur <= 0 || localProg < (localDur - 5)) {
                            for (Movie m : all) {
                                if (m.getId() == wId || m.getTmdbId() == wId) {
                                    m.setWatchProgress(localProg);
                                    m.setWatchDuration(localDur);
                                    if (!continueWatchingList.contains(m)) {
                                        continueWatchingList.add(m);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (layoutContinueWatching != null) {
                    layoutContinueWatching.setVisibility(continueWatchingList.isEmpty() ? View.GONE : View.VISIBLE);
                }
                if (continueWatchingAdapter != null) {
                    continueWatchingAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                if (layoutContinueWatching != null) layoutContinueWatching.setVisibility(View.GONE);
            }
        });
    }

    private void loadGenres() {
        MovieController.getGenres(new MovieController.GenreListCallback() {
            @Override
            public void onSuccess(List<Genre> genres) {
                genreList.clear();
                genreList.add(new Genre(0, "🔥 Tất cả"));
                if (genres != null) {
                    genreList.addAll(genres);
                }
                genreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                genreList.clear();
                genreList.add(new Genre(0, "🔥 Tất cả"));
                genreAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadMovies(String type) {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getMovies(type, new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (movies != null && !movies.isEmpty()) {
                    movieList.addAll(movies);

                    featuredMovie = movies.get(0);
                    bindBanner(featuredMovie);
                }
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Không thể kết nối CSDL XAMPP: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startBannerPlayback() {
        if (featuredMovie != null) {
            Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
            intent.putExtra("video_url", featuredMovie.getVideoUrl());
            intent.putExtra("movie_id", featuredMovie.getId());
            intent.putExtra("movie_title", featuredMovie.getTitle());
            startActivity(intent);
        }
    }

    private void bindBanner(Movie m) {
        if (m == null) return;
        tvBannerTitle.setText(m.getTitle());
        if (m.isVip()) {
            tvBannerRating.setText(String.format(Locale.US, "★ %.1f TMDB • 👑 VIP", m.getVoteAverage()));
        } else {
            tvBannerRating.setText(String.format(Locale.US, "★ %.1f TMDB", m.getVoteAverage()));
        }

        String imgUrl = (m.getBackdropPath() != null && !m.getBackdropPath().isEmpty())
                ? m.getBackdropPath() : m.getPosterPath();

        Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.gradient_banner_overlay)
                .into(imgBanner);

        if (!hasNotifiedStartup) {
            hasNotifiedStartup = true;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showMovieNotification(this, m);
            }
        }
    }

    private void triggerMovieNotification(Movie m, boolean showToast) {
        if (m == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        NotificationHelper.showMovieNotification(this, m);
        if (showToast) {
            Toast.makeText(this, "🔔 Đã gửi thông báo: " + m.getTitle() + "!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMoviesByGenre(int genreId) {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getMoviesByGenre(genreId, new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (movies != null) movieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
