package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.chatrt.api.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        if (tokenManager.getAccessToken() == null) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

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
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}