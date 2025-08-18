document.addEventListener('DOMContentLoaded', () => {
    const newMessageModalEl = document.getElementById('newMessageModal');
    if (!newMessageModalEl) return;

    if (typeof bootstrap !== 'undefined') {
        const newMessageModal = new bootstrap.Modal(newMessageModalEl);
        const friendsListEl = document.getElementById('newMessageFriendsList');
        const openChatBtn = document.getElementById('sendMessageBtn');

        const loadFriendsForMessage = async () => {
            friendsListEl.innerHTML = '<div class="text-center">Loading friends...</div>';
            try {
                const response = await fetch('/users/friends');
                if (!response.ok) throw new Error(`Network error: ${response.status}`);
                const friends = await response.json();
                displayFriendsForMessage(friends);
            } catch (error) {
                console.error('Failed to fetch friends:', error);
                friendsListEl.innerHTML = '<div class="text-danger">Failed to load friends.</div>';
            }
        };

        const displayFriendsForMessage = (friends) => {
            friendsListEl.innerHTML = '';
            if (!friends || friends.length === 0) {
                friendsListEl.innerHTML = '<div class="text-muted">No friends found.</div>';
                return;
            }

            friends.forEach(friend => {
                const friendItem = document.createElement('label');
                friendItem.className = 'list-group-item d-flex justify-content-between align-items-center';
                const userInfoContainer = document.createElement('div');
                userInfoContainer.className = 'd-flex align-items-center';
                const profileImg = document.createElement('img');
                const nameSpan = document.createElement('span');
                const radio = document.createElement('input');

                const filename = friend.profilePicturePath || 'default-avatar.jpg';
                const imageUrl = `/files/profile-pictures/${filename}`;

                profileImg.src = imageUrl;
                profileImg.alt = 'Profile Picture';
                profileImg.className = 'rounded-circle me-3';
                profileImg.style.width = '40px';
                profileImg.style.height = '40px';
                profileImg.style.objectFit = 'cover';

                nameSpan.textContent = `${friend.firstName} ${friend.lastName}`;
                radio.type = 'radio';
                radio.className = 'form-check-input';
                radio.value = friend.id;
                radio.name = 'recipient';

                userInfoContainer.appendChild(profileImg);
                userInfoContainer.appendChild(nameSpan);
                friendItem.appendChild(userInfoContainer);
                friendItem.appendChild(radio);
                friendsListEl.appendChild(friendItem);
            });
        };

        const handleOpenChat = async () => {
            const selectedFriendRadio = friendsListEl.querySelector('input[type="radio"]:checked');

            if (!selectedFriendRadio) {
                alert('Please select a friend to start a chat.');
                return;
            }

            const friendId = selectedFriendRadio.value;

            try {
                const response = await fetch(`/chat/get-chat/${friendId}`, {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Server could not find or create the conversation.');
                }

                newMessageModal.hide();
                window.location.reload();

            } catch (error) {
                console.error('Failed to open chat:', error);
                alert('An error occurred while trying to open the chat.');
            }
        };

        newMessageModalEl.addEventListener('show.bs.modal', loadFriendsForMessage);
        openChatBtn.addEventListener('click', handleOpenChat);

        newMessageModalEl.addEventListener('hidden.bs.modal', () => {
            friendsListEl.innerHTML = '';
        });

    } else {
        console.error('Bootstrap is not defined. Make sure it is loaded before send-message.js');
    }
});