package com.pm.todoapp.chat.service;

import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.core.exceptions.ConversationNotFoundException;
import com.pm.todoapp.core.exceptions.UnauthorizedException;
import com.pm.todoapp.chat.mapper.MessageMapper;
import com.pm.todoapp.users.profile.mapper.UserMapper;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.model.Message;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.users.profile.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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

    public Conversation findRawConversationById(UUID id){
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
                .map(conversation -> toResponseDTO(conversation, userId))
                .toList();
    }

    public ConversationResponseDTO findOrCreatePrivateConversation(UUID currentUser, UUID otherUser){

        User user1 = usersService.findRawById(currentUser);
        User user2 = usersService.findRawById(otherUser);

        Conversation conversation = conversationRepository.findPrivateConversationBetweenUsers(user1, user2)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .conversationType(ConversationType.PRIVATE)
                            .participants(new HashSet<>(Arrays.asList(user1, user2)))
                            .build();
                    return conversationRepository.save(newConversation);
                });

        return toResponseDTO(conversation, currentUser);
    }

    public List<MessageResponseDTO> getMessages(UUID conversationId, UUID userId) {
        User user = usersService.findRawById(userId);
        Conversation conversation = findRawConversationById(conversationId);

        if (!conversation.getParticipants().contains(user))
            throw new UnauthorizedException("You do not have permission to access this conversation");

        return conversation.getMessages().stream().map(
                message ->  MessageMapper.toResponseDTO(message, UserMapper.toUserResponseDTO(user))
        ).toList();
    }

    @Transactional
    public Map<User, MessageResponseDTO> prepareMessagesToSend(UUID chatId, UUID uuid, String content) {

        Conversation conversation = findRawConversationById(chatId);
        User sender = usersService.findRawById(uuid);

        Message savedMessage = saveNewMessage(conversation, sender, content);

        Map<User, MessageResponseDTO> personalizedMessages = new HashMap<>();

        for (User user : conversation.getParticipants()) {

            MessageResponseDTO personalizedMessageDTO = MessageMapper.toResponseDTO(savedMessage, UserMapper.toUserResponseDTO(user));
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

    public ConversationResponseDTO newConversation(String conversationName, Set<UUID> participantIds, UUID userId) {

        User user = usersService.findRawById(userId);

        Set<User> participants = participantIds.stream()
                .map(usersService::findRawById)
                .collect(Collectors.toSet());

        participants.add(user);

        Conversation conversation = Conversation.builder()
                .conversationType(ConversationType.GROUP_CHAT)
                .title(conversationName)
                .participants(participants)
                .build();

        return toResponseDTO(conversationRepository.save(conversation), userId);
    }

    private ConversationResponseDTO toResponseDTO(Conversation conversation, UUID currentUserId) {

        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .type(conversation.getConversationType())
                .title(switch (conversation.getConversationType()){
                    case PRIVATE -> conversation.getParticipants().stream().filter(
                                    participant -> !participant.getId().equals(currentUserId))
                            .findFirst()
                            .map(user -> user.getFirstName() + " " + user.getLastName())
                            .orElse("unknown user");
                    case GROUP_CHAT -> conversation.getTitle();
                }).build();
    }


}
