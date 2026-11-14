package com.example.backup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.MainActivity;
import com.example.backup.R;
import com.example.backup.adapter.MovieAdapter;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;
import com.example.backup.view.MovieDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private ImageButton btnBack;
    private TextView tvBadgeCount;
    private View layoutEmpty;
    private RecyclerView rvFavorites;
    private ProgressBar loading;

    private SessionManager session;
    private MovieAdapter favAdapter;
    private final List<Movie> favList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        initViews(view);
        setupRecyclerView();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_home);
                }
            });
        }

        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void initViews(View view) {
        btnBack      = view.findViewById(R.id.btnFavBack);
        tvBadgeCount = view.findViewById(R.id.tvFavBadgeCount);
        rvFavorites  = view.findViewById(R.id.rvFavoritesList);
        layoutEmpty  = view.findViewById(R.id.layoutEmptyFav);
        loading      = view.findViewById(R.id.favLoading);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        favAdapter = new MovieAdapter(requireContext(), favList, movie -> {
            Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvFavorites.setAdapter(favAdapter);
    }

    private void loadFavorites() {
        if (session == null || !session.isLoggedIn()) {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        loading.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);

        MovieController.getFavorites(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (!isAdded()) return;
                loading.setVisibility(View.GONE);
                favList.clear();
                if (movies != null && !movies.isEmpty()) {
                    favList.addAll(movies);
                    favAdapter.notifyDataSetChanged();
                    if (tvBadgeCount != null) tvBadgeCount.setText(favList.size() + " phim");
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                } else {
                    favAdapter.notifyDataSetChanged();
                    if (tvBadgeCount != null) tvBadgeCount.setText("0 phim");
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                loading.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Lỗi tải yêu thích: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
