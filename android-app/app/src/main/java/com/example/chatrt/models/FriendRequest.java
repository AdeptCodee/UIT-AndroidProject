package com.example.chatrt.models;

import com.google.gson.annotations.SerializedName;

/**
 * Class này mô phỏng lại FriendRequest.js.
 * Dùng để quản lý các lời mời kết bạn giữa những người dùng.
 */
public class FriendRequest {

    @SerializedName("_id")
    private String id;

    // ID của người gửi lời mời
    @SerializedName("from")
    private String from;

    // ID của người nhận lời mời
    @SerializedName("to")
    private String to;

    // Lời nhắn đi kèm khi kết bạn (ví dụ: "Chào bạn, mình là...")
    @SerializedName("message")
    private String message;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // --- Các hàm Getter và Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}