document.addEventListener('DOMContentLoaded', () => {
    const elements = {
        conversationList: document.getElementById('conversation-list'),
        chatHeader: document.getElementById('chat-header'),
        chatMessages: document.getElementById('chat-messages'),
        messageInput: document.getElementById('message-input'),
        sendButton: document.getElementById('send-button')
    };

    class Chat {
        constructor(elements) {
            this.stompClient = null;
            this.currentConversationId = null;
            this.elements = elements;

            this.init();
        }

        init() {
            this.fetchConversations();
            this.connectWebSocket();
            this.setupEventListeners();
        }

        setupEventListeners() {
            const { messageInput, sendButton } = this.elements;

            sendButton.addEventListener('click', () => this.sendMessage());
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.sendMessage();
            });
        }

        connectWebSocket() {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect({}, (frame) => {
                console.log('Connected: ' + frame);
                this.stompClient.subscribe('/user/queue/messages', (msg) => {
                    this.onMessageReceived(JSON.parse(msg.body));
                });
            }, (error) => {
                console.error('Connection error:', error);
            });
        }

        onMessageReceived(message) {
            const { conversationId } = message;
            if (conversationId === this.currentConversationId) {
                this.appendMessage(message);
            } else {
                const target = document.querySelector(`a[data-conversation-id="${conversationId}"]`);
                if (target && !target.classList.contains('active')) {
                    target.classList.add('has-new-message');
                }
            }
        }

        sendMessage() {
            const content = this.elements.messageInput.value.trim();
            if (content && this.stompClient && this.currentConversationId) {
                const chatMessage = { content };
                this.stompClient.send(`/app/chat/${this.currentConversationId}/sendMessage`, {}, JSON.stringify(chatMessage));
                this.elements.messageInput.value = '';
            }
        }

        appendMessage(message) {
            const { chatMessages } = this.elements;

            chatMessages.querySelector('.no-messages')?.remove();

            const msg = document.createElement('div');
            msg.className = `message ${message.sentByCurrentUser ? 'sent' : 'received'}`;
            msg.textContent = message.context;

            chatMessages.appendChild(msg);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        async fetchMessages(conversationId) {
            const { chatMessages } = this.elements;

            chatMessages.innerHTML = '<div>Loading messages...</div>';
            this.currentConversationId = conversationId;

            try {
                const res = await fetch(`/chat/${conversationId}/messages`, { credentials: 'include' });
                if (!res.ok) throw new Error(`Network error: ${res.status}`);
                const messages = await res.json();
                this.displayMessages(messages);
            } catch (e) {
                console.error('Failed to fetch messages:', e);
                chatMessages.innerHTML = `<div class="message received text-danger">Failed to load messages.</div>`;
            }
        }

        displayMessages(messages) {
            const { chatMessages } = this.elements;

            chatMessages.innerHTML = '';

            if (!messages?.length) {
                chatMessages.innerHTML = '<div class="text-center text-muted no-messages">No messages yet.</div>';
                return;
            }

            messages.forEach(msg => this.appendMessage(msg));
        }

        async fetchConversations() {
            try {
                const res = await fetch('/chat/conversations', { credentials: 'include' });
                if (!res.ok) throw new Error(`Network error: ${res.status}`);
                const conversations = await res.json();
                this.displayConversations(conversations);
            } catch (e) {
                console.error('Failed to fetch conversations:', e);
            }
        }

        displayConversations(conversations) {
            const { conversationList, chatHeader } = this.elements;

            conversationList.innerHTML = '';

            if (!conversations.length) {
                chatHeader.textContent = 'No conversations found.';
                return;
            }

            conversations.forEach((conv, i) => {
                const a = document.createElement('a');
                a.href = '#';
                a.className = 'nav-link';
                a.dataset.conversationId = conv.id;
                a.textContent = `${conv.title}`;

                a.addEventListener('click', (e) => {
                    e.preventDefault();
                    document.querySelectorAll('#conversation-list .nav-link').forEach(link => {
                        link.classList.remove('active', 'has-new-message');
                    });
                    a.classList.add('active');
                    chatHeader.textContent = a.textContent;
                    this.fetchMessages(conv.id);
                });

                conversationList.appendChild(a);
                if (i === 0) a.click();
            });
        }
    }

    new Chat(elements);
});
