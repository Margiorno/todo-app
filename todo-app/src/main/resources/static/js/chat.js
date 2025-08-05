document.addEventListener('DOMContentLoaded', function () {
    let stompClient = null;
    let currentConversationId = null;

    const conversationList = document.getElementById('conversation-list');
    const chatHeader = document.getElementById('chat-header');
    const chatMessages = document.getElementById('chat-messages');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');

    const connectWebSocket = () => {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/user/queue/messages', (message) => {
                onMessageReceived(JSON.parse(message.body));
            });
        }, (error) => {
            console.error('Connection error: ' + error);
        });
    };

    const onMessageReceived = (message) => {
        if (message.conversationId === currentConversationId) {
            appendMessageToChat(message);
        } else {
            const notificationTarget = document.querySelector(`a[data-conversation-id="${message.conversationId}"]`);
            if (notificationTarget && !notificationTarget.classList.contains('active')) {
                notificationTarget.classList.add('has-new-message');
            }
        }
    };

    const sendMessage = () => {
        const messageContent = messageInput.value.trim();
        if (messageContent && stompClient && currentConversationId) {
            const chatMessage = { content: messageContent };
            stompClient.send(`/app/chat/${currentConversationId}/sendMessage`, {}, JSON.stringify(chatMessage));
            messageInput.value = '';
        }
    };

    const appendMessageToChat = (message) => {
        const noMessagesDiv = chatMessages.querySelector('.no-messages');
        if(noMessagesDiv) noMessagesDiv.remove();

        const messageElement = document.createElement('div');
        messageElement.classList.add('message', message.sentByCurrentUser ? 'sent' : 'received');

        messageElement.textContent = message.context;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    };

    const displayMessages = (messages) => {
        chatMessages.innerHTML = '';
        if (!messages || messages.length === 0) {
            chatMessages.innerHTML = '<div class="text-center text-muted no-messages">No messages yet.</div>';
            return;
        }
        messages.forEach(message => appendMessageToChat(message));
    };

    const fetchMessages = async (conversationId) => {
        chatMessages.innerHTML = '<div>Loading messages...</div>';
        currentConversationId = conversationId;
        try {
            const response = await fetch(`/chat/${conversationId}/messages`, { credentials: 'include' });
            if (!response.ok) throw new Error(`Network Error: ${response.status}`);
            const messages = await response.json();
            displayMessages(messages);
        } catch (error) {
            console.error('Failed to fetch messages:', error);
            chatMessages.innerHTML = `<div class="message received text-danger">Failed to load messages.</div>`;
        }
    };

    const displayConversations = (conversations) => {
        conversationList.innerHTML = '';
        if (conversations.length === 0) {
            chatHeader.textContent = 'No conversations found.';
            return;
        }
        conversations.forEach((conversation, index) => {
            const a = document.createElement('a');
            a.href = '#';
            a.classList.add('nav-link');
            a.setAttribute('data-conversation-id', conversation.id);
            a.textContent = `Conversation ${conversation.id.substring(0, 8)}`;

            a.addEventListener('click', (e) => {
                e.preventDefault();
                document.querySelectorAll('#conversation-list .nav-link').forEach(link => {
                    link.classList.remove('active', 'has-new-message');
                });
                a.classList.add('active');
                chatHeader.textContent = a.textContent;
                fetchMessages(conversation.id);
            });
            conversationList.appendChild(a);

            if (index === 0) {
                a.click();
            }
        });
    };

    const fetchConversations = async () => {
        try {
            const response = await fetch('/chat/conversations', { credentials: 'include' });
            if (!response.ok) throw new Error(`Network Error: ${response.status}`);
            const conversations = await response.json();
            displayConversations(conversations);
        } catch (error) {
            console.error('Failed to fetch conversations:', error);
        }
    };

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    fetchConversations();
    connectWebSocket();
});