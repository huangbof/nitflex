package com.example.backup.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.adapter.AdminCastAdapter;
import com.example.backup.adapter.AdminCommentAdapter;
import com.example.backup.adapter.AdminMovieAdapter;
import com.example.backup.adapter.AdminUserAdapter;
import com.example.backup.adapter.AdminVipPackageAdapter;
import com.example.backup.controller.AdminController;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Cast;
import com.example.backup.model.Comment;
import com.example.backup.model.DashboardStats;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;
import com.example.backup.util.AdminDialogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private View adminHeader;
    private ProgressBar progressBar;

    private Button tabBtnStats, tabBtnMovies, tabBtnUsers, tabBtnCasts, tabBtnVip, tabBtnComments;
    private View viewTabStats, viewTabMovies, viewTabUsers, viewTabCasts, viewTabVip, viewTabComments;

    private EditText etSearchAdminMovies, etSearchAdminUsers, etSearchAdminComments;

    private TextView tvCommentsCount, tvEmptyComments;
    private Button btnRefreshComments;
    private RecyclerView rvComments;
    private AdminCommentAdapter commentAdapter;
    private final List<Comment> commentList = new ArrayList<>();

    private TextView tvStatRevenue, tvStatRevenueSub;
    private TextView tvStatUsersTotal, tvStatVipPercent;
    private TextView tvStatMoviesTotal, tvStatVipMoviesSub;
    private TextView tvStatViewsTotal, tvStatInteractions;
    private TextView tvStatConversionRate, tvStatConversionDetail;
    private ProgressBar pbVipConversion;
    private LinearLayout layoutTopMoviesContainer;
    private TextView tvEmptyTopMovies;
    private Button btnRefreshStats;

    private TextView tvMoviesCount;
    private Button btnAddMovie;
    private RecyclerView rvMovies;
    private AdminMovieAdapter movieAdapter;
    private final List<Movie> movieList = new ArrayList<>();

    private TextView tvUsersCount, tvVipUsersCount;
    private RecyclerView rvUsers;
    private AdminUserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();

    private Spinner spinnerCastMovie;
    private TextView tvCastsCount;
    private Button btnAddCast;
    private RecyclerView rvCasts;
    private AdminCastAdapter castAdapter;
    private final List<Cast> castList = new ArrayList<>();
    private final List<Movie> spinnerMovieList = new ArrayList<>();
    private int selectedMovieId = -1;

    private TextView tvVipPkgCount;
    private Button btnAddVipPkg;
    private RecyclerView rvVipPackages;
    private AdminVipPackageAdapter vipPkgAdapter;
    private final List<VipPackage> vipPkgList = new ArrayList<>();

    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<String> pickPosterLauncher;
    private EditText currentVideoInputTarget;
    private TextView currentVideoStatusTarget;
    private EditText currentPosterInputTarget;
    private ImageView currentPosterPreviewTarget;

    private interface FileCopyCallback {
        void onDone(File file);
    }

    private void copyPickedFileToInternalStorage(Uri uri, String subDirName, String prefix, String extension, FileCopyCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File dir = new File(getFilesDir(), subDirName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, prefix + System.currentTimeMillis() + extension);
                try (InputStream in = getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(dest)) {
                    if (in == null) {
                        runOnUiThread(() -> callback.onDone(null));
                        return;
                    }
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                }
                runOnUiThread(() -> callback.onDone(dest));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> callback.onDone(null));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentVideoInputTarget != null) {
                        ProgressDialog pd = new ProgressDialog(AdminDashboardActivity.this);
                        pd.setTitle("Nạp Video MP4");
                        pd.setMessage("Đang sao chép video vào bộ nhớ ứng dụng...\nVui lòng đợi trong giây lát.");
                        pd.setCancelable(false);
                        pd.show();

                        copyPickedFileToInternalStorage(uri, "movies", "movie_", ".mp4", savedFile -> {
                            pd.dismiss();
                            if (savedFile != null && savedFile.exists()) {
                                currentVideoInputTarget.setText(savedFile.getAbsolutePath());
                                long sizeMb = savedFile.length() / (1024 * 1024);
                                String sizeStr = sizeMb > 0 ? (sizeMb + " MB") : ((savedFile.length() / 1024) + " KB");
                                if (currentVideoStatusTarget != null) {
                                    currentVideoStatusTarget.setVisibility(View.VISIBLE);
                                    currentVideoStatusTarget.setText("✅ Đã nạp file video: " + savedFile.getName() + " (" + sizeStr + ")");
                                }
                                Toast.makeText(AdminDashboardActivity.this, "Đã nạp video MP4 vào bộ nhớ ứng dụng thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "Không thể sao chép video từ thiết bị!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        pickPosterLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentPosterInputTarget != null) {
                        copyPickedFileToInternalStorage(uri, "posters", "poster_", ".jpg", savedFile -> {
                            if (savedFile != null && savedFile.exists()) {
                                currentPosterInputTarget.setText(savedFile.getAbsolutePath());
                                if (currentPosterPreviewTarget != null) {
                                    currentPosterPreviewTarget.setVisibility(View.VISIBLE);
                                    Glide.with(AdminDashboardActivity.this)
                                            .load(savedFile)
                                            .into(currentPosterPreviewTarget);
                                }
                                Toast.makeText(AdminDashboardActivity.this, "Đã chọn ảnh poster từ thiết bị thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "Không thể sao chép ảnh từ thiết bị!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        initViews();
        handleWindowInsets();
        setupTabs();

        selectTab(0);
    }

    private void initViews() {
        btnBack       = findViewById(R.id.btnAdminBack);
        adminHeader   = findViewById(R.id.adminHeader);
        progressBar   = findViewById(R.id.adminProgressBar);

        tabBtnStats    = findViewById(R.id.tabBtnStats);
        tabBtnMovies   = findViewById(R.id.tabBtnMovies);
        tabBtnUsers    = findViewById(R.id.tabBtnUsers);
        tabBtnCasts    = findViewById(R.id.tabBtnCasts);
        tabBtnVip      = findViewById(R.id.tabBtnVip);
        tabBtnComments = findViewById(R.id.tabBtnComments);

        viewTabStats    = findViewById(R.id.viewTabStats);
        viewTabMovies   = findViewById(R.id.viewTabMovies);
        viewTabUsers    = findViewById(R.id.viewTabUsers);
        viewTabCasts    = findViewById(R.id.viewTabCasts);
        viewTabVip      = findViewById(R.id.viewTabVip);
        viewTabComments = findViewById(R.id.viewTabComments);

        btnBack.setOnClickListener(v -> finish());

        tvStatRevenue          = findViewById(R.id.tvStatRevenue);
        tvStatRevenueSub       = findViewById(R.id.tvStatRevenueSub);
        tvStatUsersTotal       = findViewById(R.id.tvStatUsersTotal);
        tvStatVipPercent       = findViewById(R.id.tvStatVipPercent);
        tvStatMoviesTotal      = findViewById(R.id.tvStatMoviesTotal);
        tvStatVipMoviesSub     = findViewById(R.id.tvStatVipMoviesSub);
        tvStatViewsTotal       = findViewById(R.id.tvStatViewsTotal);
        tvStatInteractions     = findViewById(R.id.tvStatInteractions);
        tvStatConversionRate   = findViewById(R.id.tvStatConversionRate);
        tvStatConversionDetail = findViewById(R.id.tvStatConversionDetail);
        pbVipConversion        = findViewById(R.id.pbVipConversion);
        layoutTopMoviesContainer = findViewById(R.id.layoutTopMoviesContainer);
        tvEmptyTopMovies       = findViewById(R.id.tvEmptyTopMovies);
        btnRefreshStats        = findViewById(R.id.btnRefreshStats);
        if (btnRefreshStats != null) {
            btnRefreshStats.setOnClickListener(v -> loadDashboardStats());
        }

        tvMoviesCount = findViewById(R.id.tvAdminMoviesCount);
        btnAddMovie   = findViewById(R.id.btnAdminAddMovie);
        rvMovies      = findViewById(R.id.rvAdminMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter  = new AdminMovieAdapter(this, movieList, new AdminMovieAdapter.OnMovieActionListener() {
            @Override
            public void onEdit(Movie movie) {
                showMovieDialog(movie);
            }

            @Override
            public void onDelete(Movie movie) {
                showDeleteConfirmDialog("Xóa Phim", "Bạn có chắc chắn muốn xóa phim \"" + movie.getTitle() + "\" khỏi CSDL?", () -> {
                    AdminController.deleteMovie(movie.getId(), new AdminController.AdminCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadMovies();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
        rvMovies.setAdapter(movieAdapter);
        btnAddMovie.setOnClickListener(v -> showMovieDialog(null));

        etSearchAdminMovies = findViewById(R.id.etSearchAdminMovies);
        if (etSearchAdminMovies != null) {
            etSearchAdminMovies.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (movieAdapter != null) movieAdapter.filter(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        tvUsersCount    = findViewById(R.id.tvAdminUsersCount);
        tvVipUsersCount = findViewById(R.id.tvAdminVipUsersCount);
        rvUsers         = findViewById(R.id.rvAdminUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter     = new AdminUserAdapter(this, userList, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onToggleVip(User user) {
                boolean newVip = !user.isVip();
                AdminController.toggleUserVip(user.getId(), newVip, new AdminController.AdminCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResetPassword(User user) {
                showResetPasswordDialog(user);
            }

            @Override
            public void onDelete(User user) {
                if ("admin".equalsIgnoreCase(user.getUsername()) || user.getId() == 1) {
                    Toast.makeText(AdminDashboardActivity.this, "Không thể xóa tài khoản Quản trị viên gốc (admin)!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showDeleteConfirmDialog("Xóa Tài Khoản", "Xóa người dùng \"" + user.getUsername() + "\"?", () -> {
                    AdminController.deleteUser(user.getId(), new AdminController.AdminCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
        rvUsers.setAdapter(userAdapter);

        etSearchAdminUsers = findViewById(R.id.etSearchAdminUsers);
        if (etSearchAdminUsers != null) {
            etSearchAdminUsers.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (userAdapter != null) userAdapter.filter(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        spinnerCastMovie = findViewById(R.id.spinnerAdminCastMovie);
        tvCastsCount     = findViewById(R.id.tvAdminCastsCount);
        btnAddCast       = findViewById(R.id.btnAdminAddCast);
        rvCasts          = findViewById(R.id.rvAdminCasts);
        rvCasts.setLayoutManager(new LinearLayoutManager(this));
        castAdapter      = new AdminCastAdapter(this, castList, cast -> {
            showDeleteConfirmDialog("Xóa Diễn Viên", "Xóa diễn viên \"" + cast.getActorName() + "\" khỏi phim?", () -> {
                AdminController.deleteCast(cast.getId(), new AdminController.AdminCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadCastsForMovie(selectedMovieId);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
        rvCasts.setAdapter(castAdapter);
        btnAddCast.setOnClickListener(v -> {
            if (selectedMovieId <= 0) {
                Toast.makeText(this, "Vui lòng chọn phim trước khi thêm diễn viên!", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddCastDialog(selectedMovieId);
        });

        tvVipPkgCount = findViewById(R.id.tvAdminVipPkgCount);
        btnAddVipPkg  = findViewById(R.id.btnAdminAddVipPkg);
        rvVipPackages = findViewById(R.id.rvAdminVipPackages);
        rvVipPackages.setLayoutManager(new LinearLayoutManager(this));
        vipPkgAdapter = new AdminVipPackageAdapter(this, vipPkgList, new AdminVipPackageAdapter.OnVipPackageActionListener() {
            @Override
            public void onToggleActive(VipPackage pkg) {
                pkg.setActive(!pkg.isActive());
                AdminController.updateVipPackage(pkg, new AdminController.AdminCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadVipPackages();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onEdit(VipPackage pkg) {
                showVipPackageDialog(pkg);
            }

            @Override
            public void onDelete(VipPackage pkg) {
                showDeleteConfirmDialog("Xóa Gói VIP", "Xóa gói VIP \"" + pkg.getName() + "\"?", () -> {
                    AdminController.deleteVipPackage(pkg.getId(), new AdminController.AdminCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadVipPackages();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
        rvVipPackages.setAdapter(vipPkgAdapter);
        btnAddVipPkg.setOnClickListener(v -> showVipPackageDialog(null));

        tvCommentsCount       = findViewById(R.id.tvAdminCommentsCount);
        tvEmptyComments       = findViewById(R.id.tvEmptyAdminComments);
        btnRefreshComments    = findViewById(R.id.btnRefreshComments);
        etSearchAdminComments = findViewById(R.id.etSearchAdminComments);
        rvComments            = findViewById(R.id.rvAdminComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter        = new AdminCommentAdapter(this, commentList, comment -> {
            showDeleteConfirmDialog("Xóa Bình Luận",
                    "Bạn có chắc muốn xóa bình luận của \"" + comment.getUsername() + "\" trên phim \"" + comment.getMovieTitle() + "\"?",
                    () -> {
                        AdminController.deleteComment(comment.getId(), new AdminController.AdminCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                                loadComments();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(AdminDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
        rvComments.setAdapter(commentAdapter);

        if (btnRefreshComments != null) {
            btnRefreshComments.setOnClickListener(v -> loadComments());
        }

        if (etSearchAdminComments != null) {
            etSearchAdminComments.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (commentAdapter != null) commentAdapter.filter(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void handleWindowInsets() {
        View root = findViewById(R.id.adminRoot);
        int padLeft = adminHeader.getPaddingLeft();
        int padRight = adminHeader.getPaddingRight();
        int padBottom = adminHeader.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraTop = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

            adminHeader.setPadding(padLeft, insets.top + extraTop, padRight, padBottom);
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }

    private void setupTabs() {
        tabBtnStats.setOnClickListener(v -> selectTab(0));
        tabBtnMovies.setOnClickListener(v -> selectTab(1));
        tabBtnUsers.setOnClickListener(v -> selectTab(2));
        tabBtnCasts.setOnClickListener(v -> selectTab(3));
        tabBtnVip.setOnClickListener(v -> selectTab(4));
        tabBtnComments.setOnClickListener(v -> selectTab(5));
    }

    private void selectTab(int index) {
        viewTabStats.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        viewTabMovies.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        viewTabUsers.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        viewTabCasts.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        viewTabVip.setVisibility(index == 4 ? View.VISIBLE : View.GONE);
        viewTabComments.setVisibility(index == 5 ? View.VISIBLE : View.GONE);

        int activeColor = ContextCompat.getColor(this, R.color.primary_red);
        int inactiveColor = ContextCompat.getColor(this, R.color.bg_card);

        tabBtnStats.setBackgroundColor(index == 0 ? activeColor : inactiveColor);
        tabBtnMovies.setBackgroundColor(index == 1 ? activeColor : inactiveColor);
        tabBtnUsers.setBackgroundColor(index == 2 ? activeColor : inactiveColor);
        tabBtnCasts.setBackgroundColor(index == 3 ? activeColor : inactiveColor);
        tabBtnVip.setBackgroundColor(index == 4 ? activeColor : inactiveColor);
        tabBtnComments.setBackgroundColor(index == 5 ? activeColor : inactiveColor);

        switch (index) {
            case 0: loadDashboardStats(); break;
            case 1: loadMovies(); break;
            case 2: loadUsers(); break;
            case 3: loadCastsTab(); break;
            case 4: loadVipPackages(); break;
            case 5: loadComments(); break;
        }
    }

    private void loadDashboardStats() {
        progressBar.setVisibility(View.VISIBLE);
        AdminController.getDashboardStats(new AdminController.DashboardStatsCallback() {
            @Override
            public void onSuccess(DashboardStats stats) {
                progressBar.setVisibility(View.GONE);
                if (stats == null) return;

                tvStatRevenue.setText(stats.getFormattedRevenue());
                tvStatRevenueSub.setText("Ước tính từ " + stats.getVipUsers() + " thành viên VIP");

                tvStatUsersTotal.setText(stats.getTotalUsers() + " Tài khoản");
                tvStatVipPercent.setText(stats.getVipUsers() + " VIP • " + stats.getRegularUsers() + " Thường");

                tvStatMoviesTotal.setText(stats.getTotalMovies() + " Phim");
                tvStatVipMoviesSub.setText(stats.getVipMovies() + " Phim VIP (>8.0★)");

                tvStatViewsTotal.setText(String.format(Locale.US, "%,d Lượt xem", stats.getTotalViews()));
                tvStatInteractions.setText(stats.getTotalComments() + " Bình luận • " + stats.getTotalRatings() + " Đánh giá");

                tvStatConversionRate.setText(stats.getVipConversionRate());
                int percent = 0;
                if (stats.getTotalUsers() > 0) {
                    percent = (int) Math.round(((double) stats.getVipUsers() / stats.getTotalUsers()) * 100);
                }
                pbVipConversion.setProgress(percent);
                tvStatConversionDetail.setText(stats.getVipUsers() + "/" + stats.getTotalUsers()
                        + " tài khoản đã nâng cấp VIP (" + stats.getVipConversionRate() + ")");

                populateTopMovies(stats.getTopMovies());
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải thống kê: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTopMovies(List<Movie> topMovies) {
        layoutTopMoviesContainer.removeAllViews();
        if (topMovies == null || topMovies.isEmpty()) {
            if (tvEmptyTopMovies != null) {
                tvEmptyTopMovies.setVisibility(View.VISIBLE);
                tvEmptyTopMovies.setText("Chưa có dữ liệu phim xem nhiều.");
                layoutTopMoviesContainer.addView(tvEmptyTopMovies);
            }
            return;
        }

        int maxViews = 1;
        for (Movie m : topMovies) {
            if (m.getViewCount() > maxViews) {
                maxViews = m.getViewCount();
            }
        }

        int rank = 1;
        for (Movie m : topMovies) {
            View row = getLayoutInflater().inflate(R.layout.item_admin_top_movie, layoutTopMoviesContainer, false);
            TextView tvTopRank = row.findViewById(R.id.tvTopRank);
            ImageView imgTopPoster = row.findViewById(R.id.imgTopPoster);
            TextView tvTopTitle = row.findViewById(R.id.tvTopTitle);
            TextView tvTopRating = row.findViewById(R.id.tvTopRating);
            TextView tvTopViews = row.findViewById(R.id.tvTopViews);
            ProgressBar pbTopViews = row.findViewById(R.id.pbTopViews);
            TextView tvTopVipBadge = row.findViewById(R.id.tvTopVipBadge);

            String rankText;
            if (rank == 1) rankText = "🥇";
            else if (rank == 2) rankText = "🥈";
            else if (rank == 3) rankText = "🥉";
            else rankText = "#" + rank;
            tvTopRank.setText(rankText);

            tvTopTitle.setText(m.getTitle());
            tvTopRating.setText(String.format(Locale.US, "★ %.1f", m.getVoteAverage()));
            tvTopViews.setText(String.format(Locale.US, "%,d lượt xem", m.getViewCount()));

            if (m.isVip()) {
                tvTopVipBadge.setVisibility(View.VISIBLE);
            } else {
                tvTopVipBadge.setVisibility(View.GONE);
            }

            int progress = (int) (((double) m.getViewCount() / maxViews) * 100);
            pbTopViews.setProgress(Math.max(5, progress));

            Glide.with(AdminDashboardActivity.this)
                    .load(m.getPosterPath())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgTopPoster);

            layoutTopMoviesContainer.addView(row);
            rank++;
        }
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getAllMovies(new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (movies != null) movieList.addAll(movies);
                tvMoviesCount.setText("Danh sách phim (" + movieList.size() + ")");
                movieAdapter.setMovies(movieList);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải phim: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMovieDialog(Movie existingMovie) {
        AdminDialogHelper.showMovieDialog(
                this,
                existingMovie,
                progressBar,
                (etPoster, imgPreview) -> {
                    currentPosterInputTarget = etPoster;
                    currentPosterPreviewTarget = imgPreview;
                    if (pickPosterLauncher != null) {
                        pickPosterLauncher.launch("image/*");
                    }
                },
                (etVideo, tvStatus) -> {
                    currentVideoInputTarget = etVideo;
                    currentVideoStatusTarget = tvStatus;
                    if (pickVideoLauncher != null) {
                        pickVideoLauncher.launch("video/*");
                    }
                },
                this::loadMovies
        );
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        AdminController.getAllUsers(new AdminController.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList.clear();
                int vipCount = 0;
                if (users != null) {
                    userList.addAll(users);
                    for (User u : users) {
                        if (u.isVip()) vipCount++;
                    }
                }
                tvUsersCount.setText("Danh sách người dùng (" + userList.size() + ")");
                tvVipUsersCount.setText(vipCount + " thành viên VIP");
                userAdapter.setUsers(userList);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải người dùng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResetPasswordDialog(User user) {
        AdminDialogHelper.showResetPasswordDialog(this, user, null);
    }

    private void loadCastsTab() {
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getAllMovies(new MovieController.MovieListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                progressBar.setVisibility(View.GONE);
                spinnerMovieList.clear();
                if (movies != null) spinnerMovieList.addAll(movies);

                List<String> movieTitles = new ArrayList<>();
                for (Movie m : spinnerMovieList) {
                    movieTitles.add("#" + m.getId() + " - " + m.getTitle());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminDashboardActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, movieTitles);
                spinnerCastMovie.setAdapter(adapter);

                spinnerCastMovie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position >= 0 && position < spinnerMovieList.size()) {
                            selectedMovieId = spinnerMovieList.get(position).getId();
                            loadCastsForMovie(selectedMovieId);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                if (!spinnerMovieList.isEmpty()) {
                    selectedMovieId = spinnerMovieList.get(0).getId();
                    loadCastsForMovie(selectedMovieId);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadCastsForMovie(int movieId) {
        if (movieId <= 0) return;
        progressBar.setVisibility(View.VISIBLE);
        MovieController.getMovieCasts(movieId, new MovieController.CastListCallback() {
            @Override
            public void onSuccess(List<Cast> casts) {
                progressBar.setVisibility(View.GONE);
                castList.clear();
                if (casts != null) castList.addAll(casts);
                tvCastsCount.setText("Diễn viên (" + castList.size() + ")");
                castAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                castList.clear();
                tvCastsCount.setText("Diễn viên (0)");
                castAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddCastDialog(int movieId) {
        AdminDialogHelper.showAddCastDialog(this, movieId, progressBar, () -> loadCastsForMovie(movieId));
    }

    private void loadVipPackages() {
        progressBar.setVisibility(View.VISIBLE);
        AdminController.getVipPackages(new AdminController.VipPackageListCallback() {
            @Override
            public void onSuccess(List<VipPackage> packages) {
                progressBar.setVisibility(View.GONE);
                vipPkgList.clear();
                if (packages != null) vipPkgList.addAll(packages);
                tvVipPkgCount.setText("Danh sách gói VIP (" + vipPkgList.size() + ")");
                vipPkgAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải gói VIP: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVipPackageDialog(VipPackage existingPkg) {
        AdminDialogHelper.showVipPackageDialog(this, existingPkg, progressBar, this::loadVipPackages);
    }

    private void showDeleteConfirmDialog(String title, String message, Runnable onConfirm) {
        AdminDialogHelper.showDeleteConfirmDialog(this, title, message, onConfirm);
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);
        AdminController.getAllComments(new AdminController.CommentListCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                progressBar.setVisibility(View.GONE);
                commentList.clear();
                if (comments != null) commentList.addAll(comments);
                tvCommentsCount.setText("Quản lý bình luận (" + commentList.size() + ")");
                if (tvEmptyComments != null) {
                    tvEmptyComments.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                commentAdapter.setComments(commentList);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi tải bình luận: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
