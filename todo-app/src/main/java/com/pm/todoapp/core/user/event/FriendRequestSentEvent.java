package com.pm.todoapp.core.user.event;

import java.util.UUID;

public record FriendRequestSentEvent(UUID requestId, UUID senderId, UUID receiverId) { }
