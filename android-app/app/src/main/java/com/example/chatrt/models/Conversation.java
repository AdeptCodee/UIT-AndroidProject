package com.example.chatrt.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Class này mô phỏng lại Conversation.js.
 * Quản lý thông tin cuộc trò chuyện (Chat đơn hoặc Chat nhóm).
 */
public class Conversation {

    @SerializedName("_id")
    private String id;

    @SerializedName("type")
    private String type; // "direct" (cá nhân) hoặc "group" (nhóm)

    @SerializedName("participants")
    private List<Participant> participants;

    @SerializedName("group")
    private GroupInfo group;

    @SerializedName("lastMessage")
    private LastMessage lastMessage;

    @SerializedName("unreadCounts")
    private Map<String, Integer> unreadCounts; // ID người dùng -> Số tin chưa đọc

    @SerializedName("updatedAt")
    private String updatedAt;

    // --- Các Class phụ bên trong (Nested Classes) ---

    public static class Participant {
        @SerializedName("userId")
        private String userId;

        @SerializedName("joinedAt")
        private String joinedAt;

        public String getUserId() { return userId; }
    }

    public static class GroupInfo {
        @SerializedName("name")
        private String name;

        @SerializedName("createdBy")
        private String createdBy;

        public String getName() { return name; }
    }

    public static class LastMessage {
        @SerializedName("content")
        private String content;

        @SerializedName("senderId")
        private String senderId;

        @SerializedName("createdAt")
        private String createdAt;

        public String getContent() { return content; }
    }

    // --- Các hàm Getter và Setter chính ---

    public String getId() { return id; }
    public String getType() { return type; }
    public List<Participant> getParticipants() { return participants; }
    public GroupInfo getGroup() { return group; }
    public LastMessage getLastMessage() { return lastMessage; }
    public Map<String, Integer> getUnreadCounts() { return unreadCounts; }
    public String getUpdatedAt() { return updatedAt; }
}