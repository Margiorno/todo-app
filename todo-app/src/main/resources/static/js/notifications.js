let stompClient = null;

function createNotificationElement(notification) {
    switch (notification.type) {
        case 'FRIEND_REQUEST':
            return createFriendRequestElement(notification);


        default:
            console.warn('Unknown notification type:', notification.type);
            return createDefaultElement(notification);
    }
}


function createFriendRequestElement(notification) {
    const card = document.createElement('div');
    card.id = `notification-${notification.id}`;
    card.className = 'card mb-2';

    card.innerHTML = `
        <div class="card-body d-flex justify-content-between align-items-center">
            <div>
                <strong>${notification.senderName || 'Someone'}</strong> wants to be your friend.
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
    fetch(`/api/friend-requests/${requestId}/${action}`, {
        method: 'POST',
    }).then(response => {
        if (response.ok) {
            document.getElementById(`notification-${requestId}`).remove();
        } else {
            alert(`Failed to ${action} request.`);
        }
    });
}

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/user/queue/notification', function (message) {
            const notification = JSON.parse(message.body);
            prependNotification(notification);
        });
    });
}

document.getElementById('notification-list').addEventListener('click', function(event) {
    const target = event.target;
    if (target.classList.contains('accept-request')) {
        const requestId = target.dataset.requestId;
        handleRequestAction(requestId, 'accept');
    } else if (target.classList.contains('decline-request')) {
        const requestId = target.dataset.requestId;
        handleRequestAction(requestId, 'decline');
    }
});


connect();