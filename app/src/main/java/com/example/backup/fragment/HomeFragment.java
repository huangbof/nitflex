package com.example.backup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.MainActivity;
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
import com.example.backup.view.MovieDetailActivity;
import com.example.backup.view.PlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvGenres, rvMovies;
    private ProgressBar progressBar;
    private ImageView btnTopNotification, btnTopSearch, btnTopProfile;
    private TextView btnTopAiChat;
    private View headerLayout;

    private View bannerCard;
    private ImageView imgBanner;
    private TextView tvBannerTitle, tvBannerRating, tvSectionTitle;
    private Button btnBannerPlay;
    private Movie featuredMovie;
    private SessionManager session;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        NotificationHelper.createNotificationChannel(requireContext());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted && featuredMovie != null && isAdded()) {
                        NotificationHelper.showMovieNotification(requireContext(), featuredMovie);
                        Toast.makeText(requireContext(), "🔔 Đã bật thông báo phim mới!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        initViews(view);
        setupRecyclerViews();

        loadContinueWatchingMovies();
        loadGenres();
        loadMovies(null);
    }

    private void initViews(View view) {
        headerLayout          = view.findViewById(R.id.headerLayout);
        rvGenres              = view.findViewById(R.id.rvGenres);
        rvMovies              = view.findViewById(R.id.rvMovies);
        progressBar           = view.findViewById(R.id.progressBar);
        btnTopNotification    = view.findViewById(R.id.btnTopNotification);
        btnTopSearch          = view.findViewById(R.id.btnTopSearch);
        btnTopProfile         = view.findViewById(R.id.btnTopProfile);
        btnTopAiChat          = view.findViewById(R.id.btnTopAiChat);

        bannerCard            = view.findViewById(R.id.bannerCard);
        imgBanner             = view.findViewById(R.id.imgBanner);
        tvBannerTitle         = view.findViewById(R.id.tvBannerTitle);
        tvBannerRating        = view.findViewById(R.id.tvBannerRating);
        tvSectionTitle        = view.findViewById(R.id.tvSectionTitle);
        btnBannerPlay         = view.findViewById(R.id.btnBannerPlay);

        layoutContinueWatching = view.findViewById(R.id.layoutContinueWatching);
        rvContinueWatching    = view.findViewById(R.id.rvContinueWatching);
        btnHomeViewHistory    = view.findViewById(R.id.btnHomeViewHistory);

        if (btnTopNotification != null) {
            btnTopNotification.setOnClickListener(v -> {
                if (featuredMovie != null) {
                    triggerMovieNotification(featuredMovie, true);
                } else {
                    Toast.makeText(requireContext(), "Đang tải dữ liệu phim...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTopSearch != null) {
            btnTopSearch.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_search);
                }
            });
        }

        if (btnTopProfile != null) {
            btnTopProfile.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_profile);
                }
            });
        }

        if (btnTopAiChat != null) {
            btnTopAiChat.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_ai);
                }
            });
        }

        btnBannerPlay.setOnClickListener(v -> {
            if (featuredMovie != null) {
                if (featuredMovie.isVip() && !session.isVip()) {
                    VipDialogHelper.showUpgradeDialog(requireActivity(), featuredMovie, this::startBannerPlayback);
                } else {
                    startBannerPlayback();
                }
            }
        });

        bannerCard.setOnClickListener(v -> {
            if (featuredMovie != null) {
                Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
                intent.putExtra("movie_id", featuredMovie.getId());
                intent.putExtra("movie", featuredMovie);
                startActivity(intent);
            }
        });

        if (btnHomeViewHistory != null) {
            btnHomeViewHistory.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_profile);
                }
            });
        }
    }

    private void setupRecyclerViews() {

        if (rvContinueWatching != null) {
            rvContinueWatching.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            continueWatchingAdapter = new ContinueWatchingAdapter(requireContext(), continueWatchingList, new ContinueWatchingAdapter.OnContinueWatchingListener() {
                @Override
                public void onPlay(Movie movie) {
                    if (movie.isVip() && !session.isVip()) {
                        VipDialogHelper.showUpgradeDialog(requireActivity(), movie, () -> launchPlayerForMovie(movie));
                        return;
                    }
                    launchPlayerForMovie(movie);
                }

                @Override
                public void onRemove(Movie movie) {
                    session.removeWatchedMovie(movie.getId());
                    if (session.isLoggedIn()) {
                        MovieController.deleteWatchHistoryItem(session.getUserId(), movie.getId(), null);
                    }
                    continueWatchingList.remove(movie);
                    continueWatchingAdapter.notifyDataSetChanged();
                    if (continueWatchingList.isEmpty() && layoutContinueWatching != null) {
                        layoutContinueWatching.setVisibility(View.GONE);
                    }
                    Toast.makeText(requireContext(), "Đã xóa khỏi Tiếp Tục Xem", Toast.LENGTH_SHORT).show();
                }
            });
            rvContinueWatching.setAdapter(continueWatchingAdapter);
        }

        rvGenres.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        genreAdapter = new GenreAdapter(requireContext(), genreList, genre -> {
            if (genre.getId() == 0) {
                tvSectionTitle.setText("Danh Sách Phim Đặc Sắc");
                loadMovies(null);
            } else {
                tvSectionTitle.setText("Thể loại: " + genre.getName());
                loadMoviesByGenre(genre.getId());
            }
        });
        rvGenres.setAdapter(genreAdapter);

        rvMovies.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        movieAdapter = new MovieAdapter(requireContext(), movieList, movie -> {
            Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvMovies.setAdapter(movieAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContinueWatchingMovies();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadContinueWatchingMovies();
        }
    }

    private void launchPlayerForMovie(Movie movie) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
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
                    if (!isAdded()) return;
                    continueWatchingList.clear();
                    if (movies != null && !movies.isEmpty()) {
                        continueWatchingList.addAll(movies);
                        if (layoutContinueWatching != null) layoutContinueWatching.setVisibility(View.VISIBLE);
                        if (continueWatchingAdapter != null) continueWatchingAdapter.notifyDataSetChanged();
                    } else {
                        loadLocalContinueWatchingFallback();
                    }
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
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
            public void onSuccess(List<Movie> allMovies) {
                if (!isAdded()) return;
                continueWatchingList.clear();
                if (allMovies != null) {
                    for (int wId : watchedIds) {
                        int localProg = session.getLocalWatchProgress(wId);
                        int localDur = session.getLocalWatchDuration(wId);
                        if (localDur <= 0 || localProg < (localDur - 5)) {
                            for (Movie m : allMovies) {
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
                if (!continueWatchingList.isEmpty()) {
                    if (layoutContinueWatching != null) layoutContinueWatching.setVisibility(View.VISIBLE);
                    if (continueWatchingAdapter != null) continueWatchingAdapter.notifyDataSetChanged();
                } else {
                    if (layoutContinueWatching != null) layoutContinueWatching.setVisibility(View.GONE);
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
                if (!isAdded()) return;
                genreList.clear();
                genreList.add(new Genre(0, "🔥 Tất cả"));
                if (genres != null) {
                    genreList.addAll(genres);
                }
                genreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                genreList.clear();
                genreList.add(new Genre(0, "🔥 Tất cả"));
                genreAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadMovies(String query) {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getAllMovies(new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (movies != null && !movies.isEmpty()) {
                    movieList.addAll(movies);
                    movieAdapter.notifyDataSetChanged();
                    bindBanner(movies.get(0));
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi tải phim: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startBannerPlayback() {
        if (featuredMovie != null) {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("movie_id", featuredMovie.getId());
            intent.putExtra("video_url", featuredMovie.getVideoUrl());
            intent.putExtra("movie_title", featuredMovie.getTitle());
            startActivity(intent);
        }
    }

    private void bindBanner(Movie movie) {
        this.featuredMovie = movie;
        tvBannerTitle.setText(movie.getTitle());
        tvBannerRating.setText(String.format(Locale.US, "★ %.1f TMDB", movie.getVoteAverage()));

        Glide.with(this)
                .load(movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty() ? movie.getBackdropPath() : movie.getPosterPath())
                .placeholder(R.drawable.gradient_banner_overlay)
                .error(R.drawable.gradient_banner_overlay)
                .into(imgBanner);
    }

    private void triggerMovieNotification(Movie movie, boolean userInitiated) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (userInitiated) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                }
                return;
            }
        }
        NotificationHelper.showMovieNotification(requireContext(), movie);
        if (userInitiated) {
            Toast.makeText(requireContext(), "🔔 Đã gửi thông báo phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMoviesByGenre(int genreId) {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getMoviesByGenre(genreId, new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (movies != null) movieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
