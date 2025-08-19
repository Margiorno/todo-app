package com.pm.todoapp.core.exceptions;

public class TeamRequiredException extends RuntimeException {
    public TeamRequiredException(String message) {
        super(message);
    }
}
