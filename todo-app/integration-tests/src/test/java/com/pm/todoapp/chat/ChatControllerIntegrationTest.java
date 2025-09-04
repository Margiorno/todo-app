package com.pm.todoapp.chat;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.todoapp.TodoAppApplication;
import com.pm.todoapp.chat.repository.ConversationRepository;
import com.pm.todoapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = TodoAppApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChatControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    void contextLoadsAndDependenciesAreInjected() {
        assertThat(mockMvc).isNotNull();
        assertThat(objectMapper).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(conversationRepository).isNotNull();
    }
}
