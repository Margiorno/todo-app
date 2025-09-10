package com.pm.todoapp.chat;

import com.pm.todoapp.TodoAppApplication;
import com.pm.todoapp.chat.dto.MessageDTO;
import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.model.Conversation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@SpringBootTest(
        classes = TodoAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class WebSocketChatControllerIntegrationTest extends ChatIntegrationTest{

    @LocalServerPort private int port;

    @Test
    public void connectWebSocket_shouldConnect() throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketStompClient stompClient = createWebSocketStompClient();
        String url = "http://localhost:" + port + "/ws";

        StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();
        session.disconnect();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sendMessage_shouldSendMessageToAllParticipants() throws Exception {

        var user3 = usersRepository.saveAndFlush(
                Instancio.of(com.pm.todoapp.users.profile.model.User.class)
                        .ignore(field(com.pm.todoapp.users.profile.model.User::getId))
                        .set(field(com.pm.todoapp.users.profile.model.User::getEmail), randomEmail())
                        .create()
        );
        UUID user3Id = user3.getId();

        Conversation conversation = createTestConversation(Set.of(user1Id, user2Id, user3Id));
        UUID conversationId = conversation.getId();

        MessageDTO messageToSend = Instancio.of(MessageDTO.class)
                .set(field(MessageDTO::getSender), userProviderPort.getUserById(user1Id))
                .set(field(MessageDTO::getConversationId), conversationId)
                .create();

        WebSocketStompClient stompClient1 = createWebSocketStompClient();
        WebSocketStompClient stompClient2 = createWebSocketStompClient();
        WebSocketStompClient stompClient3 = createWebSocketStompClient();

        BlockingDeque<MessageResponseDTO> receivedMessages1 = new LinkedBlockingDeque<>();
        BlockingDeque<MessageResponseDTO> receivedMessages2 = new LinkedBlockingDeque<>();
        BlockingDeque<MessageResponseDTO> receivedMessages3 = new LinkedBlockingDeque<>();

        StompSession session1 = connectWebSocket(stompClient1, user1Id.toString(), receivedMessages1);
        StompSession session2 = connectWebSocket(stompClient2, user2Id.toString(), receivedMessages2);
        StompSession session3 = connectWebSocket(stompClient3, user3Id.toString(), receivedMessages3);

        String destination = "/app/chat/" + conversationId + "/sendMessage";
        session1.send(destination, messageToSend);

        MessageResponseDTO receivedMsg1 = receivedMessages1.poll(5, TimeUnit.SECONDS);
        MessageResponseDTO receivedMsg2 = receivedMessages2.poll(5, TimeUnit.SECONDS);
        MessageResponseDTO receivedMsg3 = receivedMessages3.poll(5, TimeUnit.SECONDS);

        assertThat(receivedMsg1).isNotNull();
        assertThat(receivedMsg1.getContent()).isEqualTo(messageToSend.getContent());
        assertThat(receivedMsg1.getSender().getId()).isEqualTo(user1Id.toString());

        assertThat(receivedMsg2).isNotNull();
        assertThat(receivedMsg2.getContent()).isEqualTo(messageToSend.getContent());
        assertThat(receivedMsg2.getSender().getId()).isEqualTo(user1Id.toString());

        assertThat(receivedMsg3).isNotNull();
        assertThat(receivedMsg3.getContent()).isEqualTo(messageToSend.getContent());
        assertThat(receivedMsg3.getSender().getId()).isEqualTo(user1Id.toString());

        session1.disconnect();
        session2.disconnect();
        session3.disconnect();
    }

    private WebSocketStompClient createWebSocketStompClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport());

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(this.objectMapper);
        stompClient.setMessageConverter(converter);

        return stompClient;
    }

    private StompSession connectWebSocket(
            WebSocketStompClient stompClient,
            String userId,
            BlockingQueue<MessageResponseDTO> messageQueue) throws Exception {

        String url = "http://localhost:" + port + "/ws";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("user-id", userId);

        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/user/queue/messages", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return MessageResponseDTO.class;
                    }
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        messageQueue.add((MessageResponseDTO) payload);
                    }
                });
            }
        };

        return stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, sessionHandler)
                .get(5, TimeUnit.SECONDS);
    }





}
