package com.pm.todoapp.controller;

import com.pm.todoapp.dto.ConversationRequestDTO;
import com.pm.todoapp.dto.ConversationResponseDTO;
import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

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
}
