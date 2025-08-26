package com.pm.todoapp.domain.user.event;

import java.util.UUID;

public record FriendRequestResolvedEvent(UUID requestId) { }
