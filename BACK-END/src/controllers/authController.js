import bcrypt from "bcrypt";
import USERS from "../models/USERS.js";

export const signUp = async (req, res) => {
  try {
    const { username, password, email, firstName, lastName } = req.body;
    if (!username || !password || !email || !firstName || !lastName) {
      return res
        .status(400)
        .json({ message: "Vui lòng điền đầy đủ thông tin!" });
    }

    // Kiểm tra user có tồn tại chưa?
    const duplicateUser = await USERS.findOne({ username });
    if (duplicateUser) {
      return res.status(409).json({ message: "Tên người dùng đã tồn tại!" });
    }
    // Mã hóa pass
    const hashedPassword = await bcrypt.hash(password, 10);

    // Tạo user mới
    await USERS.create({
      username,
      hashedPassword,
      email,
      displayName: `${firstName} ${lastName}`,
    });
    // Returrn
    return res.status(204).json({ message: "Đăng ký thành công!" });
  } catch (error) {
    console.error("Đã xảy ra lỗi khi gọi hàm đăng ký người dùng.", error);
    return res.status(500).json({ message: "Đã xảy ra lỗi máy chủ!" });
  }
};
