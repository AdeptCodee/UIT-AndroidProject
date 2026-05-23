package com.example.chatrt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatrt.api.ApiClient;
import com.example.chatrt.api.ApiService;
import com.example.chatrt.models.Conversation;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView rvGroupChats, rvDirectChats;
    private ConversationAdapter groupAdapter, directAdapter;
    private List<Conversation> groupList = new ArrayList<>();
    private List<Conversation> directList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvGroupChats = view.findViewById(R.id.rvGroupChats);
        rvDirectChats = view.findViewById(R.id.rvDirectChats);

        setupRecyclerViews();
        fetchConversations();

        // Xử lý nút Gửi tin nhắn mới
        view.findViewById(R.id.btnNewMessage).setOnClickListener(v -> {
            // Sẽ mở màn hình chọn bạn bè ở bước sau
            Toast.makeText(getContext(), "Đang mở danh sách bạn bè...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupRecyclerViews() {
        rvGroupChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDirectChats.setLayoutManager(new LinearLayoutManager(getContext()));

        groupAdapter = new ConversationAdapter(groupList, convo -> openChat(convo));
        directAdapter = new ConversationAdapter(directList, convo -> openChat(convo));

        rvGroupChats.setAdapter(groupAdapter);
        rvDirectChats.setAdapter(directAdapter);
    }

    private void fetchConversations() {
        ApiService apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        apiService.getConversations().enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupList.clear();
                    directList.clear();

                    for (Conversation c : response.body()) {
                        if (c.getType().equals("group")) groupList.add(c);
                        else directList.add(c);
                    }

                    groupAdapter.notifyDataSetChanged();
                    directAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải hội thoại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(Conversation conversation) {
        // Sau này sẽ mở ChatActivity tại đây
        Toast.makeText(getContext(), "Mở hội thoại: " + conversation.getId(), Toast.LENGTH_SHORT).show();
    }
}