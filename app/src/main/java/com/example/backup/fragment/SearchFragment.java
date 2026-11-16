package com.example.backup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.example.backup.view.MovieDetailActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private ImageButton btnBack;
    private EditText etSearch;
    private Button btnSearch;
    private ImageButton btnOpenFilter;
    private RecyclerView rvResults;
    private View layoutEmpty;
    private TextView tvEmpty;
    private ProgressBar loading;

    private View layoutActiveFilterBanner;
    private TextView tvActiveFilterDesc;
    private TextView btnClearFilterBanner;

    private TextView chipSortPopular, chipSortRating, chipSortLatest;
    private TextView chipGenreAll, chipGenreAction, chipGenreAnime, chipGenreSciFi, chipGenreCrime, chipGenreDrama;

    private String currentSort = "popularity";
    private int selectedGenreId = 0;
    private String currentVipFilter = "all";
    private String currentYearFilter = "all";
    private double currentMinRating = 0.0;

    private MovieAdapter movieAdapter;
    private final List<Movie> resultsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFilterChips();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_home);
                }
            });
        }

        rvResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        movieAdapter = new MovieAdapter(requireContext(), resultsList, movie -> {
            Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvResults.setAdapter(movieAdapter);

        btnSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        performSearch();
    }

    private void initViews(View view) {
        btnBack      = view.findViewById(R.id.btnSearchBack);
        etSearch     = view.findViewById(R.id.etSearchQuery);
        btnSearch    = view.findViewById(R.id.btnDoSearch);
        btnOpenFilter= view.findViewById(R.id.btnOpenFilter);
        rvResults    = view.findViewById(R.id.rvSearchResults);
        layoutEmpty  = view.findViewById(R.id.layoutEmptySearch);
        tvEmpty      = view.findViewById(R.id.tvEmptySearch);
        loading      = view.findViewById(R.id.searchLoading);

        layoutActiveFilterBanner = view.findViewById(R.id.layoutActiveFilterBanner);
        tvActiveFilterDesc       = view.findViewById(R.id.tvActiveFilterDesc);
        btnClearFilterBanner     = view.findViewById(R.id.btnClearFilterBanner);

        chipSortPopular = view.findViewById(R.id.chipSortPopular);
        chipSortRating  = view.findViewById(R.id.chipSortRating);
        chipSortLatest  = view.findViewById(R.id.chipSortLatest);

        chipGenreAll    = view.findViewById(R.id.chipGenreAll);
        chipGenreAction = view.findViewById(R.id.chipGenreAction);
        chipGenreAnime  = view.findViewById(R.id.chipGenreAnime);
        chipGenreSciFi  = view.findViewById(R.id.chipGenreSciFi);
        chipGenreCrime  = view.findViewById(R.id.chipGenreCrime);
        chipGenreDrama  = view.findViewById(R.id.chipGenreDrama);

        if (btnOpenFilter != null) {
            btnOpenFilter.setOnClickListener(v -> showFilterBottomSheet());
        }

        if (btnClearFilterBanner != null) {
            btnClearFilterBanner.setOnClickListener(v -> resetFilters());
        }
    }

    private void setupFilterChips() {
        chipSortPopular.setOnClickListener(v -> {
            currentSort = "popularity";
            updateSortChipsUI();
            performSearch();
        });

        chipSortRating.setOnClickListener(v -> {
            currentSort = "rating";
            updateSortChipsUI();
            performSearch();
        });

        chipSortLatest.setOnClickListener(v -> {
            currentSort = "latest";
            updateSortChipsUI();
            performSearch();
        });

        chipGenreAll.setOnClickListener(v -> selectGenre(0));
        chipGenreAction.setOnClickListener(v -> selectGenre(28));
        chipGenreAnime.setOnClickListener(v -> selectGenre(16));
        chipGenreSciFi.setOnClickListener(v -> selectGenre(878));
        chipGenreCrime.setOnClickListener(v -> selectGenre(80));
        chipGenreDrama.setOnClickListener(v -> selectGenre(18));
    }

    private void selectGenre(int genreId) {
        selectedGenreId = genreId;
        updateGenreChipsUI();
        performSearch();
    }

    private void updateSortChipsUI() {
        chipSortPopular.setBackgroundResource("popularity".equals(currentSort) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipSortRating.setBackgroundResource("rating".equals(currentSort) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipSortLatest.setBackgroundResource("latest".equals(currentSort) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
    }

    private void updateGenreChipsUI() {
        chipGenreAll.setBackgroundResource(selectedGenreId == 0 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipGenreAction.setBackgroundResource(selectedGenreId == 28 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipGenreAnime.setBackgroundResource(selectedGenreId == 16 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipGenreSciFi.setBackgroundResource(selectedGenreId == 878 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipGenreCrime.setBackgroundResource(selectedGenreId == 80 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        chipGenreDrama.setBackgroundResource(selectedGenreId == 18 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_filter_bottom_sheet, null);
        dialog.setContentView(sheetView);

        final String[] tempVip = {currentVipFilter};
        final String[] tempYear = {currentYearFilter};
        final String[] tempSort = {currentSort};
        final double[] tempMinRating = {currentMinRating};

        TextView chipVipAll = sheetView.findViewById(R.id.chipFilterVipAll);
        TextView chipVipFree = sheetView.findViewById(R.id.chipFilterVipFree);
        TextView chipVipOnly = sheetView.findViewById(R.id.chipFilterVipOnly);

        Runnable updateVipUI = () -> {
            chipVipAll.setBackgroundResource("all".equals(tempVip[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipVipFree.setBackgroundResource("free".equals(tempVip[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipVipOnly.setBackgroundResource("vip".equals(tempVip[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        };
        chipVipAll.setOnClickListener(v -> { tempVip[0] = "all"; updateVipUI.run(); });
        chipVipFree.setOnClickListener(v -> { tempVip[0] = "free"; updateVipUI.run(); });
        chipVipOnly.setOnClickListener(v -> { tempVip[0] = "vip"; updateVipUI.run(); });
        updateVipUI.run();

        TextView chipYearAll = sheetView.findViewById(R.id.chipFilterYearAll);
        TextView chipYear2020 = sheetView.findViewById(R.id.chipFilterYear2020);
        TextView chipYear2010 = sheetView.findViewById(R.id.chipFilterYear2010);
        TextView chipYearClassic = sheetView.findViewById(R.id.chipFilterYearClassic);

        Runnable updateYearUI = () -> {
            chipYearAll.setBackgroundResource("all".equals(tempYear[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipYear2020.setBackgroundResource("2020s".equals(tempYear[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipYear2010.setBackgroundResource("2010s".equals(tempYear[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipYearClassic.setBackgroundResource("classic".equals(tempYear[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        };
        chipYearAll.setOnClickListener(v -> { tempYear[0] = "all"; updateYearUI.run(); });
        chipYear2020.setOnClickListener(v -> { tempYear[0] = "2020s"; updateYearUI.run(); });
        chipYear2010.setOnClickListener(v -> { tempYear[0] = "2010s"; updateYearUI.run(); });
        chipYearClassic.setOnClickListener(v -> { tempYear[0] = "classic"; updateYearUI.run(); });
        updateYearUI.run();

        TextView chipSortPop = sheetView.findViewById(R.id.chipFilterSortPop);
        TextView chipSortRat = sheetView.findViewById(R.id.chipFilterSortRating);
        TextView chipSortViews = sheetView.findViewById(R.id.chipFilterSortViews);
        TextView chipSortLat = sheetView.findViewById(R.id.chipFilterSortLatest);

        Runnable updateSortUI = () -> {
            chipSortPop.setBackgroundResource("popularity".equals(tempSort[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipSortRat.setBackgroundResource("rating".equals(tempSort[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipSortViews.setBackgroundResource("views".equals(tempSort[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipSortLat.setBackgroundResource("latest".equals(tempSort[0]) ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        };
        chipSortPop.setOnClickListener(v -> { tempSort[0] = "popularity"; updateSortUI.run(); });
        chipSortRat.setOnClickListener(v -> { tempSort[0] = "rating"; updateSortUI.run(); });
        chipSortViews.setOnClickListener(v -> { tempSort[0] = "views"; updateSortUI.run(); });
        chipSortLat.setOnClickListener(v -> { tempSort[0] = "latest"; updateSortUI.run(); });
        updateSortUI.run();

        TextView chipRatAll = sheetView.findViewById(R.id.chipFilterRatingAll);
        TextView chipRat7 = sheetView.findViewById(R.id.chipFilterRating7);
        TextView chipRat8 = sheetView.findViewById(R.id.chipFilterRating8);

        Runnable updateMinRatingUI = () -> {
            chipRatAll.setBackgroundResource(tempMinRating[0] == 0.0 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipRat7.setBackgroundResource(tempMinRating[0] == 7.0 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
            chipRat8.setBackgroundResource(tempMinRating[0] == 8.0 ? R.drawable.bg_genre_chip_selected : R.drawable.bg_genre_chip);
        };
        chipRatAll.setOnClickListener(v -> { tempMinRating[0] = 0.0; updateMinRatingUI.run(); });
        chipRat7.setOnClickListener(v -> { tempMinRating[0] = 7.0; updateMinRatingUI.run(); });
        chipRat8.setOnClickListener(v -> { tempMinRating[0] = 8.0; updateMinRatingUI.run(); });
        updateMinRatingUI.run();

        View btnClose = sheetView.findViewById(R.id.btnFilterClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        Button btnReset = sheetView.findViewById(R.id.btnFilterReset);
        btnReset.setOnClickListener(v -> {
            tempVip[0] = "all";
            tempYear[0] = "all";
            tempSort[0] = "popularity";
            tempMinRating[0] = 0.0;
            updateVipUI.run();
            updateYearUI.run();
            updateSortUI.run();
            updateMinRatingUI.run();
        });

        Button btnApply = sheetView.findViewById(R.id.btnFilterApply);
        btnApply.setOnClickListener(v -> {
            currentVipFilter = tempVip[0];
            currentYearFilter = tempYear[0];
            currentSort = tempSort[0];
            currentMinRating = tempMinRating[0];

            updateSortChipsUI();
            updateFilterBannerUI();
            dialog.dismiss();
            performSearch();
        });

        dialog.show();
    }

    private void resetFilters() {
        currentVipFilter = "all";
        currentYearFilter = "all";
        currentSort = "popularity";
        currentMinRating = 0.0;
        selectedGenreId = 0;

        updateSortChipsUI();
        updateGenreChipsUI();
        updateFilterBannerUI();
        performSearch();
    }

    private void updateFilterBannerUI() {
        if (layoutActiveFilterBanner == null) return;

        boolean hasActive = !"all".equals(currentVipFilter)
                || !"all".equals(currentYearFilter)
                || currentMinRating > 0.0
                || "views".equals(currentSort);

        if (!hasActive) {
            layoutActiveFilterBanner.setVisibility(View.GONE);
            return;
        }

        List<String> tags = new ArrayList<>();
        if ("vip".equals(currentVipFilter)) tags.add("👑 Phim VIP");
        else if ("free".equals(currentVipFilter)) tags.add("Miễn phí");

        if ("2020s".equals(currentYearFilter)) tags.add("2020-Nay");
        else if ("2010s".equals(currentYearFilter)) tags.add("2010-2019");
        else if ("classic".equals(currentYearFilter)) tags.add("Trước 2010");

        if (currentMinRating > 0.0) tags.add("≥ " + (int) currentMinRating + "★");
        if ("views".equals(currentSort)) tags.add("Xem nhiều");

        tvActiveFilterDesc.setText("⚡ Bộ lọc: " + android.text.TextUtils.join(" • ", tags));
        layoutActiveFilterBanner.setVisibility(View.VISIBLE);
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();

        loading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        MovieController.searchMoviesMultiFilter(query, selectedGenreId, currentSort, currentVipFilter, currentYearFilter, currentMinRating, new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (!isAdded()) return;
                loading.setVisibility(View.GONE);
                resultsList.clear();
                if (movies != null && !movies.isEmpty()) {
                    resultsList.addAll(movies);
                    layoutEmpty.setVisibility(View.GONE);
                } else {
                    String msg = query.isEmpty()
                            ? "Không có phim nào phù hợp với bộ lọc hiện tại"
                            : "Không tìm thấy phim '" + query + "' theo bộ lọc đã chọn";
                    tvEmpty.setText(msg);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                loading.setVisibility(View.GONE);
                MovieController.getAllMovies(new MovieController.MovieListCallback() {
                    @Override
                    public void onSuccess(List<Movie> all) {
                        if (!isAdded()) return;
                        resultsList.clear();
                        if (all != null) resultsList.addAll(all);
                        movieAdapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(resultsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(String ignored) {
                        if (!isAdded()) return;
                        tvEmpty.setText("Không thể kết nối CSDL: " + error);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
