package com.example.backup.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.backup.R;
import com.example.backup.controller.AuthController;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegEmail, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername        = findViewById(R.id.etRegUsername);
        etRegEmail           = findViewById(R.id.etRegEmail);
        etRegPassword        = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister          = findViewById(R.id.btnRegister);
        tvGoToLogin          = findViewById(R.id.tvGoToLogin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        btnRegister.setOnClickListener(v -> {
            String u  = etRegUsername.getText().toString().trim();
            String e  = etRegEmail.getText().toString().trim();
            String p  = etRegPassword.getText().toString().trim();
            String cp = etRegConfirmPassword.getText().toString().trim();

            // 1. Kiểm tra để trống
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

            // 2. Kiểm tra tên đăng nhập (độ dài >= 3, không chứa dấu cách)
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

            // 3. Kiểm tra định dạng Email
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                etRegEmail.setError("Địa chỉ email không đúng định dạng (ví dụ: user@gmail.com)!");
                etRegEmail.requestFocus();
                return;
            }

            // 4. Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
            if (p.length() < 6) {
                etRegPassword.setError("Mật khẩu bảo mật phải có ít nhất 6 ký tự!");
                etRegPassword.requestFocus();
                return;
            }

            // 5. Kiểm tra mật khẩu xác nhận khớp nhau
            if (!p.equals(cp)) {
                etRegConfirmPassword.setError("Mật khẩu xác nhận không khớp với mật khẩu đã nhập!");
                etRegConfirmPassword.requestFocus();
                return;
            }

            // Bắt đầu gửi dữ liệu đăng ký tới CSDL MySQL
            btnRegister.setEnabled(false);
            btnRegister.setText("Đang đăng ký...");

            AuthController.register(u, p, e, new AuthController.AuthCallback() {
                @Override
                public void onSuccess(int userId, String username) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Chào mừng " + username, Toast.LENGTH_LONG).show();

                    // Tự động chuyển về màn hình đăng nhập và điền sẵn username
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("REGISTERED_USERNAME", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("ĐĂNG KÝ NGAY");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }
}
