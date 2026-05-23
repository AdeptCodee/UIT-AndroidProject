package com.example.chatrt.api;

import android.content.Context;
import android.util.Log;
import com.example.chatrt.models.Conversation;
import com.example.chatrt.models.Message;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Collections;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Class này thay thế cho useSocketStore.ts bên Web.
 * Quản lý kết nối Real-time để nhận tin nhắn ngay lập tức.
 */
public class SocketManager {
    private static final String TAG = "SocketManager";
    private static final String SOCKET_URL = "http://10.0.2.2:5001"; // Cổng 5001 của Backend

    private static SocketManager instance;
    private Socket mSocket;
    private final Gson gson;
    private final TokenManager tokenManager;

    private SocketManager(Context context) {
        gson = new Gson();
        tokenManager = new TokenManager(context);
        setupSocket();
    }

    public static synchronized SocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new SocketManager(context.getApplicationContext());
        }
        return instance;
    }

    private void setupSocket() {
        try {
            String token = tokenManager.getAccessToken();

            IO.Options opts = new IO.Options();
            opts.auth = Collections.singletonMap("token", token);
            opts.transports = new String[]{"websocket"};

            mSocket = IO.socket(SOCKET_URL, opts);

            // Lắng nghe sự kiện kết nối
            mSocket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "Đã kết nối Socket thành công!"));

            // 1. Lắng nghe danh sách người dùng online (online-users)
            mSocket.on("online-users", args -> {
                Log.d(TAG, "Danh sách online: " + args[0].toString());
                // TODO: Cập nhật UI danh sách online
            });

            // 2. Lắng nghe tin nhắn mới (new-message)
            mSocket.on("new-message", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    // Chuyển từ JSON của Socket sang Model Java bằng Gson
                    Message message = gson.fromJson(data.getJSONObject("message").toString(), Message.class);
                    Conversation conversation = gson.fromJson(data.getJSONObject("conversation").toString(), Conversation.class);

                    Log.d(TAG, "Có tin nhắn mới: " + message.getContent());
                    // TODO: Gửi tin nhắn này lên màn hình Chat
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi xử lý new-message: " + e.getMessage());
                }
            });

            // 3. Lắng nghe khi có nhóm chat mới (new-group)
            mSocket.on("new-group", args -> {
                try {
                    JSONObject convoJson = (JSONObject) args[0];
                    Conversation conversation = gson.fromJson(convoJson.toString(), Conversation.class);

                    // Tự động tham gia vào phòng chat mới này (giống emit('join-conversation') bên Web)
                    mSocket.emit("join-conversation", conversation.getId());
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi xử lý new-group: " + e.getMessage());
                }
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Lỗi cấu hình Socket: " + e.getMessage());
        }
    }

    public void connect() {
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }

    // Gửi sự kiện lên Server
    public void emit(String event, Object data) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(event, data);
        }
    }
}