package com.pm.todoapp.controller;

import com.pm.todoapp.dto.*;
import com.pm.todoapp.model.NotificationType;
import com.pm.todoapp.model.User;
import com.pm.todoapp.service.ChatService;
import com.pm.todoapp.service.NotificationService;
import com.pm.todoapp.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

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

        System.out.println("sending friend invitation");

        UUID senderId = UUID.fromString(principal.getName());

        FriendRequestDTO invitation = usersService.saveFriendRequest(senderId, receiverId);
        NotificationDTO notification = notificationService.createNotification(invitation);

        System.out.println(invitation);
        System.out.println(notification);

        messagingTemplate.convertAndSendToUser(
                invitation.getReceiverId().toString(),
                "/queue/notification",
                notification
        );
    }


}
