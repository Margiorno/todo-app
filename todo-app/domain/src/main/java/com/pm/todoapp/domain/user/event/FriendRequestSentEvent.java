package com.pm.todoapp.domain.user.event;

import java.util.UUID;

public record FriendRequestSentEvent(UUID requestId, UUID senderId, UUID receiverId) { }
