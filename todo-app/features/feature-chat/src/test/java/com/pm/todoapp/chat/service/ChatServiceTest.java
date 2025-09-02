package com.pm.todoapp.chat.service;

import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.model.Message;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.chat.repository.MessageRepository;
import com.pm.todoapp.common.exceptions.ConversationNotFoundException;
import com.pm.todoapp.common.exceptions.UnauthorizedException;
import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@Slf4j
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

        log.info("Setup test users: user1Id={}, user2Id={}", user1Id, user2Id);
    }

    @Test
    void findNotExistingRawConversationById_shouldThrowException() {

        UUID nonExistingConversationId = UUID.randomUUID();
        log.info("Test case: findRawConversationById for a non-existent ID={}", nonExistingConversationId);

        when(conversationRepository.findById(nonExistingConversationId))
                .thenReturn(Optional.empty());

        log.debug("Executing service call and asserting for ConversationNotFoundException");
        assertThatThrownBy(() -> chatService.findRawConversationById(nonExistingConversationId))
                .isInstanceOf(ConversationNotFoundException.class)
                .hasMessageContaining("Conversation with id " + nonExistingConversationId + " not found");

        log.info("SUCCESS: Verified that exception was thrown as expected.");
    }

    @Test
    void findOrCreatePrivateConversation_shouldCreateNewConversation() {

        log.info("Test case: findOrCreatePrivateConversation when conversation does not exist between {} and {}", user1Id, user2Id);
        UUID expectedConversationId = UUID.randomUUID();
        log.debug("Expecting new conversation to have ID: {}", expectedConversationId);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));

        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);
        when(userRepository.getReferenceById(user2Id)).thenReturn(user2);

        log.debug("Mocking findPrivateConversationBetweenUsers to return Optional.empty()");
        when(conversationRepository.findPrivateConversationBetweenUsers(user1,user2))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> {
                            Conversation conversation = invocation.getArgument(0);
                            conversation.setId(expectedConversationId);
                            conversation.setParticipants(Set.of(user1,user2));
                            log.debug("Mocking repository.save() to return conversation with ID: {}", conversation.getId());
                            return conversation;
                });

        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);

        UserDTO user2Dto = Instancio.create(UserDTO.class);
        user2Dto.setId(user2Id);

        when(userProviderPort.getUsersByIds(anySet())).thenReturn(Set.of(user1Dto, user2Dto));

        log.info("Executing chatService.findOrCreatePrivateConversation()");
        ConversationResponseDTO result = chatService.findOrCreatePrivateConversation(user1Id, user2Id);
        log.info("Service call returned a result. Proceeding with assertions.");

        log.debug("Verifying internal behavior: checking what was passed to repository.save()");
        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(conversationCaptor.capture());
        Conversation savedConversation = conversationCaptor.getValue();
        assertThat(savedConversation.getConversationType()).isEqualTo(ConversationType.PRIVATE);
        assertThat(savedConversation.getParticipants()).containsExactlyInAnyOrder(user1, user2);
        log.debug("Internal verification successful.");

        log.debug("Verifying external result: checking the returned ConversationResponseDTO");
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
        log.info("SUCCESS: All assertions for creating a new conversation passed.");
    }

    @Test
    void getMessages_shouldThrowException_whenUserIsNotParticipant() {

        UUID conversationId = UUID.randomUUID();
        log.info("Test case: getMessages when user={} is not a participant of conversation={}", user1Id, conversationId);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        log.debug("Mocking conversationRepository.findById to simulate conversation without participants");
        Conversation conversation = Instancio.create(Conversation.class);
        conversation.setId(conversationId);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        log.debug("Executing service call and asserting for UnauthorizedException");
        assertThatThrownBy(() -> chatService.getMessages(conversationId, user1Id))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You do not have permission to access this conversation");


        log.info("SUCCESS: Verified that UnauthorizedException was thrown as expected.");
    }

    @Test
    void getMessages_shouldReturnMessageDTOs_whenUserIsParticipant() {

        UUID conversationId = UUID.randomUUID();
        log.info("Test case: getMessages when user={} is a participant of conversation={}", user1Id, conversationId);

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        log.debug("Mocking userRepository.getReferenceById for user1");
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        log.debug("Setting up conversation with messages and participants");
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

        log.debug("Setting up UserDTOs for participants");
        UserDTO user1Dto = Instancio.create(UserDTO.class);
        user1Dto.setId(user1Id);

        UserDTO user2Dto = Instancio.create(UserDTO.class);
        user2Dto.setId(user2Id);

        log.debug("Mocking userProviderPort.getUserById for user1 and user2");
        when(userProviderPort.getUserById(user1Id)).thenReturn(user1Dto);
        when(userProviderPort.getUserById(user2Id)).thenReturn(user2Dto);

        log.debug("Mocking conversationRepository.findById to return the conversation");
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        log.info("Executing chatService.getMessages()");
        List<MessageResponseDTO> result = chatService.getMessages(conversationId, user1Id);
        log.info("Service call returned {} messages", result.size());

        log.debug("Asserting result is not null and contains expected number of messages");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        log.debug("Verifying message contents");
        assertThat(result)
                .extracting(MessageResponseDTO::getContent)
                .containsExactly(message1.getContent(), message2.getContent());

        log.debug("Verifying which messages were sent by the current user");
        assertThat(result.get(0).isSentByCurrentUser()).isTrue();
        assertThat(result.get(1).isSentByCurrentUser()).isFalse();

        log.info("SUCCESS: All assertions for getMessages with participant passed.");
    }

    @Test
    void getConversations_shouldReturnConversationDTOs_inCorrectOrder(){
        UUID conversation1id = UUID.randomUUID();
        UUID conversation2id = UUID.randomUUID();
        UUID conversation3id = UUID.randomUUID();

        log.info("Test case: getConversations for user={}", user1Id);

        log.debug("Creating messages with timestamps for ordering");
        Message conversation1message = Instancio.create(Message.class);
        conversation1message.setSentAt(LocalDateTime.now());

        Message conversation2message = Instancio.create(Message.class);
        conversation2message.setSentAt(LocalDateTime.now().minusDays(1));

        log.debug("Setting up conversations with participants and messages");
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

        conversation1message.setConversation(conversation1);
        conversation2message.setConversation(conversation2);

        log.debug("Mocking user validation and user repository");
        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        log.debug("Mocking conversationRepository.findByParticipantsContains to return all conversations");
        when(conversationRepository.findByParticipantsContains(user1)).thenReturn(
                Set.of(conversation2, conversation3, conversation1)
        );

        log.info("Executing chatService.findByUserId()");
        List<ConversationResponseDTO> result = chatService.findByUserId(user1Id);
        log.info("Service call returned {} conversations", result.size());

        log.debug("Asserting result is not null and contains expected number of conversations");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);

        log.debug("Verifying order of returned conversations based on messages");
        assertThat(result.get(0).getId()).isEqualTo(conversation1id);
        assertThat(result.get(1).getId()).isEqualTo(conversation2id);
        assertThat(result.get(2).getId()).isEqualTo(conversation3id);

        log.info("SUCCESS: All assertions for getConversations returned in correct order passed.");
    }

    @Test
    void getConversations_shouldReturnEmptyList(){
        log.debug("Mocking user validation and user repository");
        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1Id)).thenReturn(user1);

        log.debug("Mocking conversationRepository.findByParticipantsContains to return empty set");
        when(conversationRepository.findByParticipantsContains(user1)).thenReturn(
                Set.of()
        );

        log.info("Executing chatService.findByUserId()");
        List<ConversationResponseDTO> result = chatService.findByUserId(user1Id);
        log.info("Service call returned {} conversations", result.size());

        log.debug("Asserting result is empty and contains no conversations");
        assertThat(result).isEmpty();

        log.info("SUCCESS: All assertions for getConversations returned no conversations.");
    }
}
