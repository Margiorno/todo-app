package com.pm.todoapp.common.exceptions;

public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException(String message) {
        super(message);
    }
}
