package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.chatrt.api.ApiClient;
import com.example.chatrt.api.ApiService;
import com.example.chatrt.api.TokenManager;
import com.example.chatrt.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        // 1. Kiểm tra Token xem đã đăng nhập chưa
        if (tokenManager.getAccessToken() == null) {
            goToLogin();
            return;
        }

        // 2. Nếu đã đăng nhập -> Hiển thị giao diện chính
        setContentView(R.layout.activity_main);

        // 3. Lấy thông tin cá nhân của mình để lưu ID (Rất quan trọng cho Chat)
        fetchMyInfo();

        // 4. Cấu hình thanh điều hướng dưới cùng
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mặc định khi mở App sẽ vào tab Chat (số 3)
        bottomNav.setSelectedItemId(R.id.nav_chat);
        loadFragment(new ChatFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (id == R.id.nav_info) {
                selectedFragment = new InfoFragment();
            } else if (id == R.id.nav_chat) {
                selectedFragment = new ChatFragment();
            } else if (id == R.id.nav_placeholder) {
                selectedFragment = new PlaceholderFragment();
            } else if (id == R.id.nav_logout) {
                performLogout();
                return true;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void fetchMyInfo() {
        // Sử dụng ApiClient và ApiService CHÍNH XÁC của dự án chúng ta
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.fetchMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu ID của mình vào TokenManager để các màn hình khác dùng để lọc "người kia"
                    tokenManager.saveUserId(response.body().getId());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Nếu lỗi thì thôi, sẽ thử lại sau
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void performLogout() {
        tokenManager.clear();
        goToLogin();
    }

    private void goToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}