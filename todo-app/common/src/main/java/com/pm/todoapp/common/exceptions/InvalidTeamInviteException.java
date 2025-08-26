package com.pm.todoapp.common.exceptions;

public class InvalidTeamInviteException extends RuntimeException {
    public InvalidTeamInviteException(String message) {
        super(message);
    }
}
