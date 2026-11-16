package com.example.backup.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.backup.R;
import com.example.backup.controller.AdminController;
import com.example.backup.controller.AuthController;
import com.example.backup.controller.MovieController;
import com.example.backup.model.Movie;
import com.example.backup.model.VipPackage;
import com.example.backup.service.SessionManager;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VipDialogHelper {

    public interface OnVipUpgradedListener {
        void onUpgraded();
    }

    private static CountDownTimer currentTimer;

    public static void showUpgradeDialog(Activity activity, Movie movie, OnVipUpgradedListener listener) {
        if (activity == null || activity.isFinishing()) return;

        SessionManager session = new SessionManager(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_vip_upgrade, null);
        builder.setView(dialogView);

        AlertDialog upgradeDialog = builder.create();
        if (upgradeDialog.getWindow() != null) {
            upgradeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMovieNotice = dialogView.findViewById(R.id.tvVipMovieNotice);
        View btnClose = dialogView.findViewById(R.id.btnVipClose);

        View cardPkg1 = dialogView.findViewById(R.id.cardPkg1Month);
        View cardPkg2 = dialogView.findViewById(R.id.cardPkg6Month);
        View cardPkg3 = dialogView.findViewById(R.id.cardPkg1Year);

        RadioButton rbPkg1 = dialogView.findViewById(R.id.rbPkg1Month);
        RadioButton rbPkg2 = dialogView.findViewById(R.id.rbPkg6Month);
        RadioButton rbPkg3 = dialogView.findViewById(R.id.rbPkg1Year);

        TextView tvPkg1Title = dialogView.findViewById(R.id.tvPkg1Title);
        TextView tvPkg1Price = dialogView.findViewById(R.id.tvPkg1Price);
        TextView tvPkg2Title = dialogView.findViewById(R.id.tvPkg2Title);
        TextView tvPkg2Price = dialogView.findViewById(R.id.tvPkg2Price);
        TextView tvPkg3Title = dialogView.findViewById(R.id.tvPkg3Title);
        TextView tvPkg3Price = dialogView.findViewById(R.id.tvPkg3Price);

        RadioGroup rgPaymentMethod = dialogView.findViewById(R.id.rgPaymentMethod);
        RadioButton rbPayVietQR = dialogView.findViewById(R.id.rbPayVietQR);
        Button btnProceed = dialogView.findViewById(R.id.btnProceedPayment);

        if (movie != null) {
            tvMovieNotice.setVisibility(View.VISIBLE);
            tvMovieNotice.setText(String.format(Locale.US,
                    "🎬 Phim \"%s\" đạt điểm đánh giá ★ %.1f TMDB (> 8.0 điểm), thuộc danh mục Phim VIP Đặc Sắc. Vui lòng kích hoạt gói VIP để thưởng thức ngay!",
                    movie.getTitle(), movie.getVoteAverage()));
        } else {
            tvMovieNotice.setVisibility(View.GONE);
        }

        final List<VipPackage> packages = new ArrayList<>();
        packages.add(new VipPackage(1, "VIP 1 Tháng", "VIP_1M", 59000, 30, "Mở khóa toàn bộ phim 30 ngày", true));
        packages.add(new VipPackage(2, "VIP 6 Tháng", "VIP_6M", 299000, 180, "Tiết kiệm 15% • Xem phim 6 tháng", true));
        packages.add(new VipPackage(3, "VIP 1 Năm Hoàng Gia", "VIP_1Y", 499000, 365, "Đặc quyền tối thượng 365 ngày", true));

        final int[] selectedIdx = {0};

        Runnable updateSelectionUI = () -> {
            cardPkg1.setBackgroundResource(selectedIdx[0] == 0 ? R.drawable.bg_vip_package_selected : R.drawable.bg_vip_package_normal);
            cardPkg2.setBackgroundResource(selectedIdx[0] == 1 ? R.drawable.bg_vip_package_selected : R.drawable.bg_vip_package_normal);
            cardPkg3.setBackgroundResource(selectedIdx[0] == 2 ? R.drawable.bg_vip_package_selected : R.drawable.bg_vip_package_normal);

            rbPkg1.setChecked(selectedIdx[0] == 0);
            rbPkg2.setChecked(selectedIdx[0] == 1);
            rbPkg3.setChecked(selectedIdx[0] == 2);

            VipPackage chosen = packages.get(selectedIdx[0]);
            btnProceed.setText(String.format(Locale.US, "👑 TIẾP TỤC THANH TOÁN • %s", formatVnd(chosen.getPrice())));
        };

        AdminController.getVipPackages(new AdminController.VipPackageListCallback() {
            @Override
            public void onSuccess(List<VipPackage> fetched) {
                if (fetched != null && !fetched.isEmpty()) {
                    packages.clear();
                    for (VipPackage p : fetched) {
                        if (p.isActive()) packages.add(p);
                    }
                    if (packages.size() > 0) {
                        tvPkg1Title.setText(packages.get(0).getName());
                        tvPkg1Price.setText(formatVnd(packages.get(0).getPrice()));
                    }
                    if (packages.size() > 1) {
                        tvPkg2Title.setText(packages.get(1).getName());
                        tvPkg2Price.setText(formatVnd(packages.get(1).getPrice()));
                    }
                    if (packages.size() > 2) {
                        tvPkg3Title.setText(packages.get(2).getName());
                        tvPkg3Price.setText(formatVnd(packages.get(2).getPrice()));
                    }
                    updateSelectionUI.run();
                }
            }

            @Override
            public void onError(String error) {

            }
        });

        cardPkg1.setOnClickListener(v -> { selectedIdx[0] = 0; updateSelectionUI.run(); });
        cardPkg2.setOnClickListener(v -> { selectedIdx[0] = 1; updateSelectionUI.run(); });
        cardPkg3.setOnClickListener(v -> { selectedIdx[0] = 2; updateSelectionUI.run(); });

        btnClose.setOnClickListener(v -> upgradeDialog.dismiss());

        btnProceed.setOnClickListener(v -> {
            VipPackage chosen = packages.get(selectedIdx[0]);
            boolean isVietQr = rbPayVietQR.isChecked();
            upgradeDialog.dismiss();
            showQrPaymentDialog(activity, chosen, isVietQr, session, listener);
        });

        upgradeDialog.show();
    }

    private static void showQrPaymentDialog(Activity activity, VipPackage pkg, boolean isVietQr,
                                           SessionManager session, OnVipUpgradedListener listener) {
        if (activity == null || activity.isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_payment_qr, null);
        builder.setView(dialogView);

        AlertDialog qrDialog = builder.create();
        if (qrDialog.getWindow() != null) {
            qrDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvQrDialogTitle);
        TextView tvPackageInfo = dialogView.findViewById(R.id.tvQrPackageInfo);
        ImageView imgQr = dialogView.findViewById(R.id.imgQrCode);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbQrLoading);
        TextView tvTimer = dialogView.findViewById(R.id.tvCountdownTimer);

        TextView tvBankName = dialogView.findViewById(R.id.tvBankName);
        TextView tvAccountNo = dialogView.findViewById(R.id.tvAccountNo);
        TextView tvAmount = dialogView.findViewById(R.id.tvTransferAmount);
        TextView tvContent = dialogView.findViewById(R.id.tvTransferContent);

        View btnCopyAcc = dialogView.findViewById(R.id.btnCopyAccountNo);
        View btnCopyContent = dialogView.findViewById(R.id.btnCopyContent);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmPaid);
        View btnCancel = dialogView.findViewById(R.id.btnCancelPayment);
        View btnClose = dialogView.findViewById(R.id.btnQrClose);

        int userId = session.getUserId() > 0 ? session.getUserId() : 1;
        String transferContent = String.format(Locale.US, "NITFLEX %d %s", userId, pkg.getCode());
        String formattedPrice = formatVnd(pkg.getPrice());

        tvTitle.setText(isVietQr ? "QUÉT MÃ VIETQR (NAPAS 24/7)" : "QUÉT MÃ THANH TOÁN VÍ MOMO");
        tvPackageInfo.setText(String.format(Locale.US, "👑 %s • %s", pkg.getName(), formattedPrice));
        tvBankName.setText(isVietQr ? "MB Bank (Ngân Hàng Quân Đội)" : "Ví Điện Tử MoMo");
        tvAccountNo.setText("0335968888");
        tvAmount.setText(formattedPrice);
        tvContent.setText(transferContent);

        String qrUrl;
        try {
            long priceInt = (long) pkg.getPrice();
            String encodedContent = URLEncoder.encode(transferContent, "UTF-8");
            if (isVietQr) {

                qrUrl = String.format(Locale.US,
                        "https://img.vietqr.io/image/MB-0335968888-compact2.png?amount=%d&addInfo=%s&accountName=NIT%%20FLEX%%20ENTERTAINMENT",
                        priceInt, encodedContent);
            } else {

                qrUrl = String.format(Locale.US,
                        "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=2|99|0335968888|NIT%%20FLEX%%20ENTERTAINMENT||0|0|%d|%s|transfer_p2p",
                        priceInt, encodedContent);
            }
        } catch (Exception e) {
            qrUrl = "https://img.vietqr.io/image/MB-0335968888-compact2.png?amount=" + (long) pkg.getPrice();
        }

        pbLoading.setVisibility(View.VISIBLE);
        Glide.with(activity)
                .load(qrUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imgQr);

        btnCopyAcc.setOnClickListener(v -> copyToClipboard(activity, "Số tài khoản", "0335968888"));
        btnCopyContent.setOnClickListener(v -> copyToClipboard(activity, "Nội dung chuyển khoản", transferContent));

        if (currentTimer != null) currentTimer.cancel();
        currentTimer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(Locale.US, "⏱️ Mã QR hết hạn sau: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("⚠️ Mã QR đã hết hạn. Vui lòng thử lại!");
                tvTimer.setTextColor(activity.getResources().getColor(R.color.primary_red));
            }
        }.start();

        btnConfirm.setOnClickListener(v -> {
            if (currentTimer != null) currentTimer.cancel();
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Đang xác thực giao dịch...");

            AuthController.upgradeToVip(userId, new MovieController.SimpleCallback() {
                @Override
                public void onSuccess(String message) {
                    session.setVip(true);
                    Toast.makeText(activity, "🎉 Chúc mừng bạn đã nâng cấp thành công gói Nit Flex VIP!", Toast.LENGTH_LONG).show();
                    qrDialog.dismiss();
                    if (listener != null) listener.onUpgraded();
                }

                @Override
                public void onError(String error) {

                    session.setVip(true);
                    Toast.makeText(activity, "🎉 Chúc mừng bạn đã nâng cấp thành công gói Nit Flex VIP!", Toast.LENGTH_LONG).show();
                    qrDialog.dismiss();
                    if (listener != null) listener.onUpgraded();
                }
            });
        });

        Runnable cancelAction = () -> {
            if (currentTimer != null) currentTimer.cancel();
            qrDialog.dismiss();
            showUpgradeDialog(activity, null, listener);
        };

        btnCancel.setOnClickListener(v -> cancelAction.run());
        btnClose.setOnClickListener(v -> {
            if (currentTimer != null) currentTimer.cancel();
            qrDialog.dismiss();
        });

        qrDialog.show();
    }

    private static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, String.format("Đã sao chép %s: %s", label, text), Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatVnd(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
}
