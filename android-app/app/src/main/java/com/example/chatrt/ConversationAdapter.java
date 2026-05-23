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

        // Tạm thời hiển thị tên ID nếu là chat nhóm, hoặc xử lý lấy tên người kia nếu chat đơn
        if (convo.getType().equals("group")) {
            holder.tvName.setText(convo.getGroup() != null ? convo.getGroup().getName() : "Nhóm không tên");
        } else {
            holder.tvName.setText("Cuộc hội thoại cá nhân"); // Sẽ xử lý lấy tên bạn bè ở bước sau
        }

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