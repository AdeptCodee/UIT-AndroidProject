import { Badge } from "../ui/badge";
import UserAvatar from "./UserAvatar";
import { cn, formatMessageTime } from "@/lib/utils";
import { Card } from "../ui/card";
import type { Conversation, Message } from "@/types/chat";

interface MessageItemProps {
  message: Message;
  index: number;
  messages: Message[];
  selectedConvo: Conversation;
  lastMessageStatus: "delivered" | "seen";
}

const MessageItem = ({
  message,
  index,
  messages,
  selectedConvo,
  lastMessageStatus,
}: MessageItemProps) => {
  const prev = messages[index - 1];

  const isGroupBreak =
    index === 0 ||
    message.senderId !== prev?.senderId ||
    new Date(message.createdAt).getTime() -
      new Date(prev?.createdAt || 0).getTime() >
      60 * 1000; // 1 phút

  const participant = selectedConvo.participants.find(
    (p) => p._id.toString() === message.senderId.toString(),
  );
  //   return (
  //     <div
  //       className={cn(
  //         "flex gap-2 message-bounce",
  //         message.isOwn ? "justify-end" : "justify-start",
  //       )}
  //     >
  //       {/* avatar */}
  //       {!message.isOwn && (
  //         <div className="w-8">
  //           {isGroupBreak && (
  //             <UserAvatar
  //               type="chat"
  //               name={participant?.displayName ?? "ChatRT"}
  //               avatarUrl={participant?.avatarUrl ?? undefined}
  //             />
  //           )}
  //         </div>
  //       )}

  //       {/* tin nhan */}
  //       <div
  //         className={cn(
  //           "max-w-xs lg:max-w-md space-y-1 flex flex-col",
  //           message.isOwn ? "items-end" : "items-start",
  //         )}
  //       >
  //         <Card
  //           className={cn(
  //             "p-3",
  //             message.isOwn
  //               ? "chat-bubble-sent border-0"
  //               : "bg-chat-bubble-received",
  //           )}
  //         >
  //           <p className="text-sm leading-relaxed break-words">
  //             {message.content}
  //           </p>
  //         </Card>

  //         {/* time */}
  //         {isGroupBreak && (
  //           <span className="text-xs text-muted-foreground px-1">
  //             {formatMessageTime(new Date(message.createdAt))}
  //           </span>
  //         )}

  //         {/* seen/delivered */}
  //         {message.isOwn && message._id === selectedConvo.lastMessage?._id && (
  //           <Badge
  //             variant="outline"
  //             className={cn(
  //               "text-xs px-1.5 py-0.5 h-4 border-0",
  //               lastMessageStatus === "seen"
  //                 ? "bg-primary/20 text-primary"
  //                 : "bg-muted text-muted-foreground",
  //             )}
  //           >
  //             {lastMessageStatus}
  //           </Badge>
  //         )}
  //       </div>
  //     </div>
  //   );

  // chat gemini code
  return (
    // Đã sửa: Dùng hàm cn() để tạo khoảng cách (mt-4 nếu là tin mới/người khác, mt-1 nếu nhắn liên tục)
    <div
      className={cn("flex flex-col w-full", isGroupBreak ? "mt-2" : "mt-0.5")}
    >
      {/* 1. KHỐI HIỂN THỊ THỜI GIAN Ở GIỮA */}
      {isGroupBreak && (
        <div className="flex justify-center w-full my-3">
          <span className="text-xs text-muted-foreground px-2 py-1 bg-muted/50 rounded-full">
            {formatMessageTime(new Date(message.createdAt))}
          </span>
        </div>
      )}

      {/* 2. KHỐI TIN NHẮN CHÍNH */}
      <div
        className={cn(
          "flex gap-2 message-bounce w-full",
          message.isOwn ? "justify-end" : "justify-start",
        )}
      >
        {/* avatar */}
        {!message.isOwn && (
          <div className="w-8">
            {isGroupBreak && (
              <UserAvatar
                type="chat"
                name={participant?.displayName ?? "ChatRT"}
                avatarUrl={participant?.avatarUrl ?? undefined}
              />
            )}
          </div>
        )}

        {/* Bong bóng tin nhắn */}
        <div
          className={cn(
            "max-w-xs lg:max-w-md flex flex-col",
            message.isOwn ? "items-end" : "items-start",
          )}
        >
          {/* Đã sửa: Hiển thị tên người gửi nếu là chat nhóm và không phải tin của mình */}
          {selectedConvo.type === "group" && !message.isOwn && isGroupBreak && (
            <span className="text-xs font-semibold text-primary/80 mb-1 px-1 block">
              {participant?.displayName ?? "Thành viên nhóm"}
            </span>
          )}

          <Card
            className={cn(
              "p-3",
              message.isOwn
                ? "chat-bubble-sent border-0"
                : "bg-chat-bubble-received",
            )}
          >
            <p className="text-sm leading-relaxed break-words">
              {message.content}
            </p>
          </Card>

          {/* seen/delivered (Vẫn giữ ở góc nhỏ bên dưới tin nhắn của mình) */}
          {message.isOwn && message._id === selectedConvo.lastMessage?._id && (
            <div className="mt-1">
              <Badge
                variant="outline"
                className={cn(
                  "text-xs px-1.5 py-0.5 h-4 border-0",
                  lastMessageStatus === "seen"
                    ? "bg-primary/20 text-primary"
                    : "bg-muted text-muted-foreground",
                )}
              >
                {lastMessageStatus}
              </Badge>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MessageItem;
