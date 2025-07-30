package com.pm.todoapp.exceptions;

public class UserRequiredException extends RuntimeException {
    public UserRequiredException(String message) {
        super(message);
    }
}
