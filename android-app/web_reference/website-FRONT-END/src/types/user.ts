export interface USER {
  _id: string;
  username: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  bio?: string;
  phone?: string;
<<<<<<< HEAD
=======
  accountNo?: string;
  accountName?: string;
  acqId?: string;
>>>>>>> repo-moi/main
  createdAt?: string;
  updatedAt?: string;
}

export interface Friend {
  _id: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
}

export interface FriendRequest {
  _id: string;
  from?: {
    _id: string;
    username: string;
    displayName: string;
    avatarUrl?: string;
  };
  to?: {
    _id: string;
    username: string;
    displayName: string;
    avatarUrl?: string;
  };
  message: string;
  createdAt: string;
  updatedAt: string;
}
