package com.pm.todoapp.chat.controller;

import com.pm.todoapp.chat.dto.ConversationRequestDTO;
import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat API", description = "Endpoints for managing chat conversations and messages")
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "Get all conversations for the authenticated user",
            description = "Retrieves a list of all private and group conversations the user is a participant in. The list is sorted by the most recent message activity.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of conversations"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getAll(@AuthenticationPrincipal UUID userId) {

        List<ConversationResponseDTO> conversations = chatService.findByUserId(userId);

        return ResponseEntity.ok(conversations);
    }

    @Operation(
            summary = "Get all messages for a specific conversation",
            description = "Retrieves the full message history for a given conversation. The user must be a participant to access the messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                    //TODO @ApiResponse(responseCode = "403", description = "Forbidden if the user is not a participant of the conversation"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the conversation does not exist")
            }
    )
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID chatId
    ){

        List<MessageResponseDTO> messages = chatService.getMessages(chatId, userId);

        return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "Create a new group conversation",
            description = "Creates a new group conversation with a specified name and a set of participants. The creator is automatically included.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Conversation created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the request body is invalid"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
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

    @Operation(
            summary = "Find or create a private conversation with another user",
            description = "Retrieves an existing one-on-one conversation between the authenticated user and another user. If no conversation exists, a new one is created and returned.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved or created the private conversation"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the other user does not exist")
            }
    )
    @GetMapping("/get-chat/{userId}")
    public ResponseEntity<ConversationResponseDTO> getChat(
            @AuthenticationPrincipal UUID loggedInUserId,
            @PathVariable UUID userId
    ){

        return ResponseEntity.ok(chatService.findOrCreatePrivateConversation(loggedInUserId, userId));

    }
}
