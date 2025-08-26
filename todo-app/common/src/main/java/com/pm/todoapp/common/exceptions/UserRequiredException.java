package com.pm.todoapp.common.exceptions;

public class UserRequiredException extends RuntimeException {
    public UserRequiredException(String message) {
        super(message);
    }
}
