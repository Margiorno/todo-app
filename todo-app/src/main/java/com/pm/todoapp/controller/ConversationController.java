package com.pm.todoapp.controller;

import com.pm.todoapp.dto.ConversationResponseDTO;
import com.pm.todoapp.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("/chat")
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<ConversationResponseDTO>> getAll(@AuthenticationPrincipal UUID userId) {

        List<ConversationResponseDTO> conversations = conversationService.findByUserId(userId);

        return ResponseEntity.ok(conversations);
    }
}
