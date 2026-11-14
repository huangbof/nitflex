package com.example.backup.view;

import android.content.Intent;
import android.os.Bundle;
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

    private EditText etRegUsername, etRegEmail, etRegPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail    = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister   = findViewById(R.id.btnRegister);
        tvGoToLogin   = findViewById(R.id.tvGoToLogin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        btnRegister.setOnClickListener(v -> {
            String u = etRegUsername.getText().toString().trim();
            String e = etRegEmail.getText().toString().trim();
            String p = etRegPassword.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty() || e.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ tất cả các trường", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);
            btnRegister.setText("Đang xử lý...");

            AuthController.register(u, p, e, new AuthController.AuthCallback() {
                @Override
                public void onSuccess(int userId, String username) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Đăng Ký");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }
}
