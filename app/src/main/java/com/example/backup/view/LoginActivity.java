package com.example.backup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.backup.MainActivity;
import com.example.backup.R;
import com.example.backup.controller.AuthController;
import com.example.backup.service.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignIn";

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleSignIn;
    private TextView tvGoToRegister;
    private SessionManager session;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> registerForActivityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.sign_in_button);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        checkRegisteredUsername(getIntent());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
            btnGoogleSignIn.setColorScheme(SignInButton.COLOR_DARK);
        }

        registerForActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        Log.d(TAG, "Google Sign-In canceled or failed resultCode=" + result.getResultCode());
                    }
                }
        );

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                registerForActivityResult.launch(signInIntent);
            });
        }

        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString().trim();
            String p = etPassword.getText().toString().trim();

            if (u.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên đăng nhập hoặc email!");
                etUsername.requestFocus();
                return;
            }
            if (p.isEmpty()) {
                etPassword.setError("Vui lòng nhập mật khẩu!");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");

            AuthController.login(u, p, new AuthController.AuthCallback() {
                @Override
                public void onSuccess(int userId, String username) {
                    session.setLogin(true, userId, username);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Xin chào " + username, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("ĐĂNG NHẬP");
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkRegisteredUsername(intent);
    }

    private void checkRegisteredUsername(Intent intent) {
        if (intent != null && intent.hasExtra("REGISTERED_USERNAME")) {
            String registeredUser = intent.getStringExtra("REGISTERED_USERNAME");
            if (registeredUser != null && !registeredUser.trim().isEmpty() && etUsername != null) {
                etUsername.setText(registeredUser.trim());
                if (etPassword != null) {
                    etPassword.requestFocus();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Toast.makeText(this, "Đã đăng nhập tài khoản " + account.getEmail(), Toast.LENGTH_SHORT).show();
            handleGoogleAccount(account);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                handleGoogleAccount(account);
            }
        } catch (ApiException e) {
            int code = e.getStatusCode();
            Log.w(TAG, "signInResult:failed code=" + code);
            String message = "Đăng nhập Google thất bại (Mã lỗi: " + code + ").";
            if (code == 10 || code == 12500) {
                message += "\n(Cần cấu hình SHA-1 & Package Name trên Google Cloud Console theo Buổi 10)";
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void handleGoogleAccount(GoogleSignInAccount account) {
        String email = account.getEmail();
        String displayName = account.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = (email != null && email.contains("@")) ? email.substring(0, email.indexOf("@")) : "Google User";
        }

        final String finalName = displayName;
        AuthController.loginOrRegisterGoogleUser(email, displayName, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(int userId, String username) {
                session.setLogin(true, userId, username);
                Toast.makeText(LoginActivity.this, "Chào mừng " + username + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {

                session.setLogin(true, 9999, finalName);
                Toast.makeText(LoginActivity.this, "Chào mừng " + finalName + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
