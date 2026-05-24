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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private List<Conversation.Participant> currentParticipants = new ArrayList<>();

    private String conversationId, myId, chatName, avatarUrl, conversationType;
    private String currentCursor = null;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private EditText etInput;
    private SocketManager socketManager;
    private Uri selectedImageUri = null;

    private SocketManager.OnOnlineUsersChangedListener onlineListener;
    private SocketManager.MessageListener messageListener;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Toast.makeText(this, "Đã chọn ảnh, nhấn Gửi để tải lên!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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

        // Cập nhật Avatar ngay lập tức từ dữ liệu Intent truyền sang
        updateHeaderAvatar(chatName, avatarUrl);

        findViewById(R.id.btnChatBack).setOnClickListener(v -> finish());
        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSend.setOnClickListener(v -> sendMessage());
    }

    // Hàm phụ để xử lý Avatar Header (Hiện ảnh hoặc chữ cái đầu)
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
            tvDefault.setText(getFirstLetter(name));
        }
    }

    private String getFirstLetter(String name) {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList, myId, currentParticipants);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0 && layoutManager.findFirstVisibleItemPosition() == 0) {
                    if (hasMore && !isLoading && currentCursor != null) fetchMessages(currentCursor);
                }
            }
        });
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

                            // Cập nhật lại Avatar và Tên từ dữ liệu mới nhất của Server
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
        if (conversationId == null || !hasMore || isLoading) return;
        isLoading = true;
        ApiService service = ApiClient.getClient(this).create(ApiService.class);
        service.getMessages(conversationId, 20, cursor).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> newMessages = response.body().getMessages();
                    currentCursor = response.body().getNextCursor();
                    if (newMessages == null || newMessages.isEmpty()) { hasMore = false; return; }
                    if (cursor == null) {
                        messageList.clear(); messageList.addAll(newMessages);
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
                    } else {
                        messageList.addAll(0, newMessages);
                        adapter.notifyItemRangeInserted(0, newMessages.size());
                    }
                    if (currentCursor == null) hasMore = false;
                }
            }
            @Override public void onFailure(Call<MessagesResponse> call, Throwable t) { isLoading = false; }
        });
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty() && selectedImageUri == null) return;
        etInput.setText("");
        Uri currentUri = selectedImageUri; selectedImageUri = null;

        RequestBody rbConvoId = RequestBody.create(MediaType.parse("text/plain"), conversationId);
        RequestBody rbContent = RequestBody.create(MediaType.parse("text/plain"), content);
        MultipartBody.Part bodyImage = currentUri != null ? prepareFilePart("image", currentUri) : null;

        ApiClient.getClient(this).create(ApiService.class)
                .sendGroupMessage(rbConvoId, rbContent, bodyImage).enqueue(new Callback<SendMessageResponse>() {
                    @Override
                    public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Message sentMsg = response.body().getMessage();
                            if (sentMsg.getSenderId() == null) sentMsg.setSenderId(myId);
                            addMessageToList(sentMsg);
                        }
                    }
                    @Override public void onFailure(Call<SendMessageResponse> call, Throwable t) {}
                });
    }

    // ... các phần khác giữ nguyên ...

    // Tìm hàm onResume và cập nhật:
    @Override
    protected void onResume() {
        super.onResume();
        markConversationAsSeen();
    }

    private void markConversationAsSeen() {
        if (conversationId == null) return;
        ApiService service = ApiClient.getClient(this).create(ApiService.class);
        service.markAsSeen(conversationId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                // Server sẽ báo qua Socket cho ChatFragment xóa Badge
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void setupSocket() {
        socketManager.connect();
        updateHeaderStatus();

        onlineListener = onlineIds -> runOnUiThread(this::updateHeaderStatus);

        // Trong hàm setupSocket() của ChatActivity.java:
        messageListener = message -> runOnUiThread(() -> {
            if (message.getConversationId().equals(conversationId)) {
                addMessageToList(message);

                // NẾU ĐANG Ở TRONG MÀN HÌNH CHAT -> ĐÁNH DẤU ĐÃ ĐỌC NGAY
                markConversationAsSeen();
            }
        });

        socketManager.addOnlineUsersListener(onlineListener);
        socketManager.addMessageListener(messageListener);
    }

// ... các phần còn lại giữ nguyên ...


    // Trong ChatActivity.java, tìm và sửa hàm updateHeaderStatus
    private void updateHeaderStatus() {
        View dot = findViewById(R.id.viewStatusDot);
        TextView tvStatus = findViewById(R.id.tvOnlineStatus);

        // Đảm bảo myId không bị null
        if (myId == null) myId = new TokenManager(this).getUserId();

        if (dot == null || tvStatus == null || currentParticipants.isEmpty() || myId == null) return;

        if ("group".equals(conversationType)) {
            dot.setVisibility(View.GONE);
            tvStatus.setText(currentParticipants.size() + " thành viên");
            return;
        }

        String otherUserId = null;
        for (Conversation.Participant p : currentParticipants) {
            if (!p.getId().equals(myId)) {
                otherUserId = p.getId();
                break;
            }
        }

        if (otherUserId != null) {
            boolean isOnline = socketManager.isUserOnline(otherUserId);
            dot.setVisibility(View.VISIBLE);
            dot.setBackgroundTintList(ColorStateList.valueOf(isOnline ? 0xFF10B981 : 0xFF9CA3AF));
            tvStatus.setText(isOnline ? "Online" : "Offline");
            tvStatus.setTextColor(isOnline ? 0xFF10B981 : 0xFF9CA3AF);
        }
    }

    private void addMessageToList(Message message) {
        for (Message m : messageList) { if (m.getId() != null && m.getId().equals(message.getId())) return; }
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) return null;
            byte[] bytes = getBytes(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse(contentResolver.getType(fileUri)), bytes);
            return MultipartBody.Part.createFormData(partName, "image.jpg", requestFile);
        } catch (Exception e) { return null; }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
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