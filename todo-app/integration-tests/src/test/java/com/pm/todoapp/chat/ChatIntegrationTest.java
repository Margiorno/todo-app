package com.pm.todoapp.chat;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.todoapp.TodoAppApplication;
import com.pm.todoapp.chat.dto.ConversationRequestDTO;
import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
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
import org.springframework.test.web.reactive.server.WebTestClient;
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
public abstract class ChatIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected ConversationRepository conversationRepository;

    @Autowired protected UserRepository userRepository;
    @Autowired protected UsersRepository usersRepository;
    @Autowired protected UserProviderPort userProviderPort;

    UUID user1Id;
    UUID user2Id;

    protected static String randomEmail() {
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

    protected Conversation createTestConversation(Set<UUID> participants) throws Exception {
        return conversationRepository.save(Instancio.of(Conversation.class)
                .ignore(field(Conversation::getId))
                .set(field(Conversation::getMessages), new ArrayList<>())
                .set(field(Conversation::getConversationType), ConversationType.GROUP_CHAT)
                .set(field(Conversation::getParticipants), participants)
                .create());
    }
}
