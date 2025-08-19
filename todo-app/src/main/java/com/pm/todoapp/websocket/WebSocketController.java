package com.pm.todoapp.websocket;

import com.pm.todoapp.chat.dto.MessageDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.chat.service.ChatService;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.service.NotificationService;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.dto.FriendRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;


// TODO DIVIDE TO CHAT AND NOTIFICATION
@Controller
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UsersService usersService;
    private final NotificationService notificationService;

    @Autowired
    public WebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate, UsersService usersService, NotificationService notificationService) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.usersService = usersService;
        this.notificationService = notificationService;
    }

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @DestinationVariable UUID chatId,
            @Payload MessageDTO message,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());

        Map<User, MessageResponseDTO> personalizedMessages = chatService.prepareMessagesToSend(
                chatId,
                userId,
                message.getContent()
        );

        personalizedMessages.forEach((participant, dto) -> {
            messagingTemplate.convertAndSendToUser(
                    participant.getId().toString(),
                    "/queue/messages",
                    dto
            );
        });
    }

    @MessageMapping("/user/{receiverId}/invite")
    public void sendFriendInvitation(
            @DestinationVariable UUID receiverId,
            Principal principal) {

        UUID senderId = UUID.fromString(principal.getName());

        FriendRequestDTO invitation = usersService.saveFriendRequest(senderId, receiverId);
        NotificationDTO notification = notificationService.createNotification(invitation);

        messagingTemplate.convertAndSendToUser(
                invitation.getReceiverId().toString(),
                "/queue/notification",
                notification
        );
    }


}
