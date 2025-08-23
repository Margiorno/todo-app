package com.pm.todoapp.core.user.event;

import java.util.UUID;

public record FriendRequestResolvedEvent(UUID requestId) { }
