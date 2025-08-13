let stompClient = null;

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
}

function createFriendRequestElement(notification) {
    const card = document.createElement('div');
    card.id = `notification-${notification.id}`;
    card.className = 'card mb-2';

    card.innerHTML = `
        <div class="card-body d-flex justify-content-between align-items-center">
            <div>
                <span>${notification.message || 'You have a new friend request.'}</span>
            </div>
            <div>
                <button class="btn btn-success btn-sm accept-request" data-request-id="${notification.id}">Accept</button>
                <button class="btn btn-danger btn-sm decline-request" data-request-id="${notification.id}">Decline</button>
            </div>
        </div>
    `;
    return card;
}

function createDefaultElement(notification) {
    const card = document.createElement('div');
    card.id = `notification-${notification.id}`;
    card.className = 'card mb-2';
    card.innerHTML = `<div class="card-body">${notification.message || 'You have a new notification.'}</div>`;
    return card;
}

function createNotificationElement(notification) {
    switch (notification.type) {
        case 'FRIEND_REQUEST':
            return createFriendRequestElement(notification);
        case 'FRIEND_REQUEST_ACCEPTED':
            return createDefaultElement(notification);
        default:
            return createDefaultElement(notification);
    }
}

function prependNotification(notification) {
    const notificationList = document.getElementById('notification-list');
    const noNotificationsMessage = document.getElementById('no-notifications-message');
    const newNotificationElement = createNotificationElement(notification);

    if (newNotificationElement) {
        notificationList.prepend(newNotificationElement);
        if (noNotificationsMessage) {
            noNotificationsMessage.style.display = 'none';
        }
    }
}

function handleRequestAction(requestId, action) {
    if (!requestId || !action) {
        alert("Client error: Missing request ID.");
        return;
    }

    const url = `/friend-requests/${requestId}/${action}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            return response.text().then(() => {
                if (response.ok) {
                    const elementToRemove = document.getElementById(`notification-${requestId}`);
                    if (elementToRemove) {
                        elementToRemove.remove();
                    }
                }
            });
        })
        .catch(error => {
            console.error(error);
        });
}

function connect() {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.error('SockJS or Stomp library not found.');
        return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe('/user/queue/notification', function (message) {
            const notification = JSON.parse(message.body);
            prependNotification(notification);
        });
    });
}

document.getElementById('notification-list').addEventListener('click', function(event) {
    const acceptButton = event.target.closest('.accept-request');
    const declineButton = event.target.closest('.decline-request');

    if (acceptButton) {
        handleRequestAction(acceptButton.dataset.requestId, 'accept');
    } else if (declineButton) {
        handleRequestAction(declineButton.dataset.requestId, 'decline');
    }
});

connect();