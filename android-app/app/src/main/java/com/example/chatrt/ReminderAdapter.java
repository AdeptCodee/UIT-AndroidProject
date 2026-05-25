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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        
        User partner = reminder.getPartnerId();
        // Nếu mình là partner thì hiển thị tên của creator
        if (partner != null && partner.getId().equals(myId)) {
            partner = reminder.getCreatorId();
        }

        String partnerName = (partner != null) ? partner.getDisplayName() : "Người dùng";
        holder.tvTitle.setText("Bạn cùng với " + partnerName);
        holder.tvContent.setText("Nội dung: " + reminder.getContent());
        holder.tvDate.setText("Ngày hẹn: " + dateFormat.format(reminder.getDueDate()));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

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
