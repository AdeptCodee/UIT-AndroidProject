package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.chatrt.api.ApiClient;
import com.example.chatrt.api.ApiService;
import com.example.chatrt.api.SocketManager;
import com.example.chatrt.api.TokenManager;
import com.example.chatrt.models.SearchResponse;
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

        // 1. Kiểm tra xem đã có Token chưa
        if (tokenManager.getAccessToken() == null) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // 2. Lấy thông tin của tôi để lưu ID vào máy (dùng cho logic chat trái/phải)
        fetchMyInfo();
        
        // Kích hoạt kết nối Socket
        SocketManager.getInstance(this).connect();

        // 3. Thiết lập thanh điều hướng dưới cùng
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mặc định mở Tab Chat
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
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        // Gọi API fetchMe để lấy ID của chính mình
        apiService.fetchMe().enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    User me = response.body().getUser();

                    // Cất ID vào TokenManager để MessageAdapter có thể lấy ra so sánh
                    tokenManager.saveUserId(me.getId());
                    Log.d("MainActivity", "Đã lưu ID người dùng: " + me.getId());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e("MainActivity", "Lỗi fetchMe: " + t.getMessage());
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
        // NGẮT KẾT NỐI SOCKET NGAY LẬP TỨC
        com.example.chatrt.api.SocketManager.getInstance(this).disconnect();
        tokenManager.clear();
        goToLogin();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
