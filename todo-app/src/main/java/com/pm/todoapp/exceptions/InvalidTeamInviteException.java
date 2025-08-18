package com.pm.todoapp.exceptions;

public class InvalidTeamInviteException extends RuntimeException {
    public InvalidTeamInviteException(String message) {
        super(message);
    }
}
