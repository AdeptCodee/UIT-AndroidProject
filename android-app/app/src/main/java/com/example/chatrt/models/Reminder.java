package com.example.chatrt.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Reminder implements Serializable {
    @SerializedName("_id")
    private String id;
    private String conversationId;
    private User creatorId;
    private User partnerId;
    private String content;
    private Date dueDate;
    private Date createdAt;

    public String getId() { return id; }
    public String getConversationId() { return conversationId; }
    public User getCreatorId() { return creatorId; }
    public User getPartnerId() { return partnerId; }
    public String getContent() { return content; }
    public Date getDueDate() { return dueDate; }
    public Date getCreatedAt() { return createdAt; }
}
