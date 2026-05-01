export const authMe = async (req, res) => {
  try {
    const user = req.user; // Lấy thông tin user đã được xác thực từ authMiddleware
    return res.status(200).json({ user });
  } catch (error) {
    console.error("Đã xảy ra lỗi khi gọi hàm authMe.", error);
    return res.status(500).json({ message: "Đã xảy ra lỗi máy chủ!" });
  }
};

export const test = async () => {
  return res.sendStatus(204);
}
