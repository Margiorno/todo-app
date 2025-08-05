package com.pm.todoapp.controller;

import com.pm.todoapp.dto.MessageDTO;
import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.model.Conversation;
import com.pm.todoapp.model.User;
import com.pm.todoapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @DestinationVariable UUID chatId,
            @Payload MessageDTO message,
            @AuthenticationPrincipal UUID uuid) {

        Map<User, MessageResponseDTO> personalizedMessages = chatService.prepareMessagesToSend(
                chatId,
                uuid,
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
}
