package com.example.chatrt.api;

import android.content.Context;

import com.example.chatrt.models.LoginRequest;
import com.example.chatrt.models.LoginResponse;
import com.example.chatrt.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthService: Wrapper xung quanh ApiService
 * Tương tự như authService.ts ở website front-end
 * Giúp tách biệt logic gọi API từ UI
 */
public class AuthService {
    private static AuthService instance;
    private ApiService apiService;
    private Context context;

    // Constructor
    private AuthService(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Singleton pattern
    public static AuthService getInstance(Context context) {
        if (instance == null) {
            instance = new AuthService(context);
        }
        return instance;
    }

    // Callback interface để xử lý kết quả login
    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String errorMessage);
        void onFailure(String failureMessage);
    }

    // Đăng nhập
    public void login(String username, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(username, password);

        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    // Lưu accessToken vào SharedPreferences
                    if (loginResponse.getAccessToken() != null) {
                        TokenManager.saveAccessToken(loginResponse.getAccessToken());
                    }

                    callback.onSuccess(loginResponse);
                } else {
                    // Backend trả về lỗi (400, 401, etc)
                    String errorMessage = "Sai tài khoản hoặc mật khẩu";
                    try {
                        if (response.errorBody() != null) {
                            // Có thể parse errorBody nếu cần chi tiết hơn
                            errorMessage = response.message();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Lỗi do mất mạng, sập server, etc
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
