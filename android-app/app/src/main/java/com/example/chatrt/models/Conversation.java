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
        @SerializedName("_id")
        private String id; // Đây là ID người dùng

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        @SerializedName("joinedAt")
        private String joinedAt;

        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getAvatarUrl() { return avatarUrl; }
    }

    public static class GroupInfo {
        @SerializedName("name")
        private String name;

        @SerializedName("createdBy")
        private String createdBy;

        public String getName() { return name; }
    }

    public static class LastMessage {
        @SerializedName("_id")
        private String id;

        @SerializedName("content")
        private String content;

        // THAY ĐỔI: senderId bây giờ là một Object User thu nhỏ, không phải String
        @SerializedName("senderId")
        private SenderInfo sender;

        @SerializedName("createdAt")
        private String createdAt;

        public String getContent() { return content; }

        public static class SenderInfo {
            @SerializedName("_id")
            private String id;
            @SerializedName("displayName")
            private String displayName;
            @SerializedName("avatarUrl")
            private String avatarUrl;

            public String getDisplayName() { return displayName; }
        }
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