package com.example.chatrt.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TokenManager: Quản lý việc lưu/xóa accessToken trong SharedPreferences
 * Tương tự như localStorage ở website front-end
 */
public class TokenManager {
    private static final String PREF_NAME = "ChatRT_Prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String EMAIL_KEY = "email";
    private static final String AVATAR_URL_KEY = "avatar_url";

    private static SharedPreferences sharedPreferences;

    // Khởi tạo SharedPreferences (gọi 1 lần trong Application class hoặc MainActivity)
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // ===== Token Methods =====
    // Lưu accessToken
    public static void saveAccessToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, token);
        editor.apply();
    }

    // Lấy accessToken
    public static String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    // ===== User Info Methods =====
    // Lưu thông tin user
    public static void saveUserInfo(String userId, String username, String displayName, String email, String avatarUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID_KEY, userId);
        editor.putString(USERNAME_KEY, username);
        editor.putString(DISPLAY_NAME_KEY, displayName);
        editor.putString(EMAIL_KEY, email);
        editor.putString(AVATAR_URL_KEY, avatarUrl);
        editor.apply();
    }

    // Lấy user ID
    public static String getUserId() {
        return sharedPreferences.getString(USER_ID_KEY, null);
    }

    // Lấy username
    public static String getUsername() {
        return sharedPreferences.getString(USERNAME_KEY, null);
    }

    // Lấy displayName
    public static String getDisplayName() {
        return sharedPreferences.getString(DISPLAY_NAME_KEY, null);
    }

    // Lấy email
    public static String getEmail() {
        return sharedPreferences.getString(EMAIL_KEY, null);
    }

    // Lấy avatarUrl
    public static String getAvatarUrl() {
        return sharedPreferences.getString(AVATAR_URL_KEY, null);
    }

    // Kiểm tra người dùng đã đăng nhập hay chưa
    public static boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    // Xóa tất cả dữ liệu (dùng khi logout)
    public static void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
