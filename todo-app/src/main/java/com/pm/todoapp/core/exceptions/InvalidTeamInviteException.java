package com.pm.todoapp.core.exceptions;

public class InvalidTeamInviteException extends RuntimeException {
    public InvalidTeamInviteException(String message) {
        super(message);
    }
}
