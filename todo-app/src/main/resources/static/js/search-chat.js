document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('search-conversations-input');
    const conversationList = document.getElementById('conversation-list');

    if (!searchInput || !conversationList) {
        return;
    }

    const filterConversations = (event) => {
        const searchTerm = event.target.value.toLowerCase().trim();

        const conversations = conversationList.querySelectorAll('.nav-link');

        let visibleCount = 0;

        conversations.forEach(conversation => {
            const conversationTitle = conversation.textContent.toLowerCase();

            if (conversationTitle.includes(searchTerm)) {
                conversation.style.display = '';
                visibleCount++;
            } else {
                conversation.style.display = 'none';
            }
        });

        const existingNoResultsMessage = conversationList.querySelector('.no-results-message');
        if (existingNoResultsMessage) {
            existingNoResultsMessage.remove();
        }

        if (visibleCount === 0 && searchTerm) {
            const noResultsMessage = document.createElement('div');
            noResultsMessage.className = 'text-center text-muted p-3 no-results-message';
            noResultsMessage.textContent = 'No conversations found.';
            conversationList.appendChild(noResultsMessage);
        }
    };

    searchInput.addEventListener('input', filterConversations);
});