package com.pm.todoapp.chat.service;

import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.dto.SenderDTO;
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
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidationPort userValidationPort;

    @Mock
    private UserProviderPort userProviderPort;

    @InjectMocks
    private ChatService chatService;

    private UUID user1Id;
    private UUID user2Id;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        user1 = User.builder().id(user1Id).build();
        user2 = User.builder().id(user2Id).build();
    }

    @Test
    void findNotExistingRawConversationById_shouldThrowException() {

        UUID nonExistingConversationId = UUID.randomUUID();

        when(conversationRepository.findById(nonExistingConversationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.findRawConversationById(nonExistingConversationId))
                .isInstanceOf(ConversationNotFoundException.class)
                .hasMessageContaining("Conversation with id " + nonExistingConversationId + " not found");
    }

    @Test
    void findOrCreatePrivateConversation_shouldCreateNewConversation() {

        UUID expectedConversationId = UUID.randomUUID();

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));

        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);
        when(userRepository.getReferenceById(user2Id)).thenReturn(user2);

        when(conversationRepository.findPrivateConversationBetweenUsers(user1,user2))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> {
                            Conversation conversation = invocation.getArgument(0);
                            conversation.setId(expectedConversationId);
                            conversation.setParticipants(Set.of(user1,user2));
                            return conversation;
                });

        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);

        UserDTO user2Dto = Instancio.create(UserDTO.class);
        user2Dto.setId(user2Id);

        when(userProviderPort.getUsersByIds(anySet())).thenReturn(Set.of(user1Dto, user2Dto));

        ConversationResponseDTO result = chatService.findOrCreatePrivateConversation(user1Id, user2Id);

        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(conversationCaptor.capture());
        Conversation savedConversation = conversationCaptor.getValue();
        assertThat(savedConversation.getConversationType()).isEqualTo(ConversationType.PRIVATE);
        assertThat(savedConversation.getParticipants()).containsExactlyInAnyOrder(user1, user2);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedConversationId);
        assertThat(result.getType()).isEqualTo(ConversationType.PRIVATE);
        assertThat(result.getTitle())
                .isEqualTo("%s %s".formatted(user2Dto.getFirstName(), user2Dto.getLastName()));

        assertThat(result.getParticipants())
                .isNotNull()
                .hasSize(2)
                .extracting("id", "firstName", "lastName", "profilePicturePath")
                .containsExactlyInAnyOrder(
                        tuple(user1Id.toString(), user1Dto.getFirstName(), user1Dto.getLastName(), user1Dto.getProfilePicturePath()),
                        tuple(user2Id.toString(), user2Dto.getFirstName(), user2Dto.getLastName(), user2Dto.getProfilePicturePath())
                );
    }

    @Test
    void getMessages_shouldThrowException_whenUserIsNotParticipant() {

        UUID conversationId = UUID.randomUUID();

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        Conversation conversation = Instancio.create(Conversation.class);
        conversation.setId(conversationId);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> chatService.getMessages(conversationId, user1Id))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You do not have permission to access this conversation");

    }

    @Test
    void getMessages_shouldReturnMessageDTOs_whenUserIsParticipant() {

        UUID conversationId = UUID.randomUUID();

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        Conversation conversation = Instancio.create(Conversation.class);
        conversation.setId(conversationId);
        conversation.setParticipants(Set.of(user1, user2));

        Message message1 = Instancio.create(Message.class);
        message1.setSender(user1);
        message1.setConversation(conversation);

        Message message2 = Instancio.create(Message.class);
        message2.setSender(user2);
        message2.setConversation(conversation);

        conversation.setMessages(List.of(message1, message2));

        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);

        UserDTO user2Dto = Instancio.create(UserDTO.class);
        user2Dto.setId(user2Id);

        when(userProviderPort.getUserById(user1Id)).thenReturn(user1Dto);
        when(userProviderPort.getUserById(user2Id)).thenReturn(user2Dto);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        List<MessageResponseDTO> result = chatService.getMessages(conversationId, user1Id);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        assertThat(result)
                .extracting(MessageResponseDTO::getContent)
                .containsExactly(message1.getContent(), message2.getContent());

        assertThat(result.get(0).isSentByCurrentUser()).isTrue();
        assertThat(result.get(1).isSentByCurrentUser()).isFalse();
    }

    @Test
    void getConversations_shouldReturnConversationDTOs_inCorrectOrder(){
        UUID conversation1id = UUID.randomUUID();
        UUID conversation2id = UUID.randomUUID();
        UUID conversation3id = UUID.randomUUID();

        Message conversation1message = Instancio.create(Message.class);
        conversation1message.setSentAt(LocalDateTime.now());

        Message conversation2message = Instancio.create(Message.class);
        conversation2message.setSentAt(LocalDateTime.now().minusDays(1));

        Conversation conversation1 = Instancio.create(Conversation.class);
        conversation1.setId(conversation1id);
        conversation1.setParticipants(Set.of(user1));
        conversation1.setMessages(List.of(conversation1message));

        Conversation conversation2 = Instancio.create(Conversation.class);
        conversation2.setId(conversation2id);
        conversation2.setParticipants(Set.of(user1));
        conversation2.setMessages(List.of(conversation2message));

        Conversation conversation3 = Instancio.create(Conversation.class);
        conversation3.setId(conversation3id);
        conversation3.setParticipants(Set.of(user1));
        conversation3.setMessages(List.of());

        conversation1message.setConversation(conversation1);
        conversation2message.setConversation(conversation2);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        when(conversationRepository.findByParticipantsContains(user1)).thenReturn(
                Set.of(conversation2, conversation3, conversation1)
        );

        List<ConversationResponseDTO> result = chatService.findByUserId(user1Id);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);

        assertThat(result.get(0).getId()).isEqualTo(conversation1id);
        assertThat(result.get(1).getId()).isEqualTo(conversation2id);
        assertThat(result.get(2).getId()).isEqualTo(conversation3id);
    }

    @Test
    void getConversations_shouldReturnEmptyList(){
        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        when(conversationRepository.findByParticipantsContains(user1)).thenReturn(
                Set.of()
        );

        List<ConversationResponseDTO> result = chatService.findByUserId(user1Id);

        assertThat(result).isEmpty();
    }

    @Test
    void prepareMessagesToSend_shouldPreparePersonalizedMessagesAndSaveChanges() {
        UUID conversationId = UUID.randomUUID();
        String messageContent =Instancio.create(String.class);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);
        when(userProviderPort.getUserById(user1Id)).thenReturn(user1Dto);

        Conversation conversation = Instancio.create(Conversation.class);
        conversation.setId(conversationId);
        conversation.setParticipants(Set.of(user1, user2));
        conversation.setMessages(new ArrayList<>());

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        Map<User, MessageResponseDTO> messages = chatService.prepareMessagesToSend(conversationId, user1Id, messageContent);

        assertThat(messages).isNotNull();
        assertThat(messages.size()).isEqualTo(2);
        assertThat(messages.get(user1).isSentByCurrentUser()).isTrue();
        assertThat(messages.get(user2).isSentByCurrentUser()).isFalse();

        assertThat(messages.values())
                .extracting(MessageResponseDTO::getContent)
                .containsOnly(messageContent);

        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(conversationCaptor.capture());

        Conversation savedConversation = conversationCaptor.getValue();
        assertThat(savedConversation.getMessages()).hasSize(1);

        Message savedMessage = savedConversation.getMessages().getFirst();
        assertThat(savedMessage.getContent()).isEqualTo(messageContent);
        assertThat(savedMessage.getSender()).isEqualTo(user1);

    }

    @Test
    public void newConversation_shouldCreateNewConversation() {
        UUID conversationId = UUID.randomUUID();
        String title =Instancio.create(String.class);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);
        when(userRepository.getReferenceById(user2Id)).thenReturn(user2);

        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);

        UserDTO user2Dto = Instancio.create(UserDTO.class);
        user2Dto.setId(user2Id);

        when(userProviderPort.getUsersByIds(Set.of(user1Id, user2Id)))
                .thenReturn(Set.of(user1Dto, user2Dto));


        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> {
                    Conversation conv = invocation.getArgument(0);
                    conv.setId(conversationId);
                    return conv;
                });

        ConversationResponseDTO result = chatService.newConversation(title, Set.of(user2Id), user1Id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(conversationId);
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getParticipants().size()).isEqualTo(2);
        assertThat(result.getParticipants())
                .extracting(SenderDTO::getId)
                .contains(user1Id.toString(), user2Id.toString());

    }


}
