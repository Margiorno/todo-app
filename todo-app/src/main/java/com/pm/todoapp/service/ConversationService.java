package com.pm.todoapp.service;

import com.pm.todoapp.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }
}
