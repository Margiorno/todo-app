package com.pm.todoapp.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pm.todoapp.chat.dto.ConversationRequestDTO;
import com.pm.todoapp.chat.dto.ConversationResponseDTO;
import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.chat.model.ConversationType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatControllerIntegrationTest extends ChatIntegrationTest{
    @Test
    void getConversations_returnsUserConversations_whenAuthenticated() throws Exception {
        Conversation conversation = createTestConversation(Set.of(user1Id, user2Id));


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

        AssertionsForClassTypes.assertThat(responseDTO.getTitle()).isEqualTo(conversation.getTitle());
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
        AssertionsForClassTypes.assertThat(responseDTO.getTitle()).isEqualTo(
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
        AssertionsForClassTypes.assertThat(response1DTO.getTitle())
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
