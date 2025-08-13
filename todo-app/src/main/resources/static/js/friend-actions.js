let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        initializeFriendButton();
    });
}


function sendFriendRequest(receiverId) {
    if (stompClient && receiverId) {
        const destination = `/app/user/${receiverId}/invite`;

        stompClient.send(destination, {}, JSON.stringify({}));

        console.log(`Friend request sent to ${receiverId}`);

        updateButtonAfterRequest();
    }
}

function updateButtonAfterRequest() {
    const addFriendBtn = document.getElementById('add-friend-btn');
    if (addFriendBtn) {
        addFriendBtn.innerText = 'Request Sent';
        addFriendBtn.disabled = true;
        addFriendBtn.classList.remove('btn-success');
        addFriendBtn.classList.add('btn-secondary');
    }
}

function initializeFriendButton() {
    const addFriendBtn = document.getElementById('add-friend-btn');
    if (addFriendBtn) {
        addFriendBtn.addEventListener('click', function() {
            const profileId = this.getAttribute('data-profile-id');
            sendFriendRequest(profileId);
        });
    }
}

connect();


document.addEventListener('DOMContentLoaded', () => {
    const sendRequest = async (url) => {
        const response = await fetch(url, {
            method: 'POST',
        });
        if (response.ok) {
            window.location.reload();
        } else {
            console.error('Request failed:', response.statusText);
        }
    };

    const acceptBtn = document.getElementById('accept-invitation-btn');
    if (acceptBtn) {
        acceptBtn.addEventListener('click', () => {
            const requestId = acceptBtn.getAttribute('data-request-id');
            sendRequest(`/friend-requests/${requestId}/accept`);
        });
    }

    const declineBtn = document.getElementById('decline-invitation-btn');
    if (declineBtn) {
        declineBtn.addEventListener('click', () => {
            const requestId = declineBtn.getAttribute('data-request-id');
            sendRequest(`/friend-requests/${requestId}/decline`);
        });
    }

    const cancelBtn = document.getElementById('cancel-invitation-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            const requestId = cancelBtn.getAttribute('data-request-id');
            sendRequest(`/friend-requests/${requestId}/cancel`);
        });
    }

    const removeFriendBtn = document.getElementById('remove-friend-btn');
    if (removeFriendBtn) {
        removeFriendBtn.addEventListener('click', () => {
            const friendId = removeFriendBtn.getAttribute('data-friend-id');
            sendRequest(`/friend-requests/${friendId}/remove`);
        });
    }
});