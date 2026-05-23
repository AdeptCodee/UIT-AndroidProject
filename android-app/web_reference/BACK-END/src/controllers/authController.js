import bcrypt from "bcrypt";
import USERS from "../models/USERS.js";
import Session from "../models/Session.js";
import jwt from "jsonwebtoken";
import crypto from "crypto";

const ACCESS_TOKEN_TTL = "30m";
const REFRESH_TOKEN_TTL = 14 * 24 * 60 * 60 * 1000;

export const signUp = async (req, res) => {
  try {
    const { username, password, email, firstName, lastName } = req.body;
    if (!username || !password || !email || !firstName || !lastName) {
      return res.status(400).json({ message: "Vui lòng điền đầy đủ thông tin!" });
    }

    const duplicateUser = await USERS.findOne({ username });
    if (duplicateUser) {
      return res.status(409).json({ message: "Tên người dùng đã tồn tại!" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const newUser = await USERS.create({
      username,
      hashedPassword,
      email,
      displayName: `${lastName} ${firstName}`,
    });

    // Trả về thông tin user vừa tạo (Mobile cần để tự động đăng nhập hoặc hiển thị)
    return res.status(201).json({
        message: "Đăng ký thành công!",
        user: {
            _id: newUser._id,
            username: newUser.username,
            displayName: newUser.displayName,
            email: newUser.email
        }
    });
  } catch (error) {
    console.error("Lỗi đăng ký:", error);
    return res.status(500).json({ message: "Đã xảy ra lỗi máy chủ!" });
  }
};

export const signIn = async (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password) {
      return res.status(400).json({ message: "Vui lòng nhập username và password!" });
    }

    const user = await USERS.findOne({ username });
    if (!user) {
      return res.status(401).json({ message: "Tên người dùng hoặc mật khẩu không đúng!" });
    }

    const isMatch = await bcrypt.compare(password, user.hashedPassword);
    if (!isMatch) {
      return res.status(401).json({ message: "Tên người dùng hoặc mật khẩu không đúng!" });
    }

    const accessToken = jwt.sign(
      { userId: user._id },
      process.env.ACCESS_TOKEN_SECRET,
      { expiresIn: ACCESS_TOKEN_TTL },
    );

    const refreshToken = crypto.randomBytes(64).toString("hex");

    await Session.create({
      userId: user._id,
      refreshToken,
      expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL),
    });

    res.cookie("refreshToken", refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: "none",
      maxAge: REFRESH_TOKEN_TTL,
    });

    // CẬP NHẬT: Trả thêm thông tin user để Mobile dùng luôn
    return res.status(200).json({
      message: `Chào mừng ${user.displayName} đã đăng nhập thành công!!`,
      accessToken,
      user: {
          _id: user._id,
          username: user.username,
          displayName: user.displayName,
          email: user.email,
          avatarUrl: user.avatarUrl
      }
    });
  } catch (error) {
    console.error("Lỗi đăng nhập:", error);
    return res.status(500).json({ message: "Đã xảy ra lỗi máy chủ!" });
  }
};

export const signOut = async (req, res) => {
  try {
    const token = req.cookies?.refreshToken;
    if (token) {
      await Session.deleteOne({ refreshToken: token });
      res.clearCookie("refreshToken");
    }
    return res.status(200).json({ message: "Đăng xuất thành công!" });
  } catch (error) {
    return res.status(500).json({ message: "Đã xảy ra lỗi máy chủ!" });
  }
};

export const refreshToken = async (req, res) => {
  try {
    const token = req.cookies?.refreshToken;
    if (!token) return res.status(401).json({ message: "Token không tồn tại." });

    const session = await Session.findOne({ refreshToken: token });
    if (!session || session.expiresAt < new Date()) {
      return res.status(403).json({ message: "Token không hợp lệ hoặc hết hạn" });
    }

    const accessToken = jwt.sign(
      { userId: session.userId },
      process.env.ACCESS_TOKEN_SECRET,
      { expiresIn: ACCESS_TOKEN_TTL },
    );

    return res.status(200).json({ accessToken });
  } catch (error) {
    return res.status(500).json({ message: "Lỗi hệ thống" });
  }
};
