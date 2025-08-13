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