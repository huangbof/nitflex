package com.example.backup.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.controller.AdminController;
import com.example.backup.model.Cast;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;

import java.util.Locale;

/**
 * Tách biệt toàn bộ logic khởi tạo và hiển thị Dialog quản trị ra khỏi AdminDashboardActivity
 * Giúp giải quyết triệt để hội chứng "Fat Activity" trong mô hình kiến trúc MVC.
 */
public class AdminDialogHelper {

    public interface OnPickPosterListener {
        void onPick(EditText etPoster, ImageView imgPreview);
    }

    public interface OnPickVideoListener {
        void onPick(EditText etVideo, TextView tvStatus);
    }

    public interface OnDialogActionListener {
        void onSuccess();
    }

    public static void showMovieDialog(
            Activity activity,
            Movie existingMovie,
            View progressBar,
            OnPickPosterListener pickPosterListener,
            OnPickVideoListener pickVideoListener,
            OnDialogActionListener onSavedListener) {

        if (activity == null || activity.isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_admin_add_movie, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitle           = dialogView.findViewById(R.id.tvAdminDialogMovieTitle);
        EditText etTitle           = dialogView.findViewById(R.id.etAdminMovieTitle);
        EditText etOrig            = dialogView.findViewById(R.id.etAdminMovieOriginalTitle);
        EditText etRating          = dialogView.findViewById(R.id.etAdminMovieRating);
        EditText etDate            = dialogView.findViewById(R.id.etAdminMovieReleaseDate);
        EditText etPoster          = dialogView.findViewById(R.id.etAdminMoviePoster);
        ImageView imgPosterPreview = dialogView.findViewById(R.id.imgAdminPosterPreview);
        TextView btnPickPoster     = dialogView.findViewById(R.id.btnPickPosterFromDevice);
        EditText etVideo           = dialogView.findViewById(R.id.etAdminMovieVideoUrl);
        TextView tvVideoStatus     = dialogView.findViewById(R.id.tvAdminVideoStatus);
        EditText etOverview        = dialogView.findViewById(R.id.etAdminMovieOverview);
        TextView btnQuickRawSample = dialogView.findViewById(R.id.btnQuickRawSample);
        TextView btnQuickRawEndgame= dialogView.findViewById(R.id.btnQuickRawEndgame);
        TextView btnPickDevice     = dialogView.findViewById(R.id.btnPickVideoFromDevice);
        Button btnCancel           = dialogView.findViewById(R.id.btnAdminMovieCancel);
        Button btnSave             = dialogView.findViewById(R.id.btnAdminMovieSave);

        if (btnPickPoster != null) {
            btnPickPoster.setOnClickListener(v -> {
                if (pickPosterListener != null) {
                    pickPosterListener.onPick(etPoster, imgPosterPreview);
                }
            });
        }

        if (btnQuickRawSample != null) {
            btnQuickRawSample.setOnClickListener(v -> {
                etVideo.setText("raw/sample_movie.mp4");
                if (tvVideoStatus != null) {
                    tvVideoStatus.setVisibility(View.VISIBLE);
                    tvVideoStatus.setText("Đã chọn file MP4: res/raw/sample_movie.mp4");
                }
            });
        }

        if (btnQuickRawEndgame != null) {
            btnQuickRawEndgame.setOnClickListener(v -> {
                etVideo.setText("raw/endgame.mp4");
                if (tvVideoStatus != null) {
                    tvVideoStatus.setVisibility(View.VISIBLE);
                    tvVideoStatus.setText("Đã chọn file MP4: res/raw/endgame.mp4");
                }
            });
        }

        if (btnPickDevice != null) {
            btnPickDevice.setOnClickListener(v -> {
                if (pickVideoListener != null) {
                    pickVideoListener.onPick(etVideo, tvVideoStatus);
                }
            });
        }

        if (existingMovie != null) {
            tvTitle.setText("✏️ Chỉnh Sửa Phim");
            etTitle.setText(existingMovie.getTitle());
            etOrig.setText(existingMovie.getOriginalTitle());
            etRating.setText(String.valueOf(existingMovie.getVoteAverage()));
            etDate.setText(existingMovie.getReleaseDate());
            etPoster.setText(existingMovie.getPosterPath());
            if (existingMovie.getPosterPath() != null && !existingMovie.getPosterPath().isEmpty() && imgPosterPreview != null) {
                imgPosterPreview.setVisibility(View.VISIBLE);
                Glide.with(activity)
                        .load(existingMovie.getPosterPath())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgPosterPreview);
            }
            etVideo.setText(existingMovie.getVideoUrl());
            if (existingMovie.getVideoUrl() != null && !existingMovie.getVideoUrl().isEmpty() && tvVideoStatus != null) {
                tvVideoStatus.setVisibility(View.VISIBLE);
                tvVideoStatus.setText("Nguồn hiện tại: " + existingMovie.getVideoUrl());
            }
            etOverview.setText(existingMovie.getOverview());
            btnSave.setText("Cập Nhật");
        } else {
            etVideo.setText("raw/sample_movie.mp4");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(activity, "Vui lòng nhập tên phim!", Toast.LENGTH_SHORT).show();
                return;
            }
            double rating = 8.0;
            try {
                rating = Double.parseDouble(etRating.getText().toString().trim());
            } catch (Exception ignored) {}

            Movie m = existingMovie != null ? existingMovie : new Movie();
            m.setTitle(title);
            m.setOriginalTitle(etOrig.getText().toString().trim());
            m.setVoteAverage(rating);
            m.setReleaseDate(etDate.getText().toString().trim());
            m.setPosterPath(etPoster.getText().toString().trim());
            m.setVideoUrl(etVideo.getText().toString().trim());
            m.setOverview(etOverview.getText().toString().trim());
            if (m.getGenreId() <= 0) {
                m.setGenreId(28);
            }

            dialog.dismiss();
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            AdminController.AdminCallback callback = new AdminController.AdminCallback() {
                @Override
                public void onSuccess(String message) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSuccess();
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            };

            if (existingMovie != null) {
                AdminController.updateMovie(m, callback);
            } else {
                AdminController.addMovie(m, callback);
            }
        });

        dialog.show();
    }

    public static void showResetPasswordDialog(Context context, User user, OnDialogActionListener onResetListener) {
        if (context == null || user == null) return;
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("🔑 Đặt lại mật khẩu cho " + user.getUsername());
        b.setMessage("Mật khẩu mới sẽ được cập nhật thành: 123456");
        b.setPositiveButton("Xác nhận", (dialog, which) -> {
            AdminController.resetUserPassword(user.getId(), "123456", new AdminController.AdminCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(context, "Đã đặt lại mật khẩu của " + user.getUsername() + " thành: 123456", Toast.LENGTH_LONG).show();
                    if (onResetListener != null) onResetListener.onSuccess();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Hủy", null);
        b.show();
    }

    public static void showAddCastDialog(Context context, int movieId, View progressBar, OnDialogActionListener onSavedListener) {
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_add_cast, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etActor     = dialogView.findViewById(R.id.etAdminCastActor);
        EditText etCharacter = dialogView.findViewById(R.id.etAdminCastCharacter);
        EditText etProfile   = dialogView.findViewById(R.id.etAdminCastProfile);
        Button btnCancel     = dialogView.findViewById(R.id.btnAdminCastCancel);
        Button btnSave       = dialogView.findViewById(R.id.btnAdminCastSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String actor = etActor.getText().toString().trim();
            if (actor.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên diễn viên!", Toast.LENGTH_SHORT).show();
                return;
            }
            Cast c = new Cast();
            c.setMovieId(movieId);
            c.setActorName(actor);
            c.setCharacterName(etCharacter.getText().toString().trim());
            c.setProfilePath(etProfile.getText().toString().trim());

            dialog.dismiss();
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            AdminController.addCast(c, new AdminController.AdminCallback() {
                @Override
                public void onSuccess(String message) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSuccess();
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    public static void showVipPackageDialog(Context context, VipPackage existingPkg, View progressBar, OnDialogActionListener onSavedListener) {
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_vip_package, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvTitle   = dialogView.findViewById(R.id.tvAdminDialogVipTitle);
        EditText etName    = dialogView.findViewById(R.id.etAdminPkgName);
        EditText etCode    = dialogView.findViewById(R.id.etAdminPkgCode);
        EditText etPrice   = dialogView.findViewById(R.id.etAdminPkgPrice);
        EditText etDuration= dialogView.findViewById(R.id.etAdminPkgDuration);
        EditText etDesc    = dialogView.findViewById(R.id.etAdminPkgDesc);
        Button btnCancel   = dialogView.findViewById(R.id.btnAdminPkgCancel);
        Button btnSave     = dialogView.findViewById(R.id.btnAdminPkgSave);

        if (existingPkg != null) {
            tvTitle.setText("👑 Chỉnh Sửa Gói VIP");
            etName.setText(existingPkg.getName());
            etCode.setText(existingPkg.getCode());
            etCode.setEnabled(false);
            etPrice.setText(String.format(Locale.US, "%.0f", existingPkg.getPrice()));
            etDuration.setText(String.valueOf(existingPkg.getDurationDays()));
            etDesc.setText(existingPkg.getDescription());
            btnSave.setText("Cập Nhật");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String code = etCode.getText().toString().trim();
            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ tên và mã gói!", Toast.LENGTH_SHORT).show();
                return;
            }
            double price = 0;
            int days = 30;
            try {
                price = Double.parseDouble(etPrice.getText().toString().trim());
                days = Integer.parseInt(etDuration.getText().toString().trim());
            } catch (Exception ignored) {}

            VipPackage p = existingPkg != null ? existingPkg : new VipPackage();
            p.setName(name);
            p.setCode(code);
            p.setPrice(price);
            p.setDurationDays(days);
            p.setDescription(etDesc.getText().toString().trim());

            dialog.dismiss();
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            AdminController.AdminCallback callback = new AdminController.AdminCallback() {
                @Override
                public void onSuccess(String message) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    if (onSavedListener != null) onSavedListener.onSuccess();
                }

                @Override
                public void onError(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            };

            if (existingPkg != null) {
                AdminController.updateVipPackage(p, callback);
            } else {
                AdminController.addVipPackage(p, callback);
            }
        });

        dialog.show();
    }

    public static void showDeleteConfirmDialog(Context context, String title, String message, Runnable onConfirm) {
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
