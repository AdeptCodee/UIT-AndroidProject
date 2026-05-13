import Conversation from "../models/Conversation.js";
import Friend from "../models/Friend.js";

const pair = (a, b) => (a < b ? [a, b] : [b, a]);

export const checkFriendShip = async (req, res, next) => {
  try {
    const me = req.user._id.toString();
    const recipientId = req.body?.recipientId ?? null;

    if (recipientId) {
      const [userA, userB] = pair(me, recipientId);

      const isFriend = await Friend.findOne({ userA, userB });

      if (!isFriend) {
        return res.status(403).json({ message: "Chưa kết bạn với người này." });
      }
      return next();
    }

    //todo: chat nhóm
    if (!recipientId) {
      return res.status(400).json({ message: "Cần cung cấp recipientId" });
    }
  } catch (error) {
    console.error("Lỗi xảy ra khi checkFriendShip:", error);
    return res.status(500).json({ message: "Lỗi hệ thống" });
  }
};
