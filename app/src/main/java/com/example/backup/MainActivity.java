package com.example.backup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.backup.fragment.AiChatFragment;
import com.example.backup.fragment.FavoritesFragment;
import com.example.backup.fragment.HomeFragment;
import com.example.backup.fragment.ProfileFragment;
import com.example.backup.fragment.SearchFragment;
import com.example.backup.service.SessionManager;
import com.example.backup.view.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    private final Fragment homeFragment      = new HomeFragment();
    private final Fragment searchFragment    = new SearchFragment();
    private final Fragment aiChatFragment    = new AiChatFragment();
    private final Fragment favoritesFragment = new FavoritesFragment();
    private final Fragment profileFragment   = new ProfileFragment();

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra trạng thái phiên đăng nhập
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);

        // Tối ưu Padding System Bars (Status Bar / Display Cutout / Navigation Bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(insets.left, insets.top, insets.right, 0);
            bottomNav.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        // 2. Khởi tạo Fragment Manager: Add Fragment đầu tiên
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                    .add(R.id.fragmentContainer, favoritesFragment, "favorites").hide(favoritesFragment)
                    .add(R.id.fragmentContainer, aiChatFragment, "ai").hide(aiChatFragment)
                    .add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment)
                    .add(R.id.fragmentContainer, homeFragment, "home")
                    .commit();
            activeFragment = homeFragment;
        }

        // 3. Xử lý chuyển đổi Tab siêu mượt (Show/Hide giữ nguyên State & cuộn trang)
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_search) {
                switchFragment(searchFragment);
                return true;
            } else if (id == R.id.nav_ai) {
                switchFragment(aiChatFragment);
                return true;
            } else if (id == R.id.nav_favorites) {
                switchFragment(favoritesFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(@NonNull Fragment target) {
        if (activeFragment == target) return;
        fragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }

    /**
     * Phương thức cho phép các Fragment hoặc sự kiện bên trong chuyển tab nhanh
     */
    public void selectTab(int menuId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuId);
        }
    }

    public void switchToHome() {
        selectTab(R.id.nav_home);
    }

    public void switchToSearch() {
        selectTab(R.id.nav_search);
    }

    public void switchToAi() {
        selectTab(R.id.nav_ai);
    }

    public void switchToFavorites() {
        selectTab(R.id.nav_favorites);
    }

    public void switchToProfile() {
        selectTab(R.id.nav_profile);
    }
}
