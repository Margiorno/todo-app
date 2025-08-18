document.addEventListener('DOMContentLoaded', () => {

    const newConversationModalEl = document.getElementById('newConversationModal');
    if (typeof bootstrap !== 'undefined') {
        const newConversationModal = new bootstrap.Modal(newConversationModalEl);
        const friendsListEl = document.getElementById('friendsList');
        const createConversationBtn = document.getElementById('createConversationBtn');
        const conversationNameInput = document.getElementById('conversationName');

        const loadFriends = async () => {
            friendsListEl.innerHTML = '<div class="text-center">Loading friends...</div>';
            try {
                const response = await fetch('/users/friends');
                if (!response.ok) {
                    throw new Error(`Network error: ${response.status}`);
                }
                const friends = await response.json();
                displayFriends(friends);
            } catch (error) {
                console.error('Failed to fetch friends:', error);
                friendsListEl.innerHTML = '<div class="text-danger">Failed to load friends.</div>';
            }
        };

        const displayFriends = (friends) => {
            const friendsListEl = document.getElementById('friendsList');
            friendsListEl.innerHTML = ''; // Wyczyść listę

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
                const checkbox = document.createElement('input');

                const filename = friend.profilePicturePath || 'default-avatar.jpg';

                const imageUrl = `/files/profile-pictures/${filename}`;

                profileImg.src = imageUrl;
                profileImg.alt = 'Profile Picture';
                profileImg.className = 'rounded-circle me-3';
                profileImg.style.width = '40px';
                profileImg.style.height = '40px';
                profileImg.style.objectFit = 'cover';

                nameSpan.textContent = `${friend.firstName} ${friend.lastName}`;

                checkbox.type = 'checkbox';
                checkbox.className = 'form-check-input';
                checkbox.value = friend.id;

                userInfoContainer.appendChild(profileImg);
                userInfoContainer.appendChild(nameSpan);
                friendItem.appendChild(userInfoContainer);
                friendItem.appendChild(checkbox);

                friendsListEl.appendChild(friendItem);
            });
        };

        newConversationModalEl.addEventListener('show.bs.modal', () => {
            loadFriends();
        });

        newConversationModalEl.addEventListener('hidden.bs.modal', () => {
            conversationNameInput.value = '';
            friendsListEl.innerHTML = '';
        });

        const createConversation = async () => {
            const conversationName = conversationNameInput.value.trim();
            const selectedFriends = Array.from(friendsListEl.querySelectorAll('input[type="checkbox"]:checked'))
                .map(checkbox => checkbox.value);

            if (!conversationName) {
                alert('Please enter a conversation name.');
                return;
            }

            if (selectedFriends.length < 1) {
                alert('Please select at least one friend to start a conversation.');
                return;
            }

            const conversationRequest = {
                conversationName: conversationName,
                participantIds: selectedFriends
            };

            try {
                const response = await fetch('/chat/new', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(conversationRequest)
                });

                if (response.ok) {
                    newConversationModal.hide();
                    window.location.reload();
                } else {
                    const errorText = await response.text();
                    console.error('Failed to create conversation:', errorText);
                    alert(`Error: Could not create conversation. ${errorText}`);
                }
            } catch (error) {
                console.error('Error creating conversation:', error);
                alert('An unexpected error occurred. Please try again.');
            }
        };

        createConversationBtn.addEventListener('click', createConversation);
    } else {
        console.error('Bootstrap is not defined. Make sure it is loaded before this script.');
    }
});