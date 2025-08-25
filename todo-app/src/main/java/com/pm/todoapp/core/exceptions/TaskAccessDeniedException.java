package com.pm.todoapp.core.exceptions;

public class TaskAccessDeniedException extends RuntimeException {
    public TaskAccessDeniedException(String message) {
        super(message);
    }
}
