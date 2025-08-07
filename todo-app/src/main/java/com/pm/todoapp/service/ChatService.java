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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final UsersService usersService;

    @Autowired
    public ChatService(ConversationRepository conversationRepository, UsersService usersService) {
        this.conversationRepository = conversationRepository;
        this.usersService = usersService;
    }

    public Conversation findConversationById(UUID id){
        return conversationRepository.findById(id).orElseThrow(
                () -> new ConversationNotFoundException("Conversation with id " + id + " not found"));
    }

    public List<ConversationResponseDTO> findByUserId(UUID userId) {
        User user = usersService.findRawById(userId);
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
        User user = usersService.findRawById(userId);
        Conversation conversation = findConversationById(conversationId);

        if (!conversation.getParticipants().contains(user))
            throw new UnauthorizedException("You do not have permission to access this conversation");

        return conversation.getMessages().stream().map(
                message ->  MessageMapper.toResponseDTO(message, userId)
        ).toList();
    }

    @Transactional
    public Map<User, MessageResponseDTO> prepareMessagesToSend(UUID chatId, UUID uuid, String content) {

        Conversation conversation = findConversationById(chatId);
        User sender = usersService.findRawById(uuid);

        Message savedMessage = saveNewMessage(conversation, sender, content);

        Map<User, MessageResponseDTO> personalizedMessages = new HashMap<>();

        for (User user : conversation.getParticipants()) {

            MessageResponseDTO personalizedMessageDTO = MessageMapper.toResponseDTO(savedMessage, user.getId());
            personalizedMessages.put(user, personalizedMessageDTO);
        }

        return personalizedMessages;
    }

    @Transactional
    protected Message saveNewMessage(Conversation conversation, User sender, String content) {

        Message message = Message.builder().
                sender(sender)
                .content(content)
                .build();

        conversation.addMessage(message);

        conversationRepository.save(conversation);

        return conversation.getMessages().getLast();
    }
}
