import Reminder from "../models/Reminder.js";

export const createReminder = async (req, res) => {
  try {
    const { conversationId, partnerId, content, dueDate } = req.body;
    const reminder = await Reminder.create({
      conversationId,
      creatorId: req.user._id,
      partnerId,
      content,
      dueDate: new Date(dueDate),
    });
    res.status(201).json(reminder);
  } catch (error) {
    res.status(500).json({ message: "Lỗi tạo nhắc hẹn" });
  }
};

export const getMyReminders = async (req, res) => {
  try {
    const now = new Date();
    // Tự động xóa các nhắc hẹn đã qua ngày (Dọn dẹp database)
    await Reminder.deleteMany({ dueDate: { $lt: now } });

    // Lấy danh sách nhắc hẹn liên quan đến user hiện tại
    const reminders = await Reminder.find({
      $or: [{ creatorId: req.user._id }, { partnerId: req.user._id }],
      dueDate: { $gte: now },
    }).populate("creatorId partnerId", "username avatar");

    res.status(200).json(reminders);
  } catch (error) {
    res.status(500).json({ message: "Lỗi lấy danh sách nhắc hẹn" });
  }
};
