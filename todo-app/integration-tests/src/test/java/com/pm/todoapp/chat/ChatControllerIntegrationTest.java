package com.pm.todoapp.chat;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.todoapp.TodoAppApplication;
import com.pm.todoapp.chat.dto.ConversationRequestDTO;
import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.dto.SenderDTO;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.users.adapter.UserProviderAdapter;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TodoAppApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // There is no error – IntelliJ just underlines it incorrectly
public class ChatControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ConversationRepository conversationRepository;

    @Autowired private UserRepository userRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private UserProviderPort userProviderPort;

    UUID user1Id;
    UUID user2Id;
    @Autowired
    private UserProviderAdapter userProviderAdapter;

    private static String randomEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @BeforeEach
    void setUp() {
        var user1 = usersRepository.save(
                Instancio.of(com.pm.todoapp.users.profile.model.User.class)
                        .ignore(field(com.pm.todoapp.users.profile.model.User::getId))
                        .set(field(com.pm.todoapp.users.profile.model.User::getEmail), randomEmail())
                        .create()
        );

        var user2 = usersRepository.save(
                Instancio.of(com.pm.todoapp.users.profile.model.User.class)
                        .ignore(field(com.pm.todoapp.users.profile.model.User::getId))
                        .set(field(com.pm.todoapp.users.profile.model.User::getEmail), randomEmail())
                        .create()
        );

        user1Id = user1.getId();
        user2Id = user2.getId();
    }

    @Test
    void getConversations_returnsUserConversations_whenAuthenticated() throws Exception {
        Conversation conversation = conversationRepository.save(Instancio.of(Conversation.class)
                .ignore(field(Conversation::getId))
                .set(field(Conversation::getMessages), new ArrayList<>())
                .set(field(Conversation::getConversationType), ConversationType.GROUP_CHAT)
                .set(field(Conversation::getParticipants), Set.of(
                        userRepository.getReferenceById(user1Id),
                        userRepository.getReferenceById(user2Id)))
                .create());


        MvcResult result = mockMvc.perform(get("/chat/conversations")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user1Id,
                                        null,
                                        List.of()
                                )
                        )))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<ConversationResponseDTO> responseDTOs = objectMapper.readValue(content, new TypeReference<>() {});

        assertThat(responseDTOs)
                .isNotNull()
                .hasSize(1);

        ConversationResponseDTO responseDTO = responseDTOs.getFirst();

        assertThat(responseDTO.getTitle()).isEqualTo(conversation.getTitle());
        assertThat(responseDTO.getParticipants()).hasSize(2);
        assertThat(responseDTO.getParticipants())
                .extracting("id")
                .containsExactlyInAnyOrder(user1Id.toString(), user2Id.toString());
    }

    @Test
    public void getConversationBetweenUsers_shouldReturnEmptyConversations_whenAuthenticated() throws Exception {
        MvcResult result = mockMvc.perform(get("/chat/get-chat/%s".formatted(user2Id))
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(
                                user1Id,
                                null,
                                List.of()
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        ConversationResponseDTO responseDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ConversationResponseDTO.class);

        assertThat(responseDTO.getParticipants()).hasSize(2);
        assertThat(responseDTO.getParticipants())
                .extracting("id")
                .containsExactlyInAnyOrder(user1Id.toString(), user2Id.toString());

        var user2 = userProviderPort.getUserById(user2Id);
        assertThat(responseDTO.getTitle()).isEqualTo(
                "%s %s".formatted(
                        user2.getFirstName(),user2.getLastName()));

        assertThat(responseDTO.getType()).isEqualTo(ConversationType.PRIVATE);
    }

    @Test
    public void createConversation_shouldCreateConversation_whenAuthenticated() throws Exception {

        ConversationRequestDTO request = Instancio.of(ConversationRequestDTO.class)
                .set(field(ConversationRequestDTO::getParticipantIds),Set.of(user2Id))
                .create();

        MvcResult result1 = mockMvc.perform(post("/chat/new")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(
                                user1Id,
                                null,
                                List.of()
                        )))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/chat/conversations")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user1Id,
                                        null,
                                        List.of()
                                )
                        )))
                .andExpect(status().isOk())
                .andReturn();

        ConversationResponseDTO response1DTO = objectMapper.readValue(
                result1.getResponse().getContentAsString(StandardCharsets.UTF_8),
                ConversationResponseDTO.class);

        List<ConversationResponseDTO> response2DTOs = objectMapper.readValue(
                result2.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {});

        assertThat(response2DTOs)
                .isNotNull()
                .hasSize(1);

        ConversationResponseDTO response2DTO = response2DTOs.getFirst();

        assertThat(response1DTO.getId())
                .isEqualTo(response2DTO.getId());
        assertThat(response1DTO.getTitle())
                .isEqualTo(response2DTO.getTitle());
        assertThat(response1DTO.getType())
                .isEqualTo(response2DTO.getType())
                .isEqualTo(ConversationType.GROUP_CHAT);
        assertThat(response1DTO.getParticipants())
                .hasSize(2);
        assertThat(response1DTO.getParticipants())
                .extracting("id")
                .containsExactlyInAnyOrder(user1Id.toString(), user2Id.toString());
    }
}
