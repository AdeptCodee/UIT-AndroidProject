import express from "express";
import dotenv from "dotenv";
import { connectDB } from "./libs/db.js";
import authRoutes from "./routes/authRoutes.js";
import cookieParser from "cookie-parser";
import userRoutes from "./routes/userRoutes.js";
import { protectedRoute } from "./middlewares/authMiddleware.js";
import cors from "cors";

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5001;

// Middleware
app.use(express.json());
app.use(cookieParser());
app.use(cors({origin: process.env.CLIENT_URL, credentials: true})); // Cấu hình CORS để cho phép frontend truy cập với cookie

// Public routes
app.use("/api/auth", authRoutes);

// Private routes
app.use(protectedRoute); // Middleware để bảo vệ các route sau
app.use("/api/users", userRoutes);


connectDB().then(() => {
  app.listen(PORT, () => {
    console.log(`Server đang chạy trên cổng ${PORT}`);
  });
});
