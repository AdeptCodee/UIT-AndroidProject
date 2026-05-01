import type { USER } from "./user";

export interface AuthState {
  accessToken: string | null;
  user: USER | null; // Có thể định nghĩa kiểu cụ thể hơn nếu biết cấu trúc user
  loading: boolean;

  clearState: () => void;

  signUp: (
    username: string,
    password: string,
    email: string,
    firstName: string,
    lastName: string,
  ) => Promise<void>;

  signIn: (username: string, password: string) => Promise<void>;

  signOut: () => Promise<void>;
}
