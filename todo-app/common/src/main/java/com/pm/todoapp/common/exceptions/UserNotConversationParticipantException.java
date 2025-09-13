package com.pm.todoapp.common.exceptions;

public class UserNotConversationParticipantException  extends RuntimeException {
    public UserNotConversationParticipantException(String message) {
        super(message);
    }
}
