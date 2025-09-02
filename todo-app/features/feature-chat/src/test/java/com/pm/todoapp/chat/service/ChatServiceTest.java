package com.pm.todoapp.chat.service;

import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.chat.repository.MessageRepository;
import com.pm.todoapp.common.exceptions.ConversationNotFoundException;
import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

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

        UserDTO user1Dto = UserDTO.builder()
                .id(user1Id)
                .firstName("John")
                .lastName("Doe")
                .profilePicturePath("/img/john.png")
                .build();


        UserDTO user2Dto = UserDTO.builder()
                .id(user2Id)
                .firstName("Jane")
                .lastName("Smith")
                .profilePicturePath("/img/jane.png")
                .build();

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
        assertThat(result.getTitle()).isEqualTo("Jane Smith");

        assertThat(result.getParticipants())
                .isNotNull()
                .hasSize(2)
                .extracting("id", "firstName", "lastName", "profilePicturePath")
                .containsExactlyInAnyOrder(
                        tuple(user1Id.toString(), "John", "Doe", "/img/john.png"),
                        tuple(user2Id.toString(), "Jane", "Smith", "/img/jane.png")
                );
        log.info("SUCCESS: All assertions for creating a new conversation passed.");
    }

}
