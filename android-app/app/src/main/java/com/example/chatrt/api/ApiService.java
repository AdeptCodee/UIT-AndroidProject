package com.example.chatrt.api;

import com.example.chatrt.models.*;import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ==========================================
    // 1. AUTH SERVICE (Thay thế authService.ts)
    // ==========================================
    @POST("auth/signin")
    Call<AuthResponse> signIn(@Body Map<String, String> credentials);

    @POST("auth/signup")
    Call<AuthResponse> signUp(@Body Map<String, String> userData);

    @POST("auth/signout")
    Call<Void> signOut();

    @POST("auth/refresh")
    Call<AuthResponse> refreshToken();

    @GET("users/me")
    Call<User> fetchMe();


    // ==========================================
    // 2. CHAT SERVICE (Thay thế chatService.ts)
    // ==========================================
    // Sửa lại dòng này
    @GET("conversations")
    Call<ConversationsResponse> getConversations();

    @GET("conversations/{id}/messages")
    Call<MessagesResponse> getMessages(
            @Path("id") String conversationId,
            @Query("limit") int limit,
            @Query("cursor") String cursor
    );

    @POST("conversations")
    Call<Conversation> createConversation(@Body Map<String, Object> data);

    @PATCH("conversations/{id}/seen")
    Call<Void> markAsSeen(@Path("id") String conversationId);

    // Gửi tin nhắn cá nhân (có thể kèm ảnh)
    @Multipart
    @POST("messages/direct")
    Call<Message> sendDirectMessage(
            @Part("recipientId") RequestBody recipientId,
            @Part("content") RequestBody content,
            @Part("conversationId") RequestBody conversationId,
            @Part MultipartBody.Part image
    );

    // Gửi tin nhắn nhóm (có thể kèm ảnh)
    @Multipart
    @POST("messages/group")
    Call<Message> sendGroupMessage(
            @Part("conversationId") RequestBody conversationId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image
    );


    // ==========================================
    // 3. FRIEND SERVICE (Thay thế friendService.ts)
    // ==========================================
    @GET("friends")
    Call<List<User>> getFriendList();

    @GET("users/search")
    Call<SearchResponse> searchUser(@Query("username") String username);

    @GET("friends/requests")
    Call<FriendRequestsResponse> getAllFriendRequests();

    @POST("friends/requests")
    Call<Void> sendFriendRequest(@Body Map<String, String> data);

    @POST("friends/requests/{id}/accept")
    Call<Void> acceptRequest(@Path("id") String requestId);

    @POST("friends/requests/{id}/decline")
    Call<Void> declineRequest(@Path("id") String requestId);


    // ==========================================
    // 4. USER SERVICE (Thay thế userService.ts)
    // ==========================================
    @Multipart
    @POST("users/uploadAvatar")
    Call<User> uploadAvatar(@Part MultipartBody.Part avatar);
}