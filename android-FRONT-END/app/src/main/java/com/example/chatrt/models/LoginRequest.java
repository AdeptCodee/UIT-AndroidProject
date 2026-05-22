package com.example.chatrt.models;

public class LoginRequest {
    // Tên biến phải khớp 100% với tên Backend của bạn đang chờ nhận
    private String username;
    private String password;

    // Hàm khởi tạo (Constructor) để đóng gói dữ liệu
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}