package com.pm.todoapp.chat.controller;

import com.pm.todoapp.chat.dto.ConversationRequestDTO;
import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getAll(@AuthenticationPrincipal UUID userId) {

        List<ConversationResponseDTO> conversations = chatService.findByUserId(userId);

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID chatId
    ){

        List<MessageResponseDTO> messages = chatService.getMessages(chatId, userId);

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/new")
    public ResponseEntity<ConversationResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid ConversationRequestDTO conversationRequestDTO
    ){

        return ResponseEntity.ok(chatService.newConversation(
                conversationRequestDTO.getConversationName(),
                conversationRequestDTO.getParticipantIds(),
                userId));
    }

    @GetMapping("get-chat/{userId}")
    public ResponseEntity<ConversationResponseDTO> getChat(
            @AuthenticationPrincipal UUID loggedInUserId,
            @PathVariable UUID userId
    ){

        return ResponseEntity.ok(chatService.findOrCreatePrivateConversation(loggedInUserId, userId));

    }
}
