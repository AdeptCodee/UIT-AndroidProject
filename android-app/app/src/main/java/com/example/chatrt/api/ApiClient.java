package com.example.chatrt.api;

import android.content.Context;
import android.content.Intent;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.chatrt.models.AuthResponse;
import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5001/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 1. Interceptor gắn Token vào Header
            Interceptor authInterceptor = chain -> {
                String token = tokenManager.getAccessToken();
                Request.Builder builder = chain.request().newBuilder();
                if (token != null) {
                    builder.addHeader("Authorization", "Bearer " + token);
                }
                return chain.proceed(builder.build());
            };

            // 2. Interceptor xử lý Refresh Token (Giống axios.ts)
            Interceptor refreshInterceptor = chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);

                // Nếu Server trả về 403 (Forbidden) - thường là do Access Token hết hạn
                if (response.code() == 403 && !request.url().toString().contains("auth/refresh")) {
                    synchronized (ApiClient.class) { // Tránh việc gọi refresh nhiều lần cùng lúc
                        response.close(); // Đóng response cũ

                        // Gọi API Refresh (Dùng một Retrofit tạm thời để tránh lặp Interceptor)
                        Retrofit tempRetrofit = new Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        ApiService service = tempRetrofit.create(ApiService.class);
                        // Lưu ý: Backend của bạn có thể cần gửi Refresh Token trong Body hoặc Cookie
                        // Ở đây tôi giả định gọi refreshToken() đã định nghĩa trong ApiService
                        retrofit2.Response<AuthResponse> refreshRes = service.refreshToken().execute();

                        if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                            String newAccess = refreshRes.body().getAccessToken();
                            tokenManager.saveAccessToken(newAccess);

                            // Thực hiện lại yêu cầu cũ với Token mới
                            Request newRequest = request.newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                            return chain.proceed(newRequest);
                        } else {
                            // Refresh thất bại -> Token hết hạn hẳn -> Bắt đăng nhập lại
                            tokenManager.clear();
                            // Bạn có thể dùng EventBus hoặc Intent để quay về màn Login
                        }
                    }
                }
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(refreshInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
