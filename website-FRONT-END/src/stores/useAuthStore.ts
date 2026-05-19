import { create } from "zustand";
import { toast } from "sonner";
import { authService } from "@/services/authService";
import type { AuthState } from "@/types/store";
import { persist } from "zustand/middleware";
import { useChatStore } from "./useChatStore";

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      loading: false,

      setAccessToken: (accessToken) => {
        set({ accessToken });
      },

      clearState: () => {
        set({ accessToken: null, user: null, loading: false });
        localStorage.clear();
        useChatStore.getState().reset();
      },

      signUp: async (username, password, email, firstName, lastName) => {
        try {
          set({ loading: true });

          // Gọi backend API để đăng ký
          await authService.signUp(
            username,
            password,
            email,
            firstName,
            lastName,
          );
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

          localStorage.clear();
          useChatStore.getState().reset();

          const { accessToken } = await authService.signIn(username, password);
          get().setAccessToken(accessToken);
          await get().fetchMe(); // Lấy thông tin người dùng sau khi đăng nhập thành công
          useChatStore.getState().fetchConversations();

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

      fetchMe: async () => {
        try {
          set({ loading: true });
          const user = await authService.fetchMe();
          set({ user });
        } catch (error) {
          console.error(error);
          set({ user: null, accessToken: null });
          toast.error("Không thể lấy thông tin người dùng. Vui lòng thử lại.");
        } finally {
          set({ loading: false });
        }
      },

      refresh: async () => {
        try {
          set({ loading: true });
          const { user, fetchMe, setAccessToken } = get();
          const accessToken = await authService.refresh();
          setAccessToken(accessToken);

          if (!user) {
            await fetchMe();
          }
        } catch (error) {
          console.error(error);
          toast.error("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!");
          get().clearState();
        } finally {
          set({ loading: false });
        }
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({ user: state.user }), //chỉ persist user
    },
  ),
);
