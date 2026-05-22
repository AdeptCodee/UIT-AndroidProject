package com.example.chatrt.api;

import com.example.chatrt.models.LoginRequest;
import com.example.chatrt.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // Endpoint đăng nhập: Backend setup là /api/auth/signin
    @POST("/api/auth/signin")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
}