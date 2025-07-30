package com.pm.todoapp.exceptions;

public class TeamRequiredException extends RuntimeException {
    public TeamRequiredException(String message) {
        super(message);
    }
}
