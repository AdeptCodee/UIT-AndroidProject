package com.example.chatrt.api;

import com.example.chatrt.models.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // Bạn hãy sửa lại "/api/auth/login" cho đúng với đường dẫn (Route) Đăng nhập ở Backend của bạn
    @POST("/api/auth/login")
    Call<Object> loginUser(@Body LoginRequest request);
}