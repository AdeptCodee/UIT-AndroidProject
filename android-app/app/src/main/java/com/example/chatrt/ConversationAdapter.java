package com.example.chatrt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chatrt.models.Conversation;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversations;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnItemClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation convo = conversations.get(position);

        // LẤY MY_ID TỪ TOKEN_MANAGER ĐỂ BIẾT AI LÀ "NGƯỜI KIA"
        com.example.chatrt.api.TokenManager tokenManager = new com.example.chatrt.api.TokenManager(holder.itemView.getContext());
        String myId = tokenManager.getUserId();

        if (convo.getType().equals("group")) {
            holder.tvName.setText(convo.getGroup() != null ? convo.getGroup().getName() : "Nhóm không tên");
        } else {
            // Tìm người kia trong danh sách participants
            if (convo.getParticipants() != null) {
                for (Conversation.Participant p : convo.getParticipants()) {
                    // Nếu ID của participant khác với ID của mình, thì đó là người mình đang chat cùng
                    if (!p.getId().equals(myId)) {
                        holder.tvName.setText(p.getDisplayName());

                        // Load ảnh đại diện
                        if (p.getAvatarUrl() != null) {
                            Glide.with(holder.itemView.getContext())
                                    .load(p.getAvatarUrl())
                                    .placeholder(R.drawable.edit_text_bg) // Ảnh tạm trong lúc chờ tải
                                    .into(holder.ivAvatar);
                        }
                        break;
                    }
                }
            }
        }

        // Hiển thị tin nhắn cuối cùng
        if (convo.getLastMessage() != null) {
            holder.tvLastMsg.setText(convo.getLastMessage().getContent());
        } else {
            holder.tvLastMsg.setText("Chưa có tin nhắn");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(convo));
    }

    @Override
    public int getItemCount() { return conversations.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvName, tvLastMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivConvoAvatar);
            tvName = itemView.findViewById(R.id.tvConvoName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}