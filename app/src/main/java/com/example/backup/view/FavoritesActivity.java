package com.example.backup.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.adapter.MovieAdapter;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvBadgeCount;
    private View layoutEmpty;
    private RecyclerView rvFavorites;
    private ProgressBar loading;
    private View favHeader;

    private SessionManager session;
    private MovieAdapter favAdapter;
    private final List<Movie> favList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem phim yêu thích!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        favHeader    = findViewById(R.id.favHeader);
        btnBack      = findViewById(R.id.btnFavBack);
        tvBadgeCount = findViewById(R.id.tvFavBadgeCount);
        rvFavorites  = findViewById(R.id.rvFavoritesList);
        layoutEmpty  = findViewById(R.id.layoutEmptyFav);
        loading      = findViewById(R.id.favLoading);

        handleWindowInsets();

        btnBack.setOnClickListener(v -> finish());

        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        favAdapter = new MovieAdapter(this, favList, movie -> {
            Intent intent = new Intent(FavoritesActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvFavorites.setAdapter(favAdapter);

        loadFavorites();
    }

    private void handleWindowInsets() {
        View root = findViewById(R.id.favRoot);
        int padLeft = favHeader.getPaddingLeft();
        int padRight = favHeader.getPaddingRight();
        int padBottom = favHeader.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraTop = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            favHeader.setPadding(padLeft, insets.top + extraTop, padRight, padBottom);
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        loading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        MovieController.getFavorites(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                loading.setVisibility(View.GONE);
                favList.clear();
                List<Integer> localFavIds = session.getLocalFavoriteIds();

                if (movies != null && !movies.isEmpty()) {
                    favList.addAll(movies);
                }

                if (!localFavIds.isEmpty()) {
                    MovieController.getAllMovies(new MovieController.MovieListCallback() {
                        @Override
                        public void onSuccess(List<Movie> allMovies) {
                            if (allMovies != null) {
                                boolean addedNew = false;
                                for (Movie m : allMovies) {
                                    if (localFavIds.contains(m.getId()) || localFavIds.contains(m.getTmdbId())) {
                                        boolean alreadyIn = false;
                                        for (Movie existing : favList) {
                                            if (existing.getId() == m.getId() || (m.getTmdbId() > 0 && existing.getTmdbId() == m.getTmdbId())) {
                                                alreadyIn = true;
                                                break;
                                            }
                                        }
                                        if (!alreadyIn) {
                                            favList.add(m);
                                            addedNew = true;
                                        }
                                    }
                                }
                                if (addedNew) {
                                    favAdapter.notifyDataSetChanged();
                                }
                            }
                            updateEmptyState();
                        }

                        @Override
                        public void onError(String error) {
                            updateEmptyState();
                        }
                    });
                } else {
                    updateEmptyState();
                }
            }

            @Override
            public void onError(String error) {

                List<Integer> localFavIds = session.getLocalFavoriteIds();
                if (!localFavIds.isEmpty()) {
                    MovieController.getAllMovies(new MovieController.MovieListCallback() {
                        @Override
                        public void onSuccess(List<Movie> allMovies) {
                            loading.setVisibility(View.GONE);
                            favList.clear();
                            if (allMovies != null) {
                                for (Movie m : allMovies) {
                                    if (localFavIds.contains(m.getId()) || localFavIds.contains(m.getTmdbId())) {
                                        favList.add(m);
                                    }
                                }
                            }
                            updateEmptyState();
                        }

                        @Override
                        public void onError(String err) {
                            loading.setVisibility(View.GONE);
                            updateEmptyState();
                        }
                    });
                } else {
                    loading.setVisibility(View.GONE);
                    updateEmptyState();
                }
            }
        });
    }

    private void updateEmptyState() {
        if (!favList.isEmpty()) {
            layoutEmpty.setVisibility(View.GONE);
            tvBadgeCount.setText(favList.size() + " phim");
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvBadgeCount.setText("0 phim");
        }
        favAdapter.notifyDataSetChanged();
    }
}
