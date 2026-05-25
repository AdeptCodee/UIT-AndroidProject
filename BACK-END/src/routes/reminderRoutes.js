import express from "express";
import { getMyReminders } from "../controllers/reminderController.js";
// Lưu ý: Đổi tên import middleware bảo vệ API cho đúng với tên file authMiddleware của bạn
import { protectedRoute } from "../middlewares/authMiddleware.js";

const router = express.Router();

// Chỉ cần 1 đường dẫn GET để lấy danh sách hẹn, tự động kiểm tra token người dùng
router.get("/", protectedRoute, getMyReminders);

export default router;
