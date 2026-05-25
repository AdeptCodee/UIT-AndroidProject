package com.example.chatrt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatrt.models.Reminder;
import com.example.chatrt.models.User;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminder> reminderList;
    private String myId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ReminderAdapter(List<Reminder> reminderList, String myId) {
        this.reminderList = reminderList;
        this.myId = myId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    // ReminderAdapter.java
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        User creator = reminder.getCreatorUser();
        User partner = reminder.getPartnerUser();

        // Hiển thị tên thật (displayName), không dùng chữ "Bạn" để tránh nhầm lẫn
        String creatorName = (creator != null && creator.getDisplayName() != null)
                ? creator.getDisplayName() : "Người dùng";
        String partnerName = (partner != null && partner.getDisplayName() != null)
                ? partner.getDisplayName() : "đối phương";

        // Cấu trúc: [Tên người gửi] đã nhắc [Tên người nhận]
        holder.tvTitle.setText(creatorName + " đã nhắc " + partnerName);
        holder.tvContent.setText(reminder.getContent());
        holder.tvDate.setText("Vào ngày: " + dateFormat.format(reminder.getDueDate()));
    }

    @Override
    public int getItemCount() { return reminderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvContent = itemView.findViewById(R.id.tvReminderContent);
            tvDate = itemView.findViewById(R.id.tvReminderDate);
        }
    }
}
