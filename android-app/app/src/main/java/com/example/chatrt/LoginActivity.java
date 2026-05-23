package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatrt.api.ApiClient;
import com.example.chatrt.api.ApiService;
import com.example.chatrt.api.TokenManager;
import com.example.chatrt.models.AuthResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextView tvUsernameError, tvPasswordError, tvGoToSignUp;
    private Button btnLogin;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        initViews();

        btnLogin.setOnClickListener(v -> performLogin());

        tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Xóa các lỗi cũ trước khi kiểm tra mới
        tvUsernameError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);

        boolean isValid = true;

        // Logic validation giống như trên giao diện Web của bạn
        if (username.length() < 3) {
            tvUsernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (password.length() < 6) {
            tvPasswordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (!isValid) return;

        // Chuẩn bị dữ liệu gửi đi
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        // Gọi API
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.signIn(credentials).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String accessToken = response.body().getAccessToken();

                    // Lưu token vào máy
                    tokenManager.saveAccessToken(accessToken);

                    // VÌ SERVER CHƯA TRẢ VỀ USER OBJECT NÊN CHÚNG TA KHÔNG GỌI .getUser().getId() Ở ĐÂY
                    // App sẽ không còn bị crash nữa

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
