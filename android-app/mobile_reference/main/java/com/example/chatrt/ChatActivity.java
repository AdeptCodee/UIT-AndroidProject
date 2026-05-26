package com.example.chatrt;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatrt.api.*;
import com.example.chatrt.models.*;
import com.example.chatrt.utils.ReminderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private List<Conversation.Participant> currentParticipants = new ArrayList<>();

    private String conversationId, myId, chatName, avatarUrl, conversationType;
    private EditText etInput;
    private SocketManager socketManager;
    private Uri selectedImageUri = null;

    private SocketManager.OnOnlineUsersChangedListener onlineListener;
    private SocketManager.MessageListener messageListener;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Toast.makeText(this, "Đã chọn ảnh!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Khôi phục lấy dữ liệu từ Intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        chatName = getIntent().getStringExtra("CHAT_NAME");
        avatarUrl = getIntent().getStringExtra("AVATAR_URL");

        myId = new TokenManager(this).getUserId();
        socketManager = SocketManager.getInstance(this);

        initViews();
        setupRecyclerView();
        fetchConversationDetails();
        fetchMessages(null);
        setupSocket();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etInput = findViewById(R.id.etMessageInput);
        ImageView btnSend = findViewById(R.id.btnSendMessage);
        ImageView btnGallery = findViewById(R.id.btnSelectImage);
        TextView tvTitle = findViewById(R.id.tvChatTitle);

        tvTitle.setText(chatName);
        updateHeaderAvatar(chatName, avatarUrl);

        // KHÔI PHỤC NÚT BACK
        findViewById(R.id.btnChatBack).setOnClickListener(v -> finish());

        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void updateHeaderAvatar(String name, String url) {
        CircleImageView ivAvatar = findViewById(R.id.ivChatAvatar);
        TextView tvDefault = findViewById(R.id.tvChatDefaultAvatar);
        if (ivAvatar == null || tvDefault == null) return;

        if (url != null && !url.isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            tvDefault.setVisibility(View.GONE);
            Glide.with(this).load(url).placeholder(R.drawable.edit_text_bg).into(ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);
            tvDefault.setVisibility(View.VISIBLE);
            tvDefault.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?");
        }
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList, myId, currentParticipants);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void fetchConversationDetails() {
        ApiService service = ApiClient.getClient(this).create(ApiService.class);
        service.getConversations().enqueue(new Callback<ConversationsResponse>() {
            @Override
            public void onResponse(Call<ConversationsResponse> call, Response<ConversationsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Conversation c : response.body().getConversations()) {
                        if (c.getId().equals(conversationId)) {
                            conversationType = c.getType();
                            currentParticipants.clear();
                            currentParticipants.addAll(c.getParticipants());

                            if ("direct".equals(conversationType)) {
                                for (Conversation.Participant p : currentParticipants) {
                                    if (!p.getId().equals(myId)) {
                                        updateHeaderAvatar(p.getDisplayName(), p.getAvatarUrl());
                                        ((TextView)findViewById(R.id.tvChatTitle)).setText(p.getDisplayName());
                                        break;
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                            updateHeaderStatus();
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<ConversationsResponse> call, Throwable t) {}
        });
    }

    private void fetchMessages(String cursor) {
        ApiService service = ApiClient.getClient(this).create(ApiService.class);
        service.getMessages(conversationId, 20, cursor).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> newMessages = response.body().getMessages();
                    if (newMessages != null) {
                        messageList.addAll(0, newMessages);
                        adapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            }
            @Override public void onFailure(Call<MessagesResponse> call, Throwable t) {}
        });
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty() && selectedImageUri == null) return;
        etInput.setText("");

        RequestBody rbConvoId = RequestBody.create(MediaType.parse("text/plain"), conversationId);
        RequestBody rbContent = RequestBody.create(MediaType.parse("text/plain"), content);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        String recipientId = "";
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(myId)) { recipientId = p.getId(); break; }
        }

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
        updateHeaderStatus();
        onlineListener = onlineIds -> runOnUiThread(this::updateHeaderStatus);
        messageListener = message -> runOnUiThread(() -> {
            if (message.getConversationId().equals(conversationId)) {
                addMessageToList(message);
                markConversationAsSeen();
            }
        });
        socketManager.addOnlineUsersListener(onlineListener);
        socketManager.addMessageListener(messageListener);
    }

    private void addMessageToList(Message message) {
        for (Message m : messageList) { if (m.getId() != null && m.getId().equals(message.getId())) return; }
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        handleReminderLogic(message);
    }

    private void handleReminderLogic(Message message) {
        if (!"direct".equals(conversationType) || message.getContent() == null) return;
        if (!message.getContent().trim().startsWith("/reminder")) return;

        ReminderParser.ReminderData data = ReminderParser.parse(message.getContent());
        if (data == null) return;

        String senderId = message.getSenderId();
        if (senderId == null) senderId = myId;

        String partnerId = null;
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(senderId)) { partnerId = p.getId(); break; }
        }

        if (partnerId != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("conversationId", conversationId);
            body.put("messageId", message.getId());
            body.put("creatorId", senderId);
            body.put("partnerId", partnerId);
            body.put("content", data.content);
            body.put("dueDate", data.dueDate);

            ApiClient.getClient(this).create(ApiService.class).createReminder(body).enqueue(new Callback<Reminder>() {
                @Override public void onResponse(Call<Reminder> call, Response<Reminder> response) {
                    if (response.isSuccessful()) Toast.makeText(ChatActivity.this, "🚀 Đã tạo nhắc hẹn!", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<Reminder> call, Throwable t) {}
            });
        }
    }

    private void updateHeaderStatus() {
        View dot = findViewById(R.id.viewStatusDot);
        TextView tvStatus = findViewById(R.id.tvOnlineStatus);
        if (dot == null || tvStatus == null || currentParticipants.isEmpty()) return;

        String otherId = null;
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(myId)) { otherId = p.getId(); break; }
        }

        if (otherId != null) {
            boolean isOnline = socketManager.isUserOnline(otherId);
            dot.setVisibility(View.VISIBLE);
            dot.setBackgroundTintList(ColorStateList.valueOf(isOnline ? 0xFF10B981 : 0xFF9CA3AF));
            tvStatus.setText(isOnline ? "Online" : "Offline");
            tvStatus.setTextColor(isOnline ? 0xFF10B981 : 0xFF9CA3AF);
        }
    }

    private void markConversationAsSeen() {
        if (conversationId == null) return;
        ApiClient.getClient(this).create(ApiService.class).markAsSeen(conversationId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        markConversationAsSeen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.removeOnlineUsersListener(onlineListener);
            socketManager.removeMessageListener(messageListener);
        }
    }
}