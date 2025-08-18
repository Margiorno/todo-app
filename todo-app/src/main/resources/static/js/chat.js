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
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
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
            if (String(conversationId) === String(this.currentConversationId)) {
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

            const messageContainer = document.createElement('div');
            messageContainer.className = `message ${message.sentByCurrentUser ? 'sent' : 'received'}`;

            const filename = message.sender?.profilePicturePath || 'default-avatar.jpg';

            const avatarImg = document.createElement('img');
            avatarImg.className = 'message-avatar';
            avatarImg.src = `/files/profile-pictures/${filename}`;
            avatarImg.alt = 'Profile Picture';

            let avatarContainer;

            if (!message.sentByCurrentUser && message.sender) {
                avatarContainer = document.createElement('a');
                avatarContainer.href = `/profile/${message.sender.id}`;
                avatarContainer.appendChild(avatarImg);
            } else {
                avatarContainer = avatarImg;
            }

            const messageContent = document.createElement('div');
            messageContent.className = 'message-content';

            const senderName = document.createElement('div');
            senderName.className = 'message-sender-name';

            if (!message.sentByCurrentUser && message.sender) {
                senderName.textContent = `${message.sender.firstName} ${message.sender.lastName}`;
            }

            const messageText = document.createElement('div');
            messageText.className = 'message-text';
            messageText.textContent = message.content;

            if (!message.sentByCurrentUser) {
                messageContent.appendChild(senderName);
            }
            messageContent.appendChild(messageText);

            messageContainer.appendChild(avatarContainer);
            messageContainer.appendChild(messageContent);

            chatMessages.appendChild(messageContainer);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        async fetchMessages(conversationId) {
            const { chatMessages } = this.elements;
            chatMessages.innerHTML = '<div class="text-center text-muted p-3">Loading messages...</div>';
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
            if (!conversations || conversations.length === 0) {
                chatHeader.textContent = 'No conversations found.';
                this.elements.chatMessages.innerHTML = '<div class="text-center text-muted no-messages">Create a new conversation to start chatting.</div>';
                return;
            }

            conversations.forEach(conv => {
                const a = document.createElement('a');
                a.href = '#';
                a.className = 'nav-link';
                a.dataset.conversationId = conv.id;
                a.textContent = conv.title;
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
            });

            const targetId = sessionStorage.getItem('openConversationId');
            sessionStorage.removeItem('openConversationId');

            let linkToClick = targetId
                ? conversationList.querySelector(`a[data-conversation-id="${targetId}"]`)
                : conversationList.querySelector('a.nav-link');

            if (linkToClick) {
                linkToClick.click();
            }
        }
    }

    window.chatApp = new Chat(elements);
});