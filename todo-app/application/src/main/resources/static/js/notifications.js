let stompClient = null;

function loadNotifications() {
    fetch('/notifications/getAll')
        .then(response => response.json())
        .then(notifications => {
            const notificationList = document.getElementById('notification-list');
            notificationList.innerHTML = '';

            if (notifications.length === 0) {
                document.getElementById('no-notifications-message').style.display = 'block';
                return;
            } else {
                document.getElementById('no-notifications-message').style.display = 'none';
            }

            notifications.forEach(notification => {
                const element = createNotificationElement(notification);
                notificationList.appendChild(element);
            });
        })
        .catch(error => console.error('Error fetching notifications:', error));
}

function createFriendRequestElement(notification) {
    const card = document.createElement('div');
    card.id = `notification-${notification.id}`;
    card.className = 'card mb-3 shadow-sm unread';

    if (notification.resolved) {
        card.classList.add('resolved');
    }

    const actionsHtml = !notification.resolved ? `
        <div class="d-flex justify-content-end mt-3 notification-actions">
            <button class="btn btn-success btn-sm me-2 accept-request" data-request-id="${notification.requestId}">Accept</button>
            <button class="btn btn-danger btn-sm decline-request" data-request-id="${notification.requestId}">Decline</button>
        </div>` : '';

    const resolvedStatusHtml = notification.resolved ? `
        <div class="mt-3 text-muted resolved-status">
            <small>This request has been resolved.</small>
        </div>` : '';

    const sender = notification.sender || {};
    const profilePicture = sender.profilePicturePath || 'default-avatar.jpg';
    const senderName = `${sender.firstName || ''} ${sender.lastName || ''}`.trim();

    card.innerHTML = `
        <div class="card-header bg-primary text-white">
            Friend Request
        </div>
        <div class="card-body">
            <div class="d-flex align-items-center">
                <a href="/profile/${sender.id}">
                    <img src="/files/profile-pictures/${profilePicture}"
                         alt="Profile Picture" class="rounded-circle me-3" style="width: 50px; height: 50px; object-fit: cover;">
                </a>
                <div>
                    <p class="card-text mb-0">
                        <a href="/profile/${sender.id}" class="fw-bold text-dark text-decoration-none">
                            ${senderName}
                        </a>
                        <span> ${notification.message}</span>
                    </p>
                </div>
            </div>
            ${actionsHtml}
            ${resolvedStatusHtml}
        </div>
        <div class="card-footer text-muted text-end" style="font-size: 0.8rem;">
            Received: <span>${notification.notificationTime || ''}</span>
        </div>
    `;
    return card;
}

function createDefaultElement(notification) {
    const card = document.createElement('div');
    card.id = `notification-${notification.id}`;
    card.className = 'card mb-3 shadow-sm unread';
    card.innerHTML = `
        <div class="card-header bg-secondary text-white">
            Notification
        </div>
        <div class="card-body">
            <p class="card-text">${notification.message || 'You have a new notification.'}</p>
        </div>
        <div class="card-footer text-muted text-end" style="font-size: 0.8rem;">
            Received: <span>${notification.notificationTime || ''}</span>
        </div>
    `;
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

function handleRequestAction(button, action) {
    const requestId = button.dataset.requestId;
    if (!requestId || !action) {
        return;
    }

    const notificationCard = button.closest('.card');
    if (!notificationCard) return;

    const url = `/social/${requestId}/${action}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                const actionsContainer = notificationCard.querySelector('.notification-actions');
                if (actionsContainer) {
                    actionsContainer.remove();
                }

                notificationCard.classList.add('resolved');

                const cardBody = notificationCard.querySelector('.card-body');
                if (cardBody) {
                    const statusDiv = document.createElement('div');
                    statusDiv.className = 'mt-3 text-muted resolved-status';

                    let statusMessage = 'Request accepted.';
                    if (action === 'decline') {
                        statusMessage = 'Request declined.';
                    }
                    statusDiv.innerHTML = `<small>${statusMessage}</small>`;
                    cardBody.appendChild(statusDiv);
                }
            } else {
                console.error('Failed to process the request.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function connect() {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.error('SockJS or Stomp library not found.');
        return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function() {
        stompClient.subscribe('/user/queue/notification', function(message) {
            const notification = JSON.parse(message.body);
            prependNotification(notification);
        });
    });
}

document.addEventListener('DOMContentLoaded', function() {
    loadNotifications();
});

document.getElementById('notification-list').addEventListener('click', function(event) {
    const acceptButton = event.target.closest('.accept-request');
    const declineButton = event.target.closest('.decline-request');

    if (acceptButton) {
        handleRequestAction(acceptButton, 'accept');
    } else if (declineButton) {
        handleRequestAction(declineButton, 'decline');
    }
});

connect();