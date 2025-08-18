document.addEventListener('DOMContentLoaded', () => {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap JavaScript library is not loaded.');
        return;
    }

    const createFriendListItem = (friend, inputType) => {
        const friendItem = document.createElement('label');
        friendItem.className = 'list-group-item d-flex justify-content-between align-items-center';
        const userInfoContainer = document.createElement('div');
        userInfoContainer.className = 'd-flex align-items-center';
        const profileImg = document.createElement('img');
        const filename = friend.profilePicturePath || 'default-avatar.jpg';
        profileImg.src = `/files/profile-pictures/${filename}`;
        profileImg.alt = 'Profile Picture';
        profileImg.className = 'rounded-circle me-3';
        profileImg.style.width = '40px';
        profileImg.style.height = '40px';
        profileImg.style.objectFit = 'cover';
        const nameSpan = document.createElement('span');
        nameSpan.className = 'friend-name';
        nameSpan.textContent = `${friend.firstName} ${friend.lastName}`;
        const input = document.createElement('input');
        input.type = inputType;
        input.className = 'form-check-input';
        input.value = friend.id;
        if (inputType === 'radio') {
            input.name = 'recipient';
        }
        userInfoContainer.appendChild(profileImg);
        userInfoContainer.appendChild(nameSpan);
        friendItem.appendChild(userInfoContainer);
        friendItem.appendChild(input);
        return friendItem;
    };

    const setupModal = (config) => {
        const modalEl = document.getElementById(config.modalId);
        if (!modalEl) return;
        const modalInstance = new bootstrap.Modal(modalEl);
        const searchInput = document.getElementById(config.searchInputId);
        const friendsListEl = document.getElementById(config.friendsListId);
        const noResultsEl = document.getElementById(config.noResultsId);
        const actionBtn = document.getElementById(config.actionBtnId);
        const conversationNameInput = config.conversationNameId ? document.getElementById(config.conversationNameId) : null;
        const selectedShelfEl = config.selectedShelfId ? document.getElementById(config.selectedShelfId) : null;

        const updateSelectedShelf = () => {
            if (!selectedShelfEl) return;
            selectedShelfEl.innerHTML = '';
            const selectedFriends = Array.from(friendsListEl.querySelectorAll('input[type="checkbox"]:checked'));
            if (selectedFriends.length > 0) {
                selectedShelfEl.style.display = 'flex';
            } else {
                selectedShelfEl.style.display = 'none';
            }
            selectedFriends.forEach(checkbox => {
                const friendId = checkbox.value;
                const friendItem = checkbox.closest('.list-group-item');
                const friendName = friendItem.querySelector('.friend-name').textContent;
                const pill = document.createElement('div');
                pill.className = 'selected-friend-pill';
                pill.textContent = friendName;
                const removeBtn = document.createElement('span');
                removeBtn.className = 'remove-friend-btn';
                removeBtn.innerHTML = '&times;';
                removeBtn.dataset.friendId = friendId;
                removeBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    const targetCheckbox = friendsListEl.querySelector(`input[value="${friendId}"]`);
                    if (targetCheckbox) {
                        targetCheckbox.checked = false;
                        updateSelectedShelf();
                    }
                });
                pill.appendChild(removeBtn);
                selectedShelfEl.appendChild(pill);
            });
        };

        const displayFriends = (friends) => {
            friendsListEl.innerHTML = '';
            if (!friends || friends.length === 0) {
                noResultsEl.textContent = config.noFriendsMessage;
                noResultsEl.style.display = 'block';
                return;
            }
            noResultsEl.textContent = 'No friends found.';
            noResultsEl.style.display = 'none';
            friends.forEach(friend => {
                const friendItem = createFriendListItem(friend, config.inputType);
                friendsListEl.appendChild(friendItem);
            });
            if (config.inputType === 'checkbox') {
                friendsListEl.addEventListener('change', (e) => {
                    if (e.target.type === 'checkbox') {
                        updateSelectedShelf();
                    }
                });
            }
        };

        const loadFriends = async () => {
            friendsListEl.innerHTML = '<div class="text-center">Loading friends...</div>';
            noResultsEl.style.display = 'none';
            try {
                const response = await fetch('/users/friends');
                if (!response.ok) throw new Error(`Network error: ${response.status}`);
                const friends = await response.json();
                displayFriends(friends);
            } catch (error) {
                console.error('Failed to fetch friends:', error);
                friendsListEl.innerHTML = '<div class="text-danger">Failed to load friends.</div>';
            }
        };

        const filterFriends = () => {
            const searchTerm = searchInput.value.toLowerCase().trim();
            const friends = friendsListEl.querySelectorAll('.list-group-item');
            let visibleCount = 0;
            const hasFriendsInList = friends.length > 0;

            friends.forEach(friend => {
                const nameSpan = friend.querySelector('.friend-name');
                if (nameSpan) {
                    if (nameSpan.textContent.toLowerCase().includes(searchTerm)) {
                        friend.classList.remove('d-none');
                        visibleCount++;
                    } else {
                        friend.classList.add('d-none');
                    }
                }
            });

            if (visibleCount === 0 && hasFriendsInList) {
                noResultsEl.style.display = 'block';
            } else {
                noResultsEl.style.display = 'none';
            }
        };

        const resetModal = () => {
            searchInput.value = '';
            if (conversationNameInput) conversationNameInput.value = '';
            friendsListEl.innerHTML = '';
            noResultsEl.style.display = 'none';

            if (selectedShelfEl) {
                selectedShelfEl.innerHTML = '';
                selectedShelfEl.style.display = 'none';
            }
        };

        modalEl.addEventListener('show.bs.modal', loadFriends);
        modalEl.addEventListener('hidden.bs.modal', resetModal);
        searchInput.addEventListener('input', filterFriends);
        actionBtn.addEventListener('click', () => config.actionHandler(modalInstance, { friendsListEl, conversationNameInput }));
    };

    const handleOpenChat = async (modalInstance, elements) => {
        const selectedFriendRadio = elements.friendsListEl.querySelector('input[type="radio"]:checked');
        if (!selectedFriendRadio) {
            alert('Please select a friend to start a chat.');
            return;
        }
        const friendId = selectedFriendRadio.value;
        try {
            const response = await fetch(`/chat/get-chat/${friendId}`, { credentials: 'include' });
            if (!response.ok) throw new Error('Server could not find or create the conversation.');
            const conversation = await response.json();
            sessionStorage.setItem('openConversationId', conversation.id);
            modalInstance.hide();
            window.location.reload();
        } catch (error) {
            console.error('Failed to open chat:', error);
            alert('An error occurred while trying to open the chat.');
        }
    };

    const handleCreateConversation = async (modalInstance, elements) => {
        const conversationName = elements.conversationNameInput.value.trim();
        const selectedFriends = Array.from(elements.friendsListEl.querySelectorAll('input[type="checkbox"]:checked')).map(cb => cb.value);

        if (!conversationName) {
            alert('Please enter a conversation name.');
            return;
        }
        if (selectedFriends.length < 1) {
            alert('Please select at least one friend to start a conversation.');
            return;
        }

        try {
            const response = await fetch('/chat/new', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ conversationName, participantIds: selectedFriends })
            });
            if (response.ok) {
                const conversation = await response.json();
                sessionStorage.setItem('openConversationId', conversation.id);
                modalInstance.hide();
                window.location.reload();
            } else {
                alert(`Error: Could not create conversation. ${await response.text()}`);
            }
        } catch (error) {
            alert('An unexpected error occurred while creating the conversation.');
        }
    };

    setupModal({
        modalId: 'newMessageModal',
        searchInputId: 'search-friends-send-message',
        friendsListId: 'newMessageFriendsList',
        noResultsId: 'newMessageFriendsList-no-results',
        actionBtnId: 'sendMessageBtn',
        inputType: 'radio',
        noFriendsMessage: 'You have no friends to chat with.',
        actionHandler: handleOpenChat
    });

    setupModal({
        modalId: 'newConversationModal',
        searchInputId: 'search-friends-new-conversation',
        friendsListId: 'friendsList',
        noResultsId: 'friendsList-no-results',
        actionBtnId: 'createConversationBtn',
        conversationNameId: 'conversationName',
        inputType: 'checkbox',
        selectedShelfId: 'selectedFriendsShelf',
        noFriendsMessage: 'You have no friends to add.',
        actionHandler: handleCreateConversation
    });
});