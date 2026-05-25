package com.example.chatrt;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatrt.api.*;
import com.example.chatrt.models.*;
import com.example.chatrt.utils.ReminderParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private List<Conversation.Participant> currentParticipants = new ArrayList<>();
    private String conversationId, myId, conversationType;
    private EditText etInput;
    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        myId = new TokenManager(this).getUserId();
        socketManager = SocketManager.getInstance(this);

        etInput = findViewById(R.id.etMessageInput);
        rvMessages = findViewById(R.id.rvMessages);
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> sendMessage());

        setupRecyclerView();
        fetchConversationDetails();
        fetchMessages(null);
        setupSocket();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList, myId, currentParticipants);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) return;
        etInput.setText("");

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        String recipientId = "";
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(myId)) { recipientId = p.getId(); break; }
        }

        RequestBody rbConvoId = RequestBody.create(MediaType.parse("text/plain"), conversationId);
        RequestBody rbContent = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody rbRecipient = RequestBody.create(MediaType.parse("text/plain"), recipientId);

        api.sendDirectMessage(rbRecipient, rbContent, rbConvoId, null).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addMessageToList(response.body().getMessage());
                }
            }
            @Override public void onFailure(Call<SendMessageResponse> call, Throwable t) {}
        });
    }

    private void setupSocket() {
        socketManager.connect();
        socketManager.addMessageListener(message -> runOnUiThread(() -> {
            if (message.getConversationId().equals(conversationId)) addMessageToList(message);
        }));
    }

    private void addMessageToList(Message message) {
        for (Message m : messageList) { if (m.getId() != null && m.getId().equals(message.getId())) return; }
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        
        // Xử lý Reminder ngay lập tức
        processReminder(message);
    }

    private void processReminder(Message message) {
        String content = message.getContent();
        if (content == null || !content.trim().startsWith("/reminder")) return;

        ReminderParser.ReminderData data = ReminderParser.parse(content);
        if (data == null) return;

        // Xác định rõ ai là người gửi, ai là người nhận
        String senderId = message.getSenderId();
        if (senderId == null) senderId = myId; // Fallback cho tin nhắn vừa gửi

        String partnerId = null;
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(senderId)) {
                partnerId = p.getId();
                break;
            }
        }

        if (partnerId != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("conversationId", conversationId);
            body.put("creatorId", senderId);
            body.put("partnerId", partnerId);
            body.put("content", data.content);
            body.put("dueDate", data.dueDate);

            ApiService api = ApiClient.getClient(this).create(ApiService.class);
            api.createReminder(body).enqueue(new Callback<Reminder>() {
                @Override
                public void onResponse(Call<Reminder> call, Response<Reminder> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "🚀 Đã tạo nhắc hẹn!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Reminder", "Lỗi tạo: " + response.code());
                    }
                }
                @Override public void onFailure(Call<Reminder> call, Throwable t) {}
            });
        }
    }

    private void fetchConversationDetails() {
        ApiClient.getClient(this).create(ApiService.class).getConversations().enqueue(new Callback<ConversationsResponse>() {
            @Override
            public void onResponse(Call<ConversationsResponse> call, Response<ConversationsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Conversation c : response.body().getConversations()) {
                        if (c.getId().equals(conversationId)) {
                            currentParticipants.clear();
                            currentParticipants.addAll(c.getParticipants());
                            conversationType = c.getType();
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<ConversationsResponse> call, Throwable t) {}
        });
    }

    private void fetchMessages(String cursor) {
        ApiClient.getClient(this).create(ApiService.class).getMessages(conversationId, 20, cursor).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.addAll(0, response.body().getMessages());
                    adapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override public void onFailure(Call<MessagesResponse> call, Throwable t) {}
        });
    }
}
