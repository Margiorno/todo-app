package com.pm.todoapp.common.exceptions;

public class InvalidFriendRequestAccessException extends RuntimeException {
    public InvalidFriendRequestAccessException(String message) {
        super(message);
    }
}
