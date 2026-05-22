package com.example.chatrt.models;

public class LoginResponse {
    // Tên biến này phải giống 100% với tên trường mà Backend trả về
    private String message;
    private String accessToken;

    // Hàm lấy dữ liệu ra
    public String getAccessToken() {
        return accessToken;
    }

    public String getMessage() {
        return message;
    }
}