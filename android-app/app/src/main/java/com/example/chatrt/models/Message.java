package com.example.chatrt.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("_id")
    private String id;

    @SerializedName("conversationId")
    private String conversationId;

    // QUAN TRỌNG: Ở danh sách tin nhắn, Server trả về String ID
    @SerializedName("senderId")
    private String senderId;

    @SerializedName("content")
    private String content;

    @SerializedName("imgUrl")
    private String imgUrl;

    @SerializedName("createdAt")
    private String createdAt;

    // --- Getters và Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}