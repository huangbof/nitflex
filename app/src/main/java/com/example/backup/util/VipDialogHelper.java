package com.example.backup.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.backup.R;
import com.example.backup.controller.AuthController;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.service.SessionManager;

import java.util.Locale;

public class VipDialogHelper {

    public interface OnVipUpgradedListener {
        void onUpgraded();
    }

    public static void showUpgradeDialog(Activity activity, Movie movie, OnVipUpgradedListener listener) {
        if (activity == null || activity.isFinishing()) return;

        SessionManager session = new SessionManager(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_vip_upgrade, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMovieNotice = dialogView.findViewById(R.id.tvVipMovieNotice);
        Button btnUpgradeMonthly = dialogView.findViewById(R.id.btnUpgradeMonthly);
        Button btnUpgradeYearly = dialogView.findViewById(R.id.btnUpgradeYearly);
        View btnClose = dialogView.findViewById(R.id.btnVipClose);

        if (movie != null) {
            tvMovieNotice.setVisibility(View.VISIBLE);
            tvMovieNotice.setText(String.format(Locale.US,
                    "🎬 Phim \"%s\" đạt điểm đánh giá ★ %.1f TMDB (> 8.0 điểm), thuộc danh mục Phim VIP Đặc Sắc. Vui lòng kích hoạt gói VIP để thưởng thức ngay!",
                    movie.getTitle(), movie.getVoteAverage()));
        } else {
            tvMovieNotice.setVisibility(View.GONE);
        }

        View.OnClickListener upgradeAction = v -> {
            session.setVip(true);
            AuthController.upgradeToVip(session.getUserId(), new MovieController.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(activity, "🎉 Chúc mừng bạn đã nâng cấp thành công gói Nit Flex VIP!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    if (listener != null) listener.onUpgraded();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(activity, "🎉 Chúc mừng bạn đã nâng cấp thành công gói Nit Flex VIP!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    if (listener != null) listener.onUpgraded();
                }
            });
        };

        btnUpgradeMonthly.setOnClickListener(upgradeAction);
        btnUpgradeYearly.setOnClickListener(upgradeAction);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
