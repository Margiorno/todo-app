package com.pm.todoapp.controller;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.MessageDTO;
import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.model.User;
import com.pm.todoapp.service.ChatService;
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

    @Autowired
    public WebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate, UsersService usersService) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.usersService = usersService;
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

    @MessageMapping("/user/{userId}/invite")
    public void sendFriendInvitation(
            @DestinationVariable UUID receiverId,
            Principal principal) {

        UUID senderId = UUID.fromString(principal.getName());

        FriendRequestDTO invitation = usersService.prepareFriendInvitation(senderId, receiverId);

        messagingTemplate.convertAndSendToUser(
                invitation.getReceiverId().toString(),
                "/queue/notification",
                invitation
        );
    }


}
