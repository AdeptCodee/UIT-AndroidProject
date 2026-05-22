package com.example.chatrt.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // QUAN TRỌNG: Máy ảo Android không hiểu chữ "localhost".
    // Bạn BẮT BUỘC phải dùng địa chỉ "10.0.2.2" để nó hiểu là đang trỏ về máy tính của bạn.
    // Nếu Backend của bạn chạy cổng 5001, hãy để nguyên dòng dưới.
    private static final String BASE_URL = "http://10.0.2.2:5001/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // Công cụ giúp dịch tự động từ Java sang JSON
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}