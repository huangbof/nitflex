package com.example.backup.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.backup.R;
import com.example.backup.controller.AuthController;
import com.example.backup.service.OtpService;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegEmail, etRegPhone, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    private String currentOtpCode = null;
    private CountDownTimer otpCountDownTimer = null;
    private AlertDialog otpDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername        = findViewById(R.id.etRegUsername);
        etRegEmail           = findViewById(R.id.etRegEmail);
        etRegPhone           = findViewById(R.id.etRegPhone);
        etRegPassword        = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister          = findViewById(R.id.btnRegister);
        tvGoToLogin          = findViewById(R.id.tvGoToLogin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        btnRegister.setOnClickListener(v -> validateAndProceedToOtp());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndProceedToOtp() {
        String u     = etRegUsername.getText().toString().trim();
        String e     = etRegEmail.getText().toString().trim();
        String phone = etRegPhone != null ? etRegPhone.getText().toString().trim() : "";
        String p     = etRegPassword.getText().toString().trim();
        String cp    = etRegConfirmPassword.getText().toString().trim();

        if (u.isEmpty()) {
            etRegUsername.setError("Vui lòng nhập tên đăng nhập!");
            etRegUsername.requestFocus();
            return;
        }
        if (e.isEmpty()) {
            etRegEmail.setError("Vui lòng nhập địa chỉ email!");
            etRegEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            if (etRegPhone != null) {
                etRegPhone.setError("Vui lòng nhập số điện thoại nhận mã OTP!");
                etRegPhone.requestFocus();
            }
            return;
        }
        if (p.isEmpty()) {
            etRegPassword.setError("Vui lòng nhập mật khẩu!");
            etRegPassword.requestFocus();
            return;
        }
        if (cp.isEmpty()) {
            etRegConfirmPassword.setError("Vui lòng xác nhận lại mật khẩu!");
            etRegConfirmPassword.requestFocus();
            return;
        }

        if (u.length() < 3) {
            etRegUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự!");
            etRegUsername.requestFocus();
            return;
        }
        if (u.contains(" ")) {
            etRegUsername.setError("Tên đăng nhập không được chứa khoảng trắng!");
            etRegUsername.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            etRegEmail.setError("Địa chỉ email không đúng định dạng (ví dụ: user@gmail.com)!");
            etRegEmail.requestFocus();
            return;
        }

        // Định dạng số điện thoại Việt Nam (10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09 hoặc +84)
        if (!phone.matches("^(0|\\+84)(3|5|7|8|9)[0-9]{8}$")) {
            if (etRegPhone != null) {
                etRegPhone.setError("Số điện thoại không hợp lệ (ví dụ: 0912345678)!");
                etRegPhone.requestFocus();
            }
            return;
        }

        if (p.length() < 6) {
            etRegPassword.setError("Mật khẩu bảo mật phải có ít nhất 6 ký tự!");
            etRegPassword.requestFocus();
            return;
        }

        if (!p.equals(cp)) {
            etRegConfirmPassword.setError("Mật khẩu xác nhận không khớp với mật khẩu đã nhập!");
            etRegConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang kiểm tra thông tin...");

        // Kiểm tra trùng lặp trên CSDL trước khi gửi OTP
        AuthController.checkBeforeRegister(u, e, phone, new AuthController.CheckCallback() {
            @Override
            public void onValid() {
                btnRegister.setEnabled(true);
                btnRegister.setText("ĐĂNG KÝ NGAY");

                // Sinh và gửi mã OTP
                currentOtpCode = OtpService.generateOtp();
                OtpService.sendOtp(RegisterActivity.this, phone, currentOtpCode);

                Toast.makeText(RegisterActivity.this,
                        "📲 Đã gửi mã OTP đến số điện thoại " + phone,
                        Toast.LENGTH_LONG).show();

                showOtpDialog(u, p, e, phone);
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                btnRegister.setText("ĐĂNG KÝ NGAY");
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOtpDialog(String username, String password, String email, String phone) {
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verification, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        otpDialog = builder.create();
        if (otpDialog.getWindow() != null) {
            otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            otpDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView tvOtpPhoneNotice = dialogView.findViewById(R.id.tvOtpPhoneNotice);
        EditText etOtpCode         = dialogView.findViewById(R.id.etOtpCode);
        TextView tvOtpTimer       = dialogView.findViewById(R.id.tvOtpTimer);
        Button btnOtpAutoFill     = dialogView.findViewById(R.id.btnOtpAutoFill);
        Button btnOtpVerify       = dialogView.findViewById(R.id.btnOtpVerify);
        Button btnOtpResend       = dialogView.findViewById(R.id.btnOtpResend);
        TextView tvOtpCancel      = dialogView.findViewById(R.id.tvOtpCancel);

        String maskedPhone = phone.length() >= 7
                ? phone.substring(0, phone.length() - 4) + "****"
                : phone;

        tvOtpPhoneNotice.setText("Mã OTP 6 chữ số đã được gửi tới số điện thoại:\n"
                + phone + "\n\nVui lòng kiểm tra tin nhắn SMS hoặc thanh thông báo để nhập mã:");

        // Bắt đầu đếm ngược 60 giây
        startOtpTimer(tvOtpTimer, btnOtpResend);

        // Nút tự động điền (hỗ trợ kiểm thử / demo học tập nhanh)
        btnOtpAutoFill.setOnClickListener(v -> {
            if (currentOtpCode != null) {
                etOtpCode.setText(currentOtpCode);
                etOtpCode.setSelection(currentOtpCode.length());
                Toast.makeText(RegisterActivity.this, "Đã tự động điền mã OTP: " + currentOtpCode, Toast.LENGTH_SHORT).show();
            }
        });

        // Nút gửi lại mã OTP
        btnOtpResend.setOnClickListener(v -> {
            currentOtpCode = OtpService.generateOtp();
            OtpService.sendOtp(RegisterActivity.this, phone, currentOtpCode);
            Toast.makeText(RegisterActivity.this, "Đã gửi lại mã OTP mới!", Toast.LENGTH_SHORT).show();
            startOtpTimer(tvOtpTimer, btnOtpResend);
        });

        // Nút xác nhận mã OTP
        btnOtpVerify.setOnClickListener(v -> {
            String enteredCode = etOtpCode.getText().toString().trim();
            if (enteredCode.isEmpty()) {
                etOtpCode.setError("Vui lòng nhập mã OTP!");
                etOtpCode.requestFocus();
                return;
            }

            if (enteredCode.length() != 6) {
                etOtpCode.setError("Mã OTP phải có đúng 6 chữ số!");
                etOtpCode.requestFocus();
                return;
            }

            if (!enteredCode.equals(currentOtpCode)) {
                etOtpCode.setError("Mã OTP không chính xác. Vui lòng kiểm tra lại!");
                etOtpCode.requestFocus();
                return;
            }

            // Mã OTP hợp lệ -> Lưu tài khoản vào MySQL
            btnOtpVerify.setEnabled(false);
            btnOtpVerify.setText("Đang hoàn tất đăng ký...");

            AuthController.register(username, password, email, phone, new AuthController.AuthCallback() {
                @Override
                public void onSuccess(int userId, String registeredUsername) {
                    if (otpCountDownTimer != null) otpCountDownTimer.cancel();
                    if (otpDialog != null && otpDialog.isShowing()) otpDialog.dismiss();

                    Toast.makeText(RegisterActivity.this,
                            "🎉 Đăng ký & Xác thực SĐT thành công! Chào mừng " + registeredUsername,
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("REGISTERED_USERNAME", registeredUsername);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnOtpVerify.setEnabled(true);
                    btnOtpVerify.setText("XÁC NHẬN & HOÀN TẤT ĐĂNG KÝ");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        // Hủy bỏ
        tvOtpCancel.setOnClickListener(v -> {
            if (otpCountDownTimer != null) otpCountDownTimer.cancel();
            if (otpDialog != null && otpDialog.isShowing()) otpDialog.dismiss();
            btnRegister.setEnabled(true);
            btnRegister.setText("ĐĂNG KÝ NGAY");
        });

        otpDialog.show();
    }

    private void startOtpTimer(TextView tvTimer, Button btnResend) {
        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }

        btnResend.setEnabled(false);
        btnResend.setTextColor(ContextCompat.getColor(this, R.color.text_gray));

        otpCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                tvTimer.setText("Mã có hiệu lực trong: " + sec + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Mã OTP đã hết hiệu lực!");
                btnResend.setEnabled(true);
                btnResend.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.primary_red));
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();
        }
    }
}
