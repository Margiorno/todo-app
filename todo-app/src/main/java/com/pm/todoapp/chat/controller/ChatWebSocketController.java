package com.pm.todoapp.chat.controller;

import com.pm.todoapp.chat.dto.MessageDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.service.ChatService;
import com.pm.todoapp.core.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

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
}
