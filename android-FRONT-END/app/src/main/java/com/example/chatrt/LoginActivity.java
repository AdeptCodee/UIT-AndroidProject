package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatrt.api.AuthService;
import com.example.chatrt.models.LoginResponse;
import com.example.chatrt.utils.TokenManager;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView txtGoToSignUp;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo TokenManager lần đầu
        TokenManager.init(this);

        // Ánh xạ: Kết nối biến Java với ID bên XML
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoToSignUp = findViewById(R.id.txtGoToSignUp);
        progressBar = findViewById(R.id.progressBar); // Thêm ProgressBar để hiển thị loading

        // Khởi tạo AuthService
        authService = AuthService.getInstance(this);

        // Lắng nghe sự kiện click nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    performLogin(username, password);
                }
            }
        });

        // Lắng nghe sự kiện click để chuyển sang trang Đăng ký
        txtGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Chuyển sang SignUpActivity khi hoàn thành
                Toast.makeText(LoginActivity.this, "Tính năng đăng ký sẽ sớm có", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm xử lý logic đăng nhập
    private void performLogin(String username, String password) {
        // Hiển thị ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Gọi API thông qua AuthService
        authService.login(username, password, new AuthService.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                // Đăng nhập thành công
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();

                // Chuyển sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng LoginActivity để không quay lại được
            }

            @Override
            public void onError(String errorMessage) {
                // Sai tài khoản/mật khẩu
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String failureMessage) {
                // Lỗi kết nối, mất mạng, etc
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Toast.makeText(LoginActivity.this, failureMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}