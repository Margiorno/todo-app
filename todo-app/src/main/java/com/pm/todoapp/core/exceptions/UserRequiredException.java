package com.pm.todoapp.core.exceptions;

public class UserRequiredException extends RuntimeException {
    public UserRequiredException(String message) {
        super(message);
    }
}
