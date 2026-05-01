import Logout from "@/components/auth/Logout";
import { useAuthStore } from "@/stores/useAuthStore";
import { Button } from "@/components/ui/button";
import api from "@/lib/axios";
import { toast } from "sonner";

const ChatAppPage = () => {
  const user = useAuthStore((s) => s.user);
  const handleOnClick = async () => {
    try {
      await api.get("/users/test", { withCredentials: true });
      toast.success("ok");
    } catch (error) {
      toast.error("Error");
      console.error(error);
    }
  };
  return (
    <div>
      {user?.displayName}
      <Logout />
      <Button onClick={handleOnClick}>test</Button>
    </div>
  );
};

export default ChatAppPage;
