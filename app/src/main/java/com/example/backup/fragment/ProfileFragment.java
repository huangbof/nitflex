package com.example.backup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.MainActivity;
import com.example.backup.R;
import com.example.backup.adapter.MovieAdapter;
import com.example.backup.controller.AuthController;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;
import com.example.backup.util.VipDialogHelper;
import com.example.backup.view.AdminDashboardActivity;
import com.example.backup.view.LoginActivity;
import com.example.backup.view.MovieDetailActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

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

    private SessionManager session;
    private MovieAdapter historyAdapter;
    private final List<Movie> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        initViews(view);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_home);
                }
            });
        }

        if (btnVipAction != null) {
            btnVipAction.setOnClickListener(v -> VipDialogHelper.showUpgradeDialog(requireActivity(), null, this::loadVipStatus));
        }

        if (session.isAdmin()) {
            if (cardAdminPortal != null) cardAdminPortal.setVisibility(View.VISIBLE);
            if (btnOpenAdminPortal != null) {
                btnOpenAdminPortal.setOnClickListener(v -> startActivity(new Intent(requireContext(), AdminDashboardActivity.class)));
            }
        } else {
            if (cardAdminPortal != null) cardAdminPortal.setVisibility(View.GONE);
        }

        String uName = session.getUsername();
        tvUsername.setText(uName != null && !uName.isEmpty() ? uName : "Người dùng Nit Flex");
        tvUserEmail.setText((uName != null && !uName.isEmpty() ? uName : "user") + "@movieapp.com");
        if (uName != null && !uName.isEmpty()) {
            tvAvatar.setText(uName.substring(0, 1).toUpperCase());
        }

        if (cardOpenFavorites != null) {
            cardOpenFavorites.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_favorites);
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());
        }

        rvWatchHistory.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        historyAdapter = new MovieAdapter(requireContext(), historyList, movie -> {
            Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            intent.putExtra("movie", movie);
            startActivity(intent);
        });
        rvWatchHistory.setAdapter(historyAdapter);

        loadData();
    }

    private void initViews(View view) {
        btnBack            = view.findViewById(R.id.btnProfileBack);
        tvAvatar           = view.findViewById(R.id.tvProfileAvatar);
        tvUsername         = view.findViewById(R.id.tvProfileUsername);
        tvUserEmail        = view.findViewById(R.id.tvUserEmail);
        tvStatFavCount     = view.findViewById(R.id.tvStatFavCount);
        tvStatHistoryCount = view.findViewById(R.id.tvStatHistoryCount);
        btnClearHistory    = view.findViewById(R.id.btnClearHistory);
        btnLogout          = view.findViewById(R.id.btnLogout);
        cardOpenFavorites  = view.findViewById(R.id.cardOpenFavorites);
        rvWatchHistory     = view.findViewById(R.id.rvWatchHistory);
        layoutEmptyHistory = view.findViewById(R.id.layoutEmptyHistory);
        loading            = view.findViewById(R.id.historyLoading);

        tvMembershipTier   = view.findViewById(R.id.tvMembershipTier);
        tvVipCardTitle     = view.findViewById(R.id.tvVipCardTitle);
        tvVipCardDesc      = view.findViewById(R.id.tvVipCardDesc);
        btnVipAction       = view.findViewById(R.id.btnVipAction);
        cardAdminPortal    = view.findViewById(R.id.cardAdminPortal);
        btnOpenAdminPortal = view.findViewById(R.id.btnOpenAdminPortal);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadData();
        }
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
                if (!isAdded()) return;
                session.setVip(isVip);
                updateVipUI();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                updateVipUI();
            }
        });
    }

    private void updateVipUI() {
        if (!isAdded()) return;
        boolean isVip = session.isVip();
        if (tvMembershipTier != null) {
            if (isVip) {
                tvMembershipTier.setText("👑 Thành viên Nit Flex VIP Hoàng Gia");
                tvMembershipTier.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_gold));
            } else {
                tvMembershipTier.setText("Tài khoản Miễn phí (Free)");
                tvMembershipTier.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
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
                if (!isAdded()) return;
                int count = (movies != null) ? movies.size() : 0;
                tvStatFavCount.setText(String.valueOf(count));
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                tvStatFavCount.setText("0");
            }
        });
    }

    private void loadWatchHistory() {
        if (loading != null) loading.setVisibility(View.VISIBLE);
        MovieController.getWatchHistory(session.getUserId(), new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (!isAdded()) return;
                if (loading != null) loading.setVisibility(View.GONE);
                historyList.clear();
                if (movies != null && !movies.isEmpty()) {
                    historyList.addAll(movies);
                    historyAdapter.notifyDataSetChanged();
                    tvStatHistoryCount.setText(String.valueOf(historyList.size()));
                    if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.GONE);
                } else {
                    historyAdapter.notifyDataSetChanged();
                    tvStatHistoryCount.setText("0");
                    if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                if (loading != null) loading.setVisibility(View.GONE);
                historyList.clear();
                historyAdapter.notifyDataSetChanged();
                tvStatHistoryCount.setText("0");
                if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch sử xem")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử xem phim của mình không?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    MovieController.clearWatchHistory(session.getUserId(), new MovieController.SimpleCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (!isAdded()) return;
                            session.clearLocalWatchHistory();
                            historyList.clear();
                            historyAdapter.notifyDataSetChanged();
                            tvStatHistoryCount.setText("0");
                            if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), "Đã xóa toàn bộ lịch sử xem", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    try {
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
                        googleSignInClient.signOut();
                    } catch (Exception ignored) {}

                    session.logout();
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
