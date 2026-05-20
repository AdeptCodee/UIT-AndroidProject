import type { Socket } from "socket.io-client";
import type { Conversation, Message } from "./chat";
import type { USER } from "./user";

export interface AuthState {
  accessToken: string | null;
  user: USER | null; // Có thể định nghĩa kiểu cụ thể hơn nếu biết cấu trúc user
  loading: boolean;

  setAccessToken: (accessToken: string) => void;
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

  fetchMe: () => Promise<void>;

  refresh: () => Promise<void>;
}
export interface ThemeState {
  isDark: boolean;
  toggleTheme: () => void;
  setTheme: (dark: boolean) => void;
}

export interface ChatState {
  conversations: Conversation[];
  messages: Record<
    string,
    {
      items: Message[];
      hasMore: boolean; //infinite scroll
      nextCursor?: string | null; // phân trang
    }
  >;
  activeConversationId: string | null;
  convoLoading: boolean;
  messageLoading: boolean;
  reset: () => void;

  setActiveConversation: (id: string | null) => void;
  fetchConversations: () => Promise<void>;
  fetchMessages: (conversationId?: string) => Promise<void>;
  sendDirectMessage: (
    recipientId: string,
    content: string,
    imgUrl?: string,

  ) => Promise<void>;
  sendGroupMessage: (
    conversationId: string,
    content: string,
    imgUrl?: string,
  ) => Promise<void>;

  // add message
  addMessage: (message: Message) => Promise<void>;
  // update message
  updateConversation: (conversation: Conversation) => void;
}

export interface SocketState {
  socket: Socket | null;
  onlineUsers: string[]; // danh sách userId đang online
  connectSocket: () => void;
  disconnectSocket: () => void;
}