package com.example.chatrt;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.chatrt.api.ApiClient;
import com.example.chatrt.api.ApiService;
import com.example.chatrt.models.LoginRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Khai báo các biến để giữ linh kiện
    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView txtGoToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ: Kết nối biến Java với ID bên XML
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoToSignUp = findViewById(R.id.txtGoToSignUp);

        // Lắng nghe sự kiện click nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    // 1. Đóng gói dữ liệu
                    LoginRequest request = new LoginRequest(username, password);

                    // 2. Chuẩn bị đường truyền API
                    ApiService apiService = ApiClient.getClient().create(ApiService.class);

                    // 3. Gửi đi (enqueue giúp gửi ngầm để không làm đơ giao diện)
                    apiService.loginUser(request).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            if (response.isSuccessful()) {
                                // Nếu Backend báo mã 200 OK
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                // TODO: Chuyển sang màn hình chính
                            } else {
                                // Nếu Backend báo lỗi 400, 401...
                                Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            // Lỗi do mất mạng, sập server...
                            Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}