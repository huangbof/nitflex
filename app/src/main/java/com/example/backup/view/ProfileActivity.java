package com.example.backup.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.adapter.MovieAdapter;
import com.example.backup.controller.AuthController;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;
import com.example.backup.util.VipDialogHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAvatar, tvUsername, tvUserEmail, tvStatFavCount, tvStatHistoryCount;
    private TextView tvMembershipTier, tvVipCardTitle, tvVipCardDesc;
    private Button btnVipAction;
    private View cardAdminPortal;
    private Button btnOpenAdminPortal;
    private TextView btnClearHistory;
    private View layoutEmptyHistory;
    private Button btnLogout;
    private View cardOpenFavorites;
    private RecyclerView rvWatchHistory;
    private ProgressBar loading;
    private View profileHeader;

    private SessionManager session;
    private MovieAdapter historyAdapter;
    private final List<Movie> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        profileHeader       = findViewById(R.id.profileHeader);
        btnBack             = findViewById(R.id.btnProfileBack);
        tvAvatar            = findViewById(R.id.tvProfileAvatar);
        tvUsername          = findViewById(R.id.tvProfileUsername);
        tvUserEmail         = findViewById(R.id.tvUserEmail);
        tvStatFavCount      = findViewById(R.id.tvStatFavCount);
        tvStatHistoryCount  = findViewById(R.id.tvStatHistoryCount);
        btnClearHistory     = findViewById(R.id.btnClearHistory);
        btnLogout           = findViewById(R.id.btnLogout);
        cardOpenFavorites   = findViewById(R.id.cardOpenFavorites);
        rvWatchHistory      = findViewById(R.id.rvWatchHistory);
        layoutEmptyHistory  = findViewById(R.id.layoutEmptyHistory);
        loading             = findViewById(R.id.historyLoading);

        tvMembershipTier    = findViewById(R.id.tvMembershipTier);
        tvVipCardTitle      = findViewById(R.id.tvVipCardTitle);
        tvVipCardDesc       = findViewById(R.id.tvVipCardDesc);
        btnVipAction        = findViewById(R.id.btnVipAction);
        cardAdminPortal     = findViewById(R.id.cardAdminPortal);
        btnOpenAdminPortal  = findViewById(R.id.btnOpenAdminPortal);

        if (btnVipAction != null) {
            btnVipAction.setOnClickListener(v -> VipDialogHelper.showUpgradeDialog(this, null, this::loadVipStatus));
        }

        if (session.isAdmin()) {
            if (cardAdminPortal != null) cardAdminPortal.setVisibility(View.VISIBLE);
            if (btnOpenAdminPortal != null) {
                btnOpenAdminPortal.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class)));
            }
        } else {
            if (cardAdminPortal != null) cardAdminPortal.setVisibility(View.GONE);
        }

        handleWindowInsets();

        String uName = session.getUsername();
        tvUsername.setText(uName);
        tvUserEmail.setText(uName + "@movieapp.com");
        if (!uName.isEmpty()) {
            tvAvatar.setText(uName.substring(0, 1).toUpperCase());
        }

        btnBack.setOnClickListener(v -> finish());

        cardOpenFavorites.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class)));

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());

        rvWatchHistory.setLayoutManager(new GridLayoutManager(this, 2));
        historyAdapter = new MovieAdapter(this, historyList, movie -> {
            Intent intent = new Intent(ProfileActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvWatchHistory.setAdapter(historyAdapter);

        loadData();
    }

    private void handleWindowInsets() {
        View root = findViewById(R.id.profileRoot);
        int padLeft = profileHeader.getPaddingLeft();
        int padRight = profileHeader.getPaddingRight();
        int padBottom = profileHeader.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraTop = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            profileHeader.setPadding(padLeft, insets.top + extraTop, padRight, padBottom);
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        loadFavoriteCount();
        loadWatchHistory();
        loadVipStatus();
    }

    private void loadVipStatus() {
        updateVipUI();
        AuthController.checkVipStatus(session.getUserId(), new AuthController.VipCallback() {
            @Override
            public void onSuccess(boolean isVip) {
                session.setVip(isVip);
                updateVipUI();
            }

            @Override
            public void onError(String message) {
                updateVipUI();
            }
        });
    }

    private void updateVipUI() {
        boolean isVip = session.isVip();
        if (tvMembershipTier != null) {
            if (isVip) {
                tvMembershipTier.setText("👑 Thành viên Nit Flex VIP Hoàng Gia");
                tvMembershipTier.setTextColor(ContextCompat.getColor(this, R.color.accent_gold));
            } else {
                tvMembershipTier.setText("Tài khoản Miễn phí (Free)");
                tvMembershipTier.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            }
        }
        if (tvVipCardTitle != null) {
            tvVipCardTitle.setText(isVip ? "Bạn Đang Là Thành Viên VIP" : "Kích Hoạt Nit Flex VIP");
        }
        if (tvVipCardDesc != null) {
            tvVipCardDesc.setText(isVip ? "Gói VIP đang có hiệu lực • Mở khóa toàn bộ phim điểm cao > 8.0 TMDB"
                    : "Mở khóa kho phim bom tấn điểm cao > 8.0 TMDB & 4K không quảng cáo");
        }
        if (btnVipAction != null) {
            btnVipAction.setText(isVip ? "Gia hạn" : "Nâng cấp");
        }
    }

    private void loadFavoriteCount() {
        MovieController.getFavorites(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                int count = (movies != null) ? movies.size() : 0;
                tvStatFavCount.setText(String.valueOf(count));
            }

            @Override
            public void onError(String error) {
                tvStatFavCount.setText("0");
            }
        });
    }

    private void loadWatchHistory() {
        loading.setVisibility(View.VISIBLE);
        layoutEmptyHistory.setVisibility(View.GONE);

        MovieController.getWatchHistory(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                loading.setVisibility(View.GONE);
                if (movies != null && !movies.isEmpty()) {
                    historyList.clear();
                    historyList.addAll(movies);
                    layoutEmptyHistory.setVisibility(View.GONE);
                    tvStatHistoryCount.setText(String.valueOf(movies.size()));
                    historyAdapter.notifyDataSetChanged();
                } else {

                    loadLocalHistoryFallback();
                }
            }

            @Override
            public void onError(String error) {

                loadLocalHistoryFallback();
            }
        });
    }

    private void loadLocalHistoryFallback() {
        List<Integer> watchedIds = session.getWatchedMovieIds();
        if (watchedIds == null || watchedIds.isEmpty()) {
            loading.setVisibility(View.GONE);
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
            layoutEmptyHistory.setVisibility(View.VISIBLE);
            tvStatHistoryCount.setText("0");
            return;
        }

        MovieController.getAllMovies(new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> allMovies) {
                loading.setVisibility(View.GONE);
                historyList.clear();
                if (allMovies != null) {

                    for (int id : watchedIds) {
                        for (Movie m : allMovies) {
                            if (m.getId() == id || m.getTmdbId() == id) {
                                m.setWatchProgress(session.getLocalWatchProgress(id));
                                m.setWatchDuration(session.getLocalWatchDuration(id));
                                if (!historyList.contains(m)) {
                                    historyList.add(m);
                                }
                                break;
                            }
                        }
                    }
                }
                if (!historyList.isEmpty()) {
                    layoutEmptyHistory.setVisibility(View.GONE);
                    tvStatHistoryCount.setText(String.valueOf(historyList.size()));
                } else {
                    layoutEmptyHistory.setVisibility(View.VISIBLE);
                    tvStatHistoryCount.setText("0");
                }
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                loading.setVisibility(View.GONE);
                layoutEmptyHistory.setVisibility(View.VISIBLE);
                tvStatHistoryCount.setText("0");
            }
        });
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lịch sử xem")
                .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử các phim đã xem?")
                .setPositiveButton("Xóa", (dialog, which) -> {

                    session.clearLocalWatchHistory();

                    MovieController.clearWatchHistory(session.getUserId(), new MovieController.SimpleCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(ProfileActivity.this, "Đã xóa lịch sử xem phim!", Toast.LENGTH_SHORT).show();
                            loadWatchHistory();
                        }

                        @Override
                        public void onError(String error) {

                            Toast.makeText(ProfileActivity.this, "Đã xóa lịch sử xem phim!", Toast.LENGTH_SHORT).show();
                            loadWatchHistory();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi Nit Flex?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {

                    try {
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build();
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                        googleSignInClient.signOut();
                    } catch (Exception ignored) {}

                    session.logout();
                    Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
