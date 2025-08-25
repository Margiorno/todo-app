document.addEventListener('DOMContentLoaded', () => {
    const sendRequest = async (url) => {
        const response = await fetch(url, { method: 'POST' });
        if (response.ok) {
            window.location.reload();
        } else {
            console.error('Request failed:', response.statusText);
        }
    };

    const addFriendBtn = document.getElementById('add-friend-btn');
    if (addFriendBtn) {
        addFriendBtn.addEventListener('click', () => {
            const profileId = addFriendBtn.getAttribute('data-profile-id');
            sendRequest(`/social/${profileId}/invite`);
        });
    }

    const acceptBtn = document.getElementById('accept-invitation-btn');
    if (acceptBtn) {
        acceptBtn.addEventListener('click', () => {
            const requestId = acceptBtn.getAttribute('data-request-id');
            sendRequest(`/social/${requestId}/accept`);
        });
    }

    const declineBtn = document.getElementById('decline-invitation-btn');
    if (declineBtn) {
        declineBtn.addEventListener('click', () => {
            const requestId = declineBtn.getAttribute('data-request-id');
            sendRequest(`/social/${requestId}/decline`);
        });
    }

    const cancelBtn = document.getElementById('cancel-invitation-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            const requestId = cancelBtn.getAttribute('data-request-id');
            sendRequest(`/social/${requestId}/cancel`);
        });
    }

    const removeFriendBtn = document.getElementById('remove-friend-btn');
    if (removeFriendBtn) {
        removeFriendBtn.addEventListener('click', () => {
            const friendId = removeFriendBtn.getAttribute('data-friend-id');
            sendRequest(`/social/${friendId}/remove`);
        });
    }
});
