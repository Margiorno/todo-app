package com.pm.todoapp.service;

import com.pm.todoapp.dto.ConversationResponseDTO;
import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.exceptions.ConversationNotFoundException;
import com.pm.todoapp.exceptions.UnauthorizedException;
import com.pm.todoapp.mapper.ConversationMapper;
import com.pm.todoapp.mapper.MessageMapper;
import com.pm.todoapp.model.Conversation;
import com.pm.todoapp.model.Message;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.ConversationRepository;
import org.hibernate.query.sqm.tree.domain.SqmTreatedBagJoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UsersService usersService;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository, UsersService usersService) {
        this.conversationRepository = conversationRepository;
        this.usersService = usersService;
    }

    public List<ConversationResponseDTO> findByUserId(UUID userId) {
        User user = usersService.findById(userId);
        Iterable<Conversation> conversations = conversationRepository.findByParticipantsContains(user);

        return StreamSupport.stream(conversations.spliterator(), false)
                .sorted((c1, c2) -> {
                    LocalDateTime last1 = c1.getMessages().stream()
                            .map(Message::getSentAt)
                            .max(Comparator.naturalOrder())
                            .orElse(LocalDateTime.MIN);

                    LocalDateTime last2 = c2.getMessages().stream()
                            .map(Message::getSentAt)
                            .max(Comparator.naturalOrder())
                            .orElse(LocalDateTime.MIN);

                    return last2.compareTo(last1);
                })
                .map(ConversationMapper::toResponseDTO)
                .toList();
    }

    public List<MessageResponseDTO> getMessages(UUID conversationId, UUID userId) {
        User user = usersService.findById(userId);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(
                ()-> new ConversationNotFoundException("Conversation with this id does not exists: " + conversationId)
        );

        if (!conversation.getParticipants().contains(user))
            throw new UnauthorizedException("You do not have permission to access this conversation");

        return conversation.getMessages().stream().map(
                message ->  MessageMapper.toResponseDTO(message, userId)
        ).toList();
    }
}
