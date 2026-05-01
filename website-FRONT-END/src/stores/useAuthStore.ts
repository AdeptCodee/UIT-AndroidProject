import { create } from "zustand";
import { toast } from "sonner";
import { authService } from "@/services/authService";
import type { AuthState } from "@/types/store";

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: null,
  user: null,
  loading: false,

  clearState: () => {
    set({ accessToken: null, user: null, loading: false });
  },

  signUp: async (username, password, email, firstName, lastName) => {
    try {
      set({ loading: true });

      // Gọi backend API để đăng ký
      await authService.signUp(username, password, email, firstName, lastName);
      toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
    } catch (error) {
      console.error(error);
      toast.error("Đăng ký thất bại. Vui lòng thử lại.");
    } finally {
      set({ loading: false });
    }
  },

  signIn: async (username, password) => {
    try {
      set({ loading: true });

      const { accessToken } = await authService.signIn(username, password);
      set({ accessToken });
      toast.success("Đăng nhập thành công! Chào mừng trở lại.");
    } catch (error) {
      console.error(error);
      toast.error("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
    } finally {
      set({ loading: false });
    }
  },

  signOut: async () => {
    try {
      get().clearState();
      
      // Gọi API để đăng xuất (xóa cookie trên server)
      await authService.signOut();

      toast.success("Đăng xuất thành công!");
    } catch (error) {
      console.error(error);
      toast.error("Đăng xuất thất bại. Vui lòng thử lại.");
    }
  },
}));
