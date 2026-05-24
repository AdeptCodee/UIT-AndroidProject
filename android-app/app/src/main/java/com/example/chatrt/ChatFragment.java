package com.example.chatrt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatrt.api.*;
import com.example.chatrt.models.*;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {
    private RecyclerView rvGroupChats, rvDirectChats;
    private ConversationAdapter groupAdapter, directAdapter;
    private List<Conversation> groupList = new ArrayList<>();
    private List<Conversation> directList = new ArrayList<>();

    private SocketManager.OnOnlineUsersChangedListener onlineListener;
    private SocketManager.ConversationUpdateListener convoListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        rvGroupChats = view.findViewById(R.id.rvGroupChats);
        rvDirectChats = view.findViewById(R.id.rvDirectChats);

        setupRecyclerViews();

        view.findViewById(R.id.btnNewMessage).setOnClickListener(v ->
                startActivity(new Intent(getContext(), SelectFriendActivity.class)));

        // THIẾT LẬP SOCKET
        SocketManager sm = SocketManager.getInstance(getContext());
        sm.connect(); // QUAN TRỌNG: Phải gọi connect để nhận dữ liệu real-time

        // 1. Lắng nghe trạng thái Online (Chấm xanh)
        onlineListener = onlineIds -> {
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                if (directAdapter != null) directAdapter.notifyDataSetChanged();
                if (groupAdapter != null) groupAdapter.notifyDataSetChanged();
            });
        };
        sm.addOnlineUsersListener(onlineListener);

        // 2. LẮNG NGHE TIN NHẮN MỚI / ĐÃ ĐỌC (Cập nhật số Unread)
        convoListener = data -> {
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                try {
                    JSONObject convoJson = data.has("conversation") ? data.getJSONObject("conversation") : data;
                    Conversation updatedConvo = new Gson().fromJson(convoJson.toString(), Conversation.class);

                    Log.d("ChatFragment", "Nhận cập nhật cho hội thoại: " + updatedConvo.getId());
                    updateListWithNewConvo(updatedConvo);
                } catch (Exception e) {
                    Log.e("ChatFragment", "Lỗi xử lý Socket update: " + e.getMessage());
                }
            });
        };
        sm.addConvoListener(convoListener);

        return view;
    }

    // TỰ ĐỘNG TẢI LẠI KHI QUAY VỀ TỪ MÀN HÌNH CHAT (Để đồng bộ Seen)
    @Override
    public void onResume() {
        super.onResume();
        fetchConversations();
    }

    private void updateListWithNewConvo(Conversation updatedConvo) {
        // 1. Xác định đây là chat nhóm hay chat cá nhân
        List<Conversation> targetList = "group".equals(updatedConvo.getType()) ? groupList : directList;
        ConversationAdapter targetAdapter = "group".equals(updatedConvo.getType()) ? groupAdapter : directAdapter;

        Conversation oldConvo = null;
        int oldIndex = -1;

        // 2. Tìm cuộc hội thoại cũ trong danh sách hiện tại
        for (int i = 0; i < targetList.size(); i++) {
            if (targetList.get(i).getId().equals(updatedConvo.getId())) {
                oldConvo = targetList.get(i);
                oldIndex = i;
                break;
            }
        }

        if (oldConvo != null) {
            // 3A. NẾU TÌM THẤY: Chỉ cập nhật các trường bị thay đổi (giữ nguyên Tên, Ảnh)
            oldConvo.setLastMessage(updatedConvo.getLastMessage());
            oldConvo.setUnreadCounts(updatedConvo.getUnreadCounts());

            // Xóa ở vị trí cũ và đưa lên vị trí đầu tiên (Top 1)
            targetList.remove(oldIndex);
            targetList.add(0, oldConvo);
        } else {
            // 3B. NẾU KHÔNG TÌM THẤY: Đây là người lạ nhắn tin lần đầu, thêm thẳng vào đầu danh sách
            targetList.add(0, updatedConvo);
        }

        // 4. Báo cho Adapter biết dữ liệu đã thay đổi để vẽ lại giao diện
        if (targetAdapter != null) {
            targetAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getContext() != null) {
            SocketManager sm = SocketManager.getInstance(getContext());
            sm.removeOnlineUsersListener(onlineListener);
            sm.removeConvoListener(convoListener);
        }
    }

    private void setupRecyclerViews() {
        rvGroupChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDirectChats.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new ConversationAdapter(groupList, this::openChat);
        directAdapter = new ConversationAdapter(directList, this::openChat);
        rvGroupChats.setAdapter(groupAdapter);
        rvDirectChats.setAdapter(directAdapter);
    }

    private void fetchConversations() {
        if (getContext() == null) return;
        ApiClient.getClient(getContext()).create(ApiService.class).getConversations()
                .enqueue(new Callback<ConversationsResponse>() {
                    @Override
                    public void onResponse(Call<ConversationsResponse> call, Response<ConversationsResponse> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null) {
                            groupList.clear(); directList.clear();
                            for (Conversation c : response.body().getConversations()) {
                                if ("group".equals(c.getType())) groupList.add(c);
                                else directList.add(c);
                            }
                            if (groupAdapter != null) groupAdapter.notifyDataSetChanged();
                            if (directAdapter != null) directAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(Call<ConversationsResponse> call, Throwable t) {}
                });
    }

    private void openChat(Conversation convo) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", convo.getId());
        String name = ""; String url = null;
        if ("group".equals(convo.getType())) {
            name = convo.getGroup() != null ? convo.getGroup().getName() : "Nhóm";
        } else {
            String myId = new TokenManager(getContext()).getUserId();
            if (convo.getParticipants() != null) {
                for (Conversation.Participant p : convo.getParticipants()) {
                    if (!p.getId().equals(myId)) { name = p.getDisplayName(); url = p.getAvatarUrl(); break; }
                }
            }
        }
        intent.putExtra("CHAT_NAME", name);
        intent.putExtra("AVATAR_URL", url);
        startActivity(intent);
    }
}