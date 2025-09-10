package com.pm.todoapp.chat.service;


import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.dto.SenderDTO;
import com.pm.todoapp.chat.mapper.MessageMapper;
import com.pm.todoapp.chat.mapper.SenderMapper;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.model.Message;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.common.exceptions.ConversationNotFoundException;
import com.pm.todoapp.common.exceptions.UnauthorizedException;
import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserValidationPort userValidationPort;
    private final UserProviderPort userProviderPort;


    public Conversation findRawConversationById(UUID id){
        return conversationRepository.findById(id).orElseThrow(
                () -> new ConversationNotFoundException("Conversation with id " + id + " not found"));
    }

    public List<ConversationResponseDTO> findByUserId(UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

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

        userValidationPort.ensureUserExistsById(currentUser);
        userValidationPort.ensureUserExistsById(otherUser);

        User user1 = userRepository.getReferenceById(currentUser);
        User user2 = userRepository.getReferenceById(otherUser);

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

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Conversation conversation = findRawConversationById(conversationId);

        if (!conversation.getParticipants().contains(user))
            throw new UnauthorizedException("You do not have permission to access this conversation");

        return conversation.getMessages().stream().map(
                message ->  {
                    SenderDTO senderDTO = SenderMapper.fromUserDtoToSenderDTO(
                            userProviderPort.getUserById(message.getSender().getId()));

                    return MessageMapper.toResponseDTO(message,senderDTO,userId);
                }
        ).toList();
    }

    @Transactional
    public Map<User, MessageResponseDTO> prepareMessagesToSend(UUID chatId, UUID userId, String content) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);
        UserDTO userDTO = userProviderPort.getUserById(userId);
        SenderDTO senderDTO = SenderMapper.fromUserDtoToSenderDTO(userDTO);

        Conversation conversation = findRawConversationById(chatId);
        Message savedMessage = saveNewMessage(conversation, user, content);
        Map<User, MessageResponseDTO> personalizedMessages = new HashMap<>();

        for (User participant : conversation.getParticipants()) {

            MessageResponseDTO personalizedMessageDTO = MessageMapper.toResponseDTO(savedMessage, senderDTO, participant.getId());
            personalizedMessages.put(participant, personalizedMessageDTO);
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

    public ConversationResponseDTO newConversation(String conversationName, Set<UUID> participantIds, UUID creatorId) {

        userValidationPort.ensureUserExistsById(creatorId);
        userValidationPort.ensureUsersExistsById(participantIds);

        User creator = userRepository.getReferenceById(creatorId);
        Set<User> participants = participantIds.stream()
                .map(userRepository::getReferenceById)
                .collect(Collectors.toSet());

        participants.add(creator);

        Conversation conversation = Conversation.builder()
                .conversationType(ConversationType.GROUP_CHAT)
                .title(conversationName)
                .participants(participants)
                .build();

        return toResponseDTO(conversationRepository.save(conversation), creatorId);
    }

    private ConversationResponseDTO toResponseDTO(Conversation conversation, UUID currentUserId) {

        Set<UUID> participantsIds = conversation.getParticipants()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<UserDTO> participants = userProviderPort.getUsersByIds(participantsIds);


        String title = switch (conversation.getConversationType()){
            case PRIVATE -> participants.stream()
                    .filter(participant -> !participant.getId().equals(currentUserId))
                    .findFirst()
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .orElse("unknown user");
            case GROUP_CHAT -> conversation.getTitle();
        };

        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .type(conversation.getConversationType())
                .title(title)
                .participants(participants
                        .stream()
                        .map(SenderMapper::fromUserDtoToSenderDTO)
                        .collect(Collectors.toSet()))
                .build();
    }


}
