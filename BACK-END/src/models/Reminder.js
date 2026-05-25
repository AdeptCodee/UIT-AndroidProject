import mongoose from "mongoose";

const reminderSchema = new mongoose.Schema({
  conversationId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "Conversation",
    required: true,
  },
  creatorId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "USERS",
    required: true,
  },
  partnerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "USERS",
    required: true,
  }, // Người cùng hội thoại
  content: { type: String, required: true },
  dueDate: { type: Date, required: true },
  createdAt: { type: Date, default: Date.now },
});

const Reminder = mongoose.model("Reminder", reminderSchema);
export default Reminder;
